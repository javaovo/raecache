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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.AbstractCacheProvider;
import io.github.javaovo.cache.Cache;
import io.github.javaovo.cache.CacheException;
import io.github.javaovo.cache.CacheProvider;
import io.github.javaovo.cache.redis.client.RaeRedisClient;
import io.github.javaovo.cache.redis.support.Redis4ClientConfig;
import io.github.javaovo.cache.redis.support.Redis4ClientFactoryAdapter;
import io.github.javaovo.cache.redis.support.Redis4ClientFactoryAdapter.Redis2Policy;
import io.github.javaovo.cache.util.MapUtils;

public class RedisCacheProvider extends AbstractCacheProvider {
	private final Logger logger = LoggerFactory.getLogger(RedisCacheProvider.class);

	private Redis4ClientFactoryAdapter redis4ClientFactoryAdapter;

	private String ns = "";

	private RedisCache cache = null;

	public String name() {
		return "redis4";
	}

	@Override
	public String getNs() {
		return ns;
	}

	public RaeRedisClient<String, Object> getResource() {
		RaeRedisClient<String, Object> client = null;
		try {
			client = redis4ClientFactoryAdapter.getRedisClientFactory().getResource();
			if (client == null) {
			}
		} catch (Exception e) {
			logger.error("get redis4 resource error.", e);
		}
		return client;
	}

	@Override
	public Cache getCache() throws CacheException {
		if (cache == null) {
			cache = new RedisCache(ns, getResource());
		}
		return cache;
	}

	@Override
	public Cache getCache(String cacheName) throws CacheException {
		return getCache();
	}

	@Override
	public void init(Map<String, Object> map) throws CacheException {
		Redis4ClientConfig config = new Redis4ClientConfig();

		config.setHost(MapUtils.getString(map, "host", "127.0.0.1"));
		config.setPassword(MapUtils.getString(map, "password"));
		config.setCompress(MapUtils.getString(map, "compress", "").toUpperCase());
		config.setTimeout(MapUtils.getIntValue(map, "timeout", 20000));
		config.setMaxTotal(MapUtils.getIntValue(map, "maxTotal", 50));
		config.setMaxIdle(MapUtils.getIntValue(map, "maxIdle", 10));
		config.setMinIdle(MapUtils.getIntValue(map, "minIdle", 5));
		config.setTimeBetweenEvictionRunsMillis(MapUtils.getIntValue(map, "timeBetweenEvictionRunsMillis", 30000));
		config.setMinEvictableIdleTimeMillis(MapUtils.getIntValue(map, "minEvictableIdleTimeMillis", 60000));
		config.setNumTestsPerEvictionRun(MapUtils.getIntValue(map, "numTestsPerEvictionRun", -1));
		config.setTestOnBorrow(MapUtils.getBooleanValue(map, "testOnBorrow", false));
		config.setTestOnReturn(MapUtils.getBooleanValue(map, "testOnReturn", false));
		config.setTestWhileIdle(MapUtils.getBooleanValue(map, "testWhileIdle", true));

		Redis2Policy policy = Redis2Policy.cluster;
		if (config.getHost().indexOf(",") == -1) {
			config.setDatabase(MapUtils.getIntValue(map, "database", 0));
			String[] h = config.getHost().split(":");
			config.setPort(Integer.valueOf(h[1]));
			policy = Redis2Policy.single;
		} else if (config.getHost().startsWith("redis-sentinel://")) {
			policy = Redis2Policy.sentinel;
		}
		ns = MapUtils.getString(map, "ns", "");

		logger.info("initialize redis cache,policy:{}, ns:{}", policy, ns);
		redis4ClientFactoryAdapter = new Redis4ClientFactoryAdapter(config, policy, ns);
	}

	@Override
	public void destory() {
		redis4ClientFactoryAdapter.close();
	}

	@Override
	public boolean supportExpired() {
		return true;
	}

	@Override
	public void registerExpired(List<CacheProvider> otherProviderList) {
		
	}
}
