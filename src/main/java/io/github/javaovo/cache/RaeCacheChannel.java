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
package io.github.javaovo.cache;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;

public class RaeCacheChannel extends AbstractValueAdaptingCache implements Cache {

	private final Logger logger = LoggerFactory.getLogger(RaeCacheChannel.class);

	private CacheProvider L1 = null;
	private CacheProvider L2 = null;
	private String cacheName = "raecache";

	public RaeCacheChannel(CacheProvider L1, CacheProvider L2, String name, boolean allowNullValues) {
		super(allowNullValues);
		this.L1 = L1;
		this.L2 = L2;

		if (name != null && name.length() > 0) {
			cacheName = name;
		}
	}

	@Override
	public String getName() {
		return cacheName;
	}

	@Override
	public Object getNativeCache() {
		if (L1 == null) {
			logger.warn("cache provider undefined!");
			return null;
		}
		Cache cache1 = L1.getCache();
		return cache1;
	}

	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		@SuppressWarnings("unchecked")
		T entity = (T) this.fromStoreValue(get(key).get());
		if (entity == null) {
			try {
				entity = valueLoader.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
			put(key, entity);
		}

		return entity;
	}

	@Override
	public void put(Object key, Object value) {
		if (L1 != null) {
			try {
				Cache cache1 = L1.getCache();
				cache1.put(key, value);
			} catch (CacheException e) {
				logger.warn("Put L1 cache fail.", e);
			}
		}
		if (L2 != null) {
			try {
				Cache cache2 = L2.getCache();
				cache2.put(key, value);
			} catch (CacheException e) {
				logger.warn("Put L2 cache fail.", e);
			}
		}
	}

	@Override
	public boolean exists(Object key) {
		if (L1.getCache().exists(key)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Key Exists L1 Cache.", key);
			}
			return true;
		}
		if (L2.getCache().exists(key)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Key Exists L2 Cache.", key);
			}
			return true;
		}
		return false;
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		if (L1 == null) {
			logger.warn("cache provider undefined!");
			return null;
		}
		Cache cache1 = L1.getCache();
		cache1.put(key, value);

		if (L2 != null) {
			Cache cache2 = L2.getCache();
			cache2.put(key, value);
		}

		return get(key);
	}

	@Override
	public void evict(Object key) {
		if (L1 != null) {
			try {
				Cache cache1 = L1.getCache();
				cache1.evict(key);
			} catch (CacheException e) {
				logger.warn("Evict L1 cache key fail.", e);
			}
		}
		if (L2 != null) {
			try {
				Cache cache2 = L2.getCache();
				cache2.evict(key);
			} catch (CacheException e) {
				logger.warn("Evict L2 cache key fail.", e);
			}
		}
	}

	@Override
	public void clear() {
		if (L1 == null) {
			logger.warn("cache provider undefined!");
			return;
		}
		Cache cache1 = L1.getCache();
		cache1.clear();
	}

	@Override
	protected Object lookup(Object key) {
		Object obj = null;
		if (L1 == null) {
			logger.warn("cache provider undefined!");
			return obj;
		} else {
			Cache cache1 = L1.getCache();
			obj = cache1.get(key);
			if (logger.isDebugEnabled()) {
				logger.debug("obtained from the L1 cache：{}", obj);
			}
			if (obj == null && L2 != null) {
				Cache cache2 = L2.getCache();
				obj = cache2.get(key);
				if (obj != null) {
					cache1.put(key, obj);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("obtained from the L2 cache：{}", obj);
				}
			}
		}
		return obj;
	}

	@Override
	public void put(Object key, Object value, Integer expireInSec) {
		if (L1 != null && L1.supportExpired()) {
			try {
				Cache cache1 = L1.getCache();
				cache1.put(key, value, expireInSec);
			} catch (CacheException e) {
				logger.warn("put L1 cache fail.", e);
			}
		}
		if (L2 != null && L2.supportExpired()) {
			try {
				Cache cache2 = L2.getCache();
				cache2.put(key, value, expireInSec);
			} catch (CacheException e) {
				logger.warn("put L2 cache fail.", e);
			}
		}

	}

	@Override
	public void update(Object key, Object value) {
		put(key, value);
	}

	@Override
	public void update(Object key, Object value, Integer expireInSec) {
		put(key, value, expireInSec);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List keys() {
		return null;
	}

	@Override
	public void evict(@SuppressWarnings("rawtypes") List keys) {
		if (L1 != null) {
			try {
				Cache cache1 = L1.getCache();
				cache1.evict(keys);
			} catch (CacheException e) {
				logger.warn("evict L1 cache key fail.", e);
			}
		}
		if (L2 != null) {
			try {
				Cache cache2 = L2.getCache();
				cache2.evict(keys);
			} catch (CacheException e) {
				logger.warn("evict L2 cache key fail.", e);
			}
		}
	}

	@Override
	public void destroy() {
		if (L1 != null) {
			L1.destory();
		}
		if (L2 != null) {
			L2.destory();
		}
	}

	@Override
	public void putEx(Object key, Object value, Integer expireInSec) {
		if (L1 != null && L1.supportExpired()) {
			try {
				Cache cache1 = L1.getCache();
				cache1.putEx(key, value, expireInSec);
			} catch (CacheException e) {
				logger.warn("putEx L1 cache fail.", e);
			}
		}
		if (L2 != null && L2.supportExpired()) {
			try {
				Cache cache2 = L2.getCache();
				cache2.putEx(key, value, expireInSec);
			} catch (CacheException e) {
				logger.warn("putEx L2 cache fail.", e);
			}
		}

	}

	@Override
	public void expireIn(Object key, Integer expireInSec) {
		if (L1 != null && L1.supportExpired()) {
			Cache cache1 = L1.getCache();
			cache1.expireIn(key, expireInSec);
		}
		if (L2 != null && L2.supportExpired()) {
			Cache cache2 = L2.getCache();
			cache2.expireIn(key, expireInSec);
		}
	}
}
