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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.AbstractCacheProvider;
import io.github.javaovo.cache.Cache;
import io.github.javaovo.cache.CacheException;
import io.github.javaovo.cache.CacheProvider;
import io.github.javaovo.cache.util.MapUtils;
import net.sf.ehcache.CacheManager;

public class EhCacheProvider extends AbstractCacheProvider {

	private final static Logger log = LoggerFactory.getLogger(EhCacheProvider.class);

	private static final String DEFAILT_CACHE_NAME = "defaultCache";
	private static final String DEFAULT_CACHE_XML = "/io/github/javaovo/cache/config/default_ehcache.xml";

	private CacheManager manager;

	private String cacheName = null;

	private EhCache ehcache = null;

	@Override
	public String name() {
		return "ehcache";
	}

	@Override
	public String getNs() {
		return " ";
	}

	/**
	 * Builds a Cache. Even though this method provides properties, they are not
	 * used. Properties for EHCache are specified in the ehcache.xml file.
	 * Configuration will be read from ehcache.xml for a cache declaration where the
	 * name attribute matches the name parameter in this builder.
	 *
	 * @param name the name of the cache. Must match a cache configured in
	 *             ehcache.xml
	 * @throws CacheException inter alia, if a cache of the same name already exists
	 */
	@Override
	public Cache getCache() throws CacheException {
		if (ehcache == null) {
			try {
				net.sf.ehcache.Cache cache = manager.getCache(cacheName);
				if (cache == null) {
					log.warn("Could not find configuration [" + cacheName + "]; using defaults.");
					manager.addCache(cacheName);
					cache = manager.getCache(cacheName);
					if (log.isDebugEnabled()) {
						log.debug("started EhCache region: " + cacheName);
					}
				}
				ehcache = new EhCache(cache, null);
			} catch (net.sf.ehcache.CacheException e) {
				throw new CacheException(e);
			}
		}
		return ehcache;
	}

	@Override
	public Cache getCache(String name) throws CacheException {
		try {
			net.sf.ehcache.Cache cache = manager.getCache(name);
			if (cache == null) {
				log.warn("Could not find configuration [" + cacheName + "]; using defaults.");
				if (ehcache == null) {
					return getCache();
				}
				return ehcache;
			} else {
				manager.addCache(cacheName);
				cache = manager.getCache(cacheName);
				if (log.isDebugEnabled()) {
					log.debug("started EhCache region: " + cacheName);
				}
				return new EhCache(cache, null);
			}
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	public Cache getResource() {
		return getCache();
	}

	/**
	 * Callback to perform any necessary initialization of the underlying cache
	 * implementation during SessionFactory construction.
	 *
	 * @param props current configuration settings.
	 */
	@Override
	public void init(Map<String, Object> map) throws CacheException {
		if (manager != null) {
			log.warn("Attempt to restart an already started EhCacheProvider.");
			return;
		}

		String confXml = MapUtils.getString(map, "configXml", DEFAULT_CACHE_XML);
		cacheName = MapUtils.getString(map, "cacheName", DEFAILT_CACHE_NAME);
		log.info("RAECache ready to initialize... configXml:{}, cacheName:{}", confXml, cacheName);
		if (manager == null) {
			try {
				manager = new CacheManager(EhCacheProvider.class.getResourceAsStream(confXml));
			} catch (Exception e) {
				log.warn("RAECache initialization failed, retry by default conf.ex:{}", e);
				manager = CacheManager.getInstance();
			}
		}
	}

	/**
	 * Callback to perform any necessary cleanup of the underlying cache
	 * implementation.
	 */
	@Override
	public void destory() {
		if (manager != null) {
			manager.shutdown();
			manager = null;
		}
	}

	@Override
	public boolean supportExpired() {
		return true;
	}

	@Override
	public void registerExpired(List<CacheProvider> otherCacheList) {

	}
}
