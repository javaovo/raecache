/*
 * Copyright 2011-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.javaovo.cache.redis.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.internal.LettuceAssert;

/**
 * A compressing/decompressing {@link RedisCodec} that wraps a typed {@link RedisCodec codec} and compresses values using GZIP
 * or Deflate. See {@link io.lettuce.core.codec.CompressionCodec.CompressionType} for supported compression types.
 *
 * @author Mark Paluch
 */
public class Redis4CompressionCodec {
	private static final int ZLIB_MAGIC_1 = 0x78;
	private static final int ZLIB_MAGIC_2a = 0x01;
	private static final int ZLIB_MAGIC_2b = 0x5e;
	private static final int ZLIB_MAGIC_2c = 0x9c;
	private static final int ZLIB_MAGIC_2d = 0xda;

	/**
	 * A {@link RedisCodec} that compresses values from a delegating {@link RedisCodec}.
	 *
	 * @param delegate codec used for key-value encoding/decoding, must not be {@literal null}.
	 * @param compressionType the compression type, must not be {@literal null}.
	 * @param <K> Key type.
	 * @param <V> Value type.
	 * @return Value-compressing codec.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <K, V> RedisCodec<K, V> valueCompressor(RedisCodec<K, V> delegate, CompressionType compressionType) {
		LettuceAssert.notNull(delegate, "RedisCodec must not be null");
		LettuceAssert.notNull(compressionType, "CompressionType must not be null");
		return (RedisCodec) new CompressingValueCodecWrapper((RedisCodec) delegate, compressionType);
	}

	private static class CompressingValueCodecWrapper implements RedisCodec<Object, Object> {

		private RedisCodec<Object, Object> delegate;
		private CompressionType compressionType;

		public CompressingValueCodecWrapper(RedisCodec<Object, Object> delegate, CompressionType compressionType) {
			this.delegate = delegate;
			this.compressionType = compressionType;
		}

		@Override
		public Object decodeKey(ByteBuffer bytes) {
			return delegate.decodeKey(bytes);
		}

		@Override
		public Object decodeValue(ByteBuffer bytes) {
			try {
				Object obj = delegate.decodeValue(decompress(bytes));
				return obj;
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public ByteBuffer encodeKey(Object key) {
			return delegate.encodeKey(key);
		}

		@Override
		public ByteBuffer encodeValue(Object value) {
			try {
				return compress(delegate.encodeValue(value));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		private ByteBuffer compress(ByteBuffer source) throws IOException {
			if (source.remaining() == 0) {
				return source;
			}

			ByteBufferInputStream sourceStream = new ByteBufferInputStream(source);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.remaining() / 2);
			OutputStream compressor = null;

			if (compressionType == CompressionType.GZIP) {
				compressor = new GZIPOutputStream(outputStream);
			} else if (compressionType == CompressionType.DEFLATE) {
				compressor = new DeflaterOutputStream(outputStream);
			}

			try {
				copy(sourceStream, compressor);
			} finally {
				compressor.close();
			}

			return ByteBuffer.wrap(outputStream.toByteArray());
		}

		private ByteBuffer decompress(ByteBuffer source) throws IOException {
			if (source.remaining() == 0) {
				return source;
			}
			ByteBuffer readonly = source.asReadOnlyBuffer();
			byte[] signature = new byte[2];
			int len = readonly.remaining();
			readonly.get(signature, 0, 2);
			readonly.clear();

			if (compressionType == CompressionType.GZIP) {
				if (!isGzip(signature)) {
					return source;
				}
			} else if (compressionType == CompressionType.DEFLATE) {
				if (!isZlib(signature, len)) {
					return source;
				}
			} else {
				return source;
			}

			ByteBufferInputStream sourceStream = new ByteBufferInputStream(source);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.remaining() * 2);
			InputStream decompressor = null;
			if (compressionType == CompressionType.GZIP) {
				decompressor = new GZIPInputStream(sourceStream);
			} else if (compressionType == CompressionType.DEFLATE) {
				decompressor = new InflaterInputStream(sourceStream);
			}

			try {
				copy(decompressor, outputStream);
			} finally {
				decompressor.close();
			}

			return ByteBuffer.wrap(outputStream.toByteArray());
		}

		private boolean isZlib(final byte[] signature, final int length) {
			return length > 3 && signature[0] == ZLIB_MAGIC_1 && (signature[1] == (byte) ZLIB_MAGIC_2a || signature[1] == (byte) ZLIB_MAGIC_2b
					|| signature[1] == (byte) ZLIB_MAGIC_2c || signature[1] == (byte) ZLIB_MAGIC_2d);
		}

		private boolean isGzip(final byte[] signature) {
			int magic = (signature[1] & 0xFF) << 8 | signature[0];
			if (magic != GZIPInputStream.GZIP_MAGIC) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Copies all bytes from the input stream to the output stream. Does not close or flush either stream.
	 *
	 * @param from the input stream to read from
	 * @param to the output stream to write to
	 * @return the number of bytes copied
	 * @throws IOException if an I/O error occurs
	 */
	private static long copy(InputStream from, OutputStream to) throws IOException {
		LettuceAssert.notNull(from, "From must not be null");
		LettuceAssert.notNull(to, "From must not be null");
		byte[] buf = new byte[4096];
		long total = 0;
		while (true) {
			int r = from.read(buf);
			if (r == -1) {
				break;
			}
			to.write(buf, 0, r);
			total += r;
		}
		return total;
	}

	public enum CompressionType {
		GZIP, DEFLATE;
	}

	private static class ByteBufferInputStream extends InputStream {

		private final ByteBuffer buffer;

		public ByteBufferInputStream(ByteBuffer b) {
			this.buffer = b;
		}

		@Override
		public int available() throws IOException {
			return buffer.remaining();
		}

		@Override
		public int read() throws IOException {
			if (buffer.remaining() > 0) {
				return (buffer.get() & 0xFF);
			}
			return -1;
		}
	}

}
