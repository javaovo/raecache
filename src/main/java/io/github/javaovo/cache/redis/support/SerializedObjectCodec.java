/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.github.javaovo.cache.redis.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.lettuce.core.codec.RedisCodec;

public class SerializedObjectCodec implements RedisCodec<String, Object> {

	private Charset charset = Charset.forName("UTF-8");

	@Override
	public String decodeKey(ByteBuffer bytes) {
		return charset.decode(bytes).toString();
	}

	@Override
	public Object decodeValue(ByteBuffer bytes) {
		try {
			byte[] array = new byte[bytes.remaining()];
			bytes.get(array);
			ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(array));
			return is.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public ByteBuffer encodeKey(String key) {
		return charset.encode(key);
	}

	@Override
	public ByteBuffer encodeValue(Object value) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			return ByteBuffer.wrap(baos.toByteArray());
		} catch (IOException e) {
			return null;
		} finally {
			try {
				oos.close();
				baos.close();
			} catch (Exception e) {

			}
		}
	}
}