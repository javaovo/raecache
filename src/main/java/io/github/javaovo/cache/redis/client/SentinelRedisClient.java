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
package io.github.javaovo.cache.redis.client;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPool;

import io.github.javaovo.cache.CacheException;
import io.github.javaovo.cache.redis.support.Redis4ClientConfig;
import io.github.javaovo.cache.redis.support.Redis4CompressionCodec;
import io.github.javaovo.cache.redis.support.Redis4CompressionCodec.CompressionType;
import io.github.javaovo.cache.redis.support.SerializedObjectCodec;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoArgs.Unit;
import io.lettuce.core.GeoCoordinates;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.GeoWithin;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.KeyValue;
import io.lettuce.core.KillArgs;
import io.lettuce.core.Limit;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.Range;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScoredValueScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.SortArgs;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.Value;
import io.lettuce.core.ValueScanCursor;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ScoredValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.support.ConnectionPoolSupport;

public class SentinelRedisClient implements RaeRedisClient<String, Object> {

	private GenericObjectPool<StatefulRedisConnection<String, Object>> pool;

	private RedisClient lettuceJedisClient;

	private String ns;

	public SentinelRedisClient(Redis4ClientConfig config, RedisURI redisURI, String ns) {
		if (redisURI == null) {
			return;
		}
		this.ns = ns;
		lettuceJedisClient = RedisClient.create(redisURI);
		lettuceJedisClient.setOptions(ClientOptions.builder().autoReconnect(true).build());

		final RedisCodec<String, Object> redisCodes;
		switch (config.getCompress()) {
		case "GZIP":
			redisCodes = Redis4CompressionCodec.valueCompressor(new SerializedObjectCodec(), CompressionType.GZIP);
			break;
		case "DEFLATE":
			redisCodes = Redis4CompressionCodec.valueCompressor(new SerializedObjectCodec(), CompressionType.DEFLATE);
			break;
		default:
			redisCodes = new SerializedObjectCodec();
		}

		this.pool = ConnectionPoolSupport.createGenericObjectPool(() -> lettuceJedisClient.connect(redisCodes), config);
	}

	@Override
	public void close() throws IOException {
		pool.close();
		lettuceJedisClient.shutdown();
	}

	@Override
	public String getNs() {
		return ns;
	}

	@Override
	public Long hdel(String key, String... fields) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hdel(key, fields);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean hexists(String key, String field) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hexists(key, field);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object hget(String key, String field) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hget(key, field);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long hincrby(String key, String field, long amount) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hincrby(key, field, amount);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double hincrbyfloat(String key, String field, double amount) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hincrbyfloat(key, field, amount);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Map<String, Object> hgetall(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hgetall(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long hgetall(KeyValueStreamingChannel<String, Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hgetall(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<String> hkeys(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hkeys(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long hkeys(KeyStreamingChannel<String> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hkeys(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long hlen(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hlen(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<KeyValue<String, Object>> hmget(String key, String... fields) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hmget(key, fields);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long hmget(KeyValueStreamingChannel<String, Object> channel, String key, String... fields) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hmget(channel, key, fields);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String hmset(String key, Map<String, Object> map) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hmset(key, map);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public MapScanCursor<String, Object> hscan(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public MapScanCursor<String, Object> hscan(String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public MapScanCursor<String, Object> hscan(String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public MapScanCursor<String, Object> hscan(String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor hscan(KeyValueStreamingChannel<String, Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor hscan(KeyValueStreamingChannel<String, Object> channel, String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(channel, key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor hscan(KeyValueStreamingChannel<String, Object> channel, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(channel, key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor hscan(KeyValueStreamingChannel<String, Object> channel, String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hscan(channel, key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean hset(String key, String field, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hset(key, field, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean hsetnx(String key, String field, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hsetnx(key, field, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long hstrlen(String key, String field) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hstrlen(key, field);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> hvals(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hvals(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long hvals(ValueStreamingChannel<Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().hvals(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long del(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().del(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long unlink(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().unlink(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public byte[] dump(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().dump(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long exists(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().exists(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean expire(String key, long seconds) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().expire(key, seconds);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean expireat(String key, Date timestamp) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().expireat(key, timestamp);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean expireat(String key, long timestamp) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().expireat(key, timestamp);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<String> keys(String pattern) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().keys(pattern);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long keys(KeyStreamingChannel<String> channel, String pattern) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().keys(channel, pattern);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String migrate(String host, int port, String key, int db, long timeout) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().migrate(host, port, key, db, timeout);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String migrate(String host, int port, int db, long timeout, MigrateArgs<String> migrateArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().migrate(host, port, db, timeout, migrateArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean move(String key, int db) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().move(key, db);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String objectEncoding(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().objectEncoding(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long objectIdletime(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().objectIdletime(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long objectRefcount(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().objectRefcount(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean persist(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().persist(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Boolean pexpire(String key, long milliseconds) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pexpire(key, milliseconds);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean pexpireat(String key, Date timestamp) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pexpireat(key, timestamp);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean pexpireat(String key, long timestamp) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pexpireat(key, timestamp);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long pttl(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pttl(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object randomkey() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().randomkey();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String rename(String key, String newKey) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().rename(key, newKey);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean renamenx(String key, String newKey) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().renamenx(key, newKey);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String restore(String key, long ttl, byte[] value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().restore(key, ttl, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> sort(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sort(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long sort(ValueStreamingChannel<Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sort(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> sort(String key, SortArgs sortArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sort(key, sortArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long sort(ValueStreamingChannel<Object> channel, String key, SortArgs sortArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sort(channel, key, sortArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sortStore(String key, SortArgs sortArgs, String destination) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sortStore(key, sortArgs, destination);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long touch(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().touch(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long ttl(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().ttl(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String type(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().type(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyScanCursor<String> scan() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyScanCursor<String> scan(ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyScanCursor<String> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyScanCursor<String> scan(ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor scan(KeyStreamingChannel<String> channel) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(channel);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor scan(KeyStreamingChannel<String> channel, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(channel, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor scan(KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(channel, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public StreamScanCursor scan(KeyStreamingChannel<String> channel, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scan(channel, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long append(String key, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().append(key, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitcount(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitcount(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitcount(key, start, end);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Long> bitfield(String key, BitFieldArgs bitFieldArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitfield(key, bitFieldArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitpos(String key, boolean state) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitpos(key, state);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitpos(String key, boolean state, long start) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitpos(key, state, start);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitpos(String key, boolean state, long start, long end) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitpos(key, state, start, end);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitopAnd(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitopAnd(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitopNot(String destination, String source) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitopNot(destination, source);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitopOr(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitopOr(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long bitopXor(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bitopXor(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long decr(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().decr(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long decrby(String key, long amount) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().decrby(key, amount);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object get(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().get(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long getbit(String key, long offset) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().getbit(key, offset);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object getrange(String key, long start, long end) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().getrange(key, start, end);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object getset(String key, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().getset(key, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long incr(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().incr(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long incrby(String key, long amount) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().incrby(key, amount);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double incrbyfloat(String key, double amount) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().incrbyfloat(key, amount);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<KeyValue<String, Object>> mget(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().mget(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long mget(KeyValueStreamingChannel<String, Object> channel, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().mget(channel, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String mset(Map<String, Object> map) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().mset(map);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean msetnx(Map<String, Object> map) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().msetnx(map);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String set(String key, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().set(key, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String set(String key, Object value, SetArgs setArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().set(key, value, setArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long setbit(String key, long offset, int value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().setbit(key, offset, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String setex(String key, long seconds, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().setex(key, seconds, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String psetex(String key, long milliseconds, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().psetex(key, milliseconds, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Boolean setnx(String key, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().setnx(key, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long setrange(String key, long offset, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().setrange(key, offset, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long strlen(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().strlen(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyValue<String, Object> blpop(long timeout, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().blpop(timeout, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public KeyValue<String, Object> brpop(long timeout, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().brpop(timeout, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object brpoplpush(long timeout, String source, String destination) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().brpoplpush(timeout, source, destination);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object lindex(String key, long index) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lindex(key, index);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long linsert(String key, boolean before, Object pivot, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().linsert(key, before, pivot, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long llen(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().llen(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object lpop(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lpop(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long lpush(String key, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lpush(key, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long lpushx(String key, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lpushx(key, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> lrange(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lrange(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long lrange(ValueStreamingChannel<Object> channel, String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lrange(channel, key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long lrem(String key, long count, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lrem(key, count, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String lset(String key, long index, Object value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lset(key, index, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String ltrim(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().ltrim(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Object rpop(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().rpop(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object rpoplpush(String source, String destination) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().rpoplpush(source, destination);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long rpush(String key, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().rpush(key, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long rpushx(String key, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().rpushx(key, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zadd(String key, double score, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, score, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zadd(String key, Object... scoresAndValues) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, scoresAndValues);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zadd(String key, ScoredValue<Object>... scoredValues) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, scoredValues);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zadd(String key, ZAddArgs zAddArgs, double score, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, zAddArgs, score, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zadd(String key, ZAddArgs zAddArgs, Object... scoresAndValues) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, zAddArgs, scoresAndValues);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zadd(String key, ZAddArgs zAddArgs, ScoredValue<Object>... scoredValues) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zadd(key, zAddArgs, scoredValues);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double zaddincr(String key, double score, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zaddincr(key, score, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double zaddincr(String key, ZAddArgs zAddArgs, double score, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zaddincr(key, zAddArgs, score, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zcard(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zcard(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zcount(String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zcount(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zcount(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zcount(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zcount(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zcount(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double zincrby(String key, double amount, String member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zincrby(key, amount, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zinterstore(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zinterstore(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zinterstore(String destination, ZStoreArgs storeArgs, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zinterstore(destination, storeArgs, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zlexcount(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zlexcount(String key, Range<? extends Object> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zlexcount(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> zrange(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrange(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zrange(ValueStreamingChannel<Object> channel, String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrange(channel, key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangeWithScores(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangeWithScores(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long zrangeWithScores(ScoredValueStreamingChannel<Object> channel, String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangeWithScores(channel, key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> zrangebylex(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebylex(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> zrangebylex(String key, Range<? extends Object> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebylex(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> zrangebylex(String key, String min, String max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebylex(key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> zrangebylex(String key, Range<? extends Object> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebylex(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, double min, double max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, String min, String max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrangebyscore(String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, double min, double max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, String min, String max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscore(ValueStreamingChannel<Object> channel, String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscore(channel, key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, double min, double max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, String min, String max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrangebyscoreWithScores(String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, double min, double max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, String min, String max, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, min, max, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrangebyscoreWithScores(channel, key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrank(String key, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrank(key, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrem(String key, Object... members) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrem(key, members);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebylex(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebylex(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebylex(String key, Range<? extends Object> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebylex(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebyrank(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebyrank(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebyscore(String key, double min, double max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebyscore(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebyscore(String key, String min, String max) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebyscore(key, min, max);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zremrangebyscore(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zremrangebyscore(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrange(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrange(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrange(ValueStreamingChannel<Object> channel, String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrange(channel, key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangeWithScores(String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangeWithScores(key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangeWithScores(ScoredValueStreamingChannel<Object> channel, String key, long start, long stop) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangeWithScores(channel, key, start, stop);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebylex(String key, Range<? extends Object> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebylex(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebylex(String key, Range<? extends Object> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebylex(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, double max, double min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, String max, String min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, double max, double min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, String max, String min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> zrevrangebyscore(String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, double max, double min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, String max, String min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, double max, double min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, String max, String min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscore(ValueStreamingChannel<Object> channel, String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscore(channel, key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, double max, double min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, String max, String min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, double max, double min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, String max, String min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<ScoredValue<Object>> zrevrangebyscoreWithScores(String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, double max, double min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, String max, String min) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, max, min);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, Range<? extends Number> range) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, range);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, double max, double min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, String max, String min, long offset, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, max, min, offset, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<Object> channel, String key, Range<? extends Number> range, Limit limit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrangebyscoreWithScores(channel, key, range, limit);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zrevrank(String key, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zrevrank(key, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ScoredValueScanCursor<Object> zscan(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ScoredValueScanCursor<Object> zscan(String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ScoredValueScanCursor<Object> zscan(String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ScoredValueScanCursor<Object> zscan(String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor zscan(ScoredValueStreamingChannel<Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor zscan(ScoredValueStreamingChannel<Object> channel, String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(channel, key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor zscan(ScoredValueStreamingChannel<Object> channel, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(channel, key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor zscan(ScoredValueStreamingChannel<Object> channel, String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscan(channel, key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Double zscore(String key, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zscore(key, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zunionstore(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zunionstore(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long zunionstore(String destination, ZStoreArgs storeArgs, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().zunionstore(destination, storeArgs, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public <T> T eval(String script, ScriptOutputType type, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().eval(script, type, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public <T> T eval(String script, ScriptOutputType type, String[] keys, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().eval(script, type, keys, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public <T> T evalsha(String digest, ScriptOutputType type, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().evalsha(digest, type, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public <T> T evalsha(String digest, ScriptOutputType type, String[] keys, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().evalsha(digest, type, keys, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Boolean> scriptExists(String... digests) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scriptExists(digests);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String scriptFlush() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scriptFlush();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String scriptKill() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scriptKill();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String scriptLoad(Object script) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scriptLoad(script);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String digest(Object script) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().digest(script);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String discard() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().discard();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public TransactionResult exec() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().exec();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String multi() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().multi();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String watch(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().watch(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String unwatch() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().unwatch();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sadd(String key, Object... members) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sadd(key, members);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long scard(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().scard(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Set<Object> sdiff(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sdiff(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sdiff(ValueStreamingChannel<Object> channel, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sdiff(channel, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sdiffstore(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sdiffstore(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Set<Object> sinter(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sinter(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sinter(ValueStreamingChannel<Object> channel, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sinter(channel, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sinterstore(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sinterstore(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Boolean sismember(String key, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sismember(key, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Boolean smove(String source, String destination, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().smove(source, destination, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Set<Object> smembers(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().smembers(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long smembers(ValueStreamingChannel<Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().smembers(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Object spop(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().spop(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Set<Object> spop(String key, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().spop(key, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Object srandmember(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().srandmember(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> srandmember(String key, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().srandmember(key, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long srandmember(ValueStreamingChannel<Object> channel, String key, long count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().srandmember(channel, key, count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long srem(String key, Object... members) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().srem(key, members);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Set<Object> sunion(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sunion(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sunion(ValueStreamingChannel<Object> channel, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sunion(channel, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long sunionstore(String destination, String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sunionstore(destination, keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ValueScanCursor<Object> sscan(String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ValueScanCursor<Object> sscan(String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ValueScanCursor<Object> sscan(String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public ValueScanCursor<Object> sscan(String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor sscan(ValueStreamingChannel<Object> channel, String key) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(channel, key);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor sscan(ValueStreamingChannel<Object> channel, String key, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(channel, key, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor sscan(ValueStreamingChannel<Object> channel, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(channel, key, scanCursor, scanArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public StreamScanCursor sscan(ValueStreamingChannel<Object> channel, String key, ScanCursor scanCursor) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().sscan(channel, key, scanCursor);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String bgrewriteaof() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bgrewriteaof();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String bgsave() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().bgsave();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String clientGetname() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientGetname();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String clientSetname(String name) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientSetname(name);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String clientKill(String addr) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientKill(addr);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long clientKill(KillArgs killArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientKill(killArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String clientPause(long timeout) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientPause(timeout);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String clientList() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().clientList();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> command() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().command();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> commandInfo(String... commands) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().commandInfo(commands);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> commandInfo(CommandType... commands) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().commandInfo(commands);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long commandCount() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().commandCount();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Map<String, String> configGet(String parameter) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().configGet(parameter);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String configResetstat() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().configResetstat();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String configRewrite() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().configRewrite();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String configSet(String parameter, String value) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().configSet(parameter, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long dbsize() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().dbsize();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String debugCrashAndRecover(Long delay) {
		throw new UnsupportedOperationException("single redis not supported debugCrashAndRecover yet!");
	}

	@Override
	public String debugHtstats(int db) {
		throw new UnsupportedOperationException("single redis not supported debugHtstats yet!");
	}

	@Override
	public String debugObject(String key) {
		throw new UnsupportedOperationException("single redis not supported debugObject yet!");
	}

	@Override
	public void debugOom() {
		throw new UnsupportedOperationException("single redis not supported debugOom yet!");
	}

	@Override
	public void debugSegfault() {
		throw new UnsupportedOperationException("single redis not supported debugSegfault yet!");
	}

	@Override
	public String debugReload() {
		throw new UnsupportedOperationException("single redis not supported debugReload yet!");
	}

	@Override
	public String debugRestart(Long delay) {
		throw new UnsupportedOperationException("single redis not supported debugRestart yet!");
	}

	@Override
	public String debugSdslen(String key) {
		throw new UnsupportedOperationException("single redis not supported debugSdslen yet!");
	}

	@Override
	public String flushall() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().flushall();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String flushallAsync() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().flushallAsync();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String flushdb() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().flushdb();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String flushdbAsync() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().flushdbAsync();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String info() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().info();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String info(String section) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().info(section);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Date lastsave() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().lastsave();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String save() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().save();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public void shutdown(boolean save) {
		throw new UnsupportedOperationException("single redis not supported shutdown yet!");
	}

	@Override
	public String slaveof(String host, int port) {
		throw new UnsupportedOperationException("single redis not supported slaveof yet!");
	}

	@Override
	public String slaveofNoOne() {
		throw new UnsupportedOperationException("single redis not supported slaveofNoOne yet!");
	}

	@Override
	public List<Object> slowlogGet() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().slowlogGet();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public List<Object> slowlogGet(int count) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().slowlogGet(count);
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Long slowlogLen() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().slowlogLen();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public String slowlogReset() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().slowlogReset();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> time() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().time();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long pfadd(String key, Object... values) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pfadd(key, values);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String pfmerge(String destkey, String... sourcekeys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pfmerge(destkey, sourcekeys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long pfcount(String... keys) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pfcount(keys);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long publish(String channel, Object message) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().publish(channel, message);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<String> pubsubChannels() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pubsubChannels();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<String> pubsubChannels(String channel) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pubsubChannels(channel);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Map<String, Long> pubsubNumsub(String... channels) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pubsubNumsub(channels);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long pubsubNumpat() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().pubsubNumpat();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object echo(Object msg) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().echo(msg);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Object> role() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().role();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String ping() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().ping();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String readOnly() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().readOnly();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String readWrite() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().readWrite();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public String quit() {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().quit();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long waitForReplication(int replicas, long timeout) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().waitForReplication(replicas, timeout);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public <T> T dispatch(ProtocolKeyword type, CommandOutput<String, Object, T> output) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().dispatch(type, output);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public <T> T dispatch(ProtocolKeyword type, CommandOutput<String, Object, T> output, CommandArgs<String, Object> args) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().dispatch(type, output, args);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException("single redis not support isOpen yet!");
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException("single redis not support reset yet!");
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, Object member) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().geoadd(key, longitude, latitude, member);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long geoadd(String key, Object... lngLatMember) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().geoadd(key, lngLatMember);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<Value<String>> geohash(String key, Object... members) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().geohash(key, members);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Set<Object> georadius(String key, double longitude, double latitude, double distance, Unit unit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadius(key, longitude, latitude, distance, unit);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<GeoWithin<Object>> georadius(String key, double longitude, double latitude, double distance, Unit unit, GeoArgs geoArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadius(key, longitude, latitude, distance, unit, geoArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long georadius(String key, double longitude, double latitude, double distance, Unit unit, GeoRadiusStoreArgs<String> geoRadiusStoreArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadius(key, longitude, latitude, distance, unit, geoRadiusStoreArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Set<Object> georadiusbymember(String key, Object member, double distance, Unit unit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadiusbymember(key, member, distance, unit);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<GeoWithin<Object>> georadiusbymember(String key, Object member, double distance, Unit unit, GeoArgs geoArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadiusbymember(key, member, distance, unit, geoArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Long georadiusbymember(String key, Object member, double distance, Unit unit, GeoRadiusStoreArgs<String> geoRadiusStoreArgs) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().georadiusbymember(key, member, distance, unit, geoRadiusStoreArgs);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public List<GeoCoordinates> geopos(String key, Object... members) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().geopos(key, members);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Double geodist(String key, Object from, Object to, Unit unit) {
		try (StatefulRedisConnection<String, Object> connection = pool.borrowObject()) {
			return connection.sync().geodist(key, from, to, unit);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

}
