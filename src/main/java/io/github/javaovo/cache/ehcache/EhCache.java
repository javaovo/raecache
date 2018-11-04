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
package io.github.javaovo.cache.ehcache;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.Cache;
import io.github.javaovo.cache.CacheException;
import io.github.javaovo.cache.CacheExpiredListener;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:38:54
 * @Since: 1.0
 */
public class EhCache implements Cache, CacheEventListener {

	private final static Logger log = LoggerFactory.getLogger(EhCache.class);

	private net.sf.ehcache.Cache cache;

	public EhCache(net.sf.ehcache.Cache cache, CacheExpiredListener listener) {
		this.cache = cache;
		if (listener != null) {
			this.cache.getCacheEventNotificationService().registerListener(this);
		}
	}

	@SuppressWarnings("rawtypes")
	public List keys() throws CacheException {
		return this.cache.getKeys();
	}

	public Object get(Object key) throws CacheException {
		try {
			if (key == null)
				return null;
			else {
				Element element = cache.get(key);
				if (element != null)
					return element.getObjectValue();
			}
			return null;
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		Object value = get(key);
		if (value != null && type != null && !type.isInstance(value)) {
			throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
		}
		return (T) value;
	}

	public void update(Object key, Object value) throws CacheException {
		put(key, value);
	}

	public void put(Object key, Object value) throws CacheException {
		put(key, value, null);
	}

	@Override
	public void evict(Object key) throws CacheException {
		try {
			cache.remove(key);
		} catch (IllegalStateException e) {
			throw new CacheException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void evict(List keys) throws CacheException {
		cache.removeAll(keys);
	}

	public void clear() throws CacheException {
		try {
			cache.removeAll();
		} catch (IllegalStateException e) {
			throw new CacheException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	public void destroy() throws CacheException {
		try {
			cache.getCacheManager().removeCache(cache.getName());
		} catch (IllegalStateException e) {
			throw new CacheException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element elem) {

	}

	@Override
	public void notifyElementPut(Ehcache arg0, Element arg1) throws net.sf.ehcache.CacheException {
	}

	@Override
	public void notifyElementRemoved(Ehcache arg0, Element arg1) throws net.sf.ehcache.CacheException {
	}

	@Override
	public void notifyElementUpdated(Ehcache arg0, Element arg1) throws net.sf.ehcache.CacheException {
	}

	@Override
	public void notifyRemoveAll(Ehcache arg0) {
	}

	@Override
	public void put(Object key, Object value, Integer expireInSec) throws CacheException {
		try {
			Element element = new Element(key, value);
			if (expireInSec != null && expireInSec > 0) {
				element.setTimeToLive(expireInSec);
			}
			cache.put(element);
		} catch (IllegalArgumentException e) {
			throw new CacheException(e);
		} catch (IllegalStateException e) {
			throw new CacheException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void update(Object key, Object value, Integer expireInSec) throws CacheException {
		put(key, value, expireInSec);
	}

	@Override
	public boolean exists(Object key) {
		if (cache.get(key) != null) {
			return true;
		}
		return false;
	}

	@Override
	public void putEx(Object key, Object value, Integer timeToLiveSeconds) throws CacheException {
		if (timeToLiveSeconds == null || timeToLiveSeconds < 1) {
			throw new CacheException("ehcache putEx timeToLiveSeconds must be great than 1.");
		}
		put(key, value, timeToLiveSeconds);
	}

	@Override
	public void expireIn(Object key, Integer timeToLiveSeconds) throws CacheException {
		try {
			Element ele = cache.get(key);
			if (ele == null) {
				log.warn("renewal ehcache " + key + " failure. cache value is not exists.");
				return;
			}
			ele.setTimeToLive(timeToLiveSeconds);
			cache.put(ele);
		} catch (IllegalArgumentException e) {
			throw new CacheException(e);
		} catch (IllegalStateException e) {
			throw new CacheException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}
}