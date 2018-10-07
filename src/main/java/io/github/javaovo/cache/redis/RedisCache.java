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
package io.github.javaovo.cache.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.Cache;
import io.github.javaovo.cache.CacheException;
import io.github.javaovo.cache.redis.client.RaeRedisClient;

public class RedisCache implements Cache {

	private final static Logger log = LoggerFactory.getLogger(RedisCache.class);

	protected RaeRedisClient<String, Object> client;
	private String namespace;

	public RedisCache(String namespace, RaeRedisClient<String, Object> client) {
		this.namespace = namespace;
		this.client = client;
	}

	/**
	 * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰 同时满足小型应用,多个J2Cache共享一个redis database的场景
	 *
	 * @param region
	 * @return
	 */
	public String getKeyName(Object key) {
		if (key != null && key.toString().startsWith(namespace)) {
			return "" + key;
		}

		if (namespace != null && !namespace.isEmpty()) {
			return (namespace + ":" + key);
		}
		return "" + key;
	}

	public Object get(Object key) throws CacheException {
		if (null == key) {
			return null;
		}
		return client.get(getKeyName(key));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Class<T> type) {
		Object value = get(key);
		if (value != null && type != null && !type.isInstance(value)) {
			throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
		}
		return (T) value;
	}

	public void evict(Object key) throws CacheException {
		if (key == null) {
			return;
		}
		try {
			long result = client.del(getKeyName(key));
			if (log.isDebugEnabled()) {
				log.debug("evict redis {} result:{}", getKeyName(key), result);
			}
		} catch (Exception e) {
			log.error("evict redis " + getKeyName(key) + " cache error.", e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException {
		if (keys == null || keys.size() == 0) {
			return;
		}

		try {
			int size = keys.size();
			String[] evictKeys = new String[keys.size()];
			for (int i = 0; i < size; i++) {
				evictKeys[i] = getKeyName(keys.get(i));
			}
			client.del(evictKeys);
		} catch (Exception e) {
			log.error("evict redis " + keys + " cache error.", e);
		}
	}

	public List<String> keys() throws CacheException {
		return null;
	}

	public void clear() throws CacheException {

	}

	public void destroy() throws CacheException {
		this.clear();
	}

	public void put(Object key, Object value) throws CacheException {
		put(key, value, null);
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
	}

	@Override
	public void put(Object key, Object value, Integer expireInSec) throws CacheException {
		if (key == null) {
			return;
		}
		if (value == null) {
			evict(key);
		} else {
			try {
				String byteKey = getKeyName(key);
				if (expireInSec != null && expireInSec > 0) {
					client.setex(byteKey, expireInSec, value);
				} else {
					client.set(byteKey, value);
				}
			} catch (Exception e) {
				log.error("put redis " + getKeyName(key) + " cache error.", e);
			}
		}
	}

	@Override
	public void update(Object key, Object value, Integer expireInSec) throws CacheException {
		put(key, value, expireInSec);
	}

	@Override
	public boolean exists(Object key) {
		try {
			return client.exists(getKeyName(key)) > 0;
		} catch (Exception e) {
			log.error("invoke redis exists " + getKeyName(key) + " method error.", e);
		}
		return false;
	}

	@Override
	public void putEx(Object key, Object value, Integer seconds) {
		if (seconds == null || seconds < 1) {
			throw new CacheException("redis cache putEx seconds must be great than 1.");
		}
		put(key, value, seconds);
	}

	@Override
	public void expireIn(Object key, Integer seconds) {
		if (!exists(key)) {
			log.warn("renewal redis " + getKeyName(key) + " failure. cache value is not exists.");
			return;
		}
		try {
			String byteKey = getKeyName(key);
			client.expire(byteKey, seconds);
		} catch (Exception e) {
			log.error("renewal redis " + getKeyName(key) + " cache error.", e);
		}
	}

	public Long incrBy(Object key, Long val) {
		try {
			return client.incrby(getKeyName(key), val);
		} catch (Exception e) {
			log.error("redis incrBy " + getKeyName(key) + " method error.", e);
		}
		return 0l;
	}
}
