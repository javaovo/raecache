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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.redis.client.SentinelRedisClient;

import io.lettuce.core.RedisURI;

public class Redis4SentinelFactory implements Redis4ClientFactory<SentinelRedisClient> {

	private static final Logger log = LoggerFactory.getLogger(Redis4SentinelFactory.class);

	private SentinelRedisClient redisClient;
	private Redis4ClientConfig config;

	private String namespace;

	public Redis4SentinelFactory(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public SentinelRedisClient getResource() {
		return redisClient;
	}

	@Override
	public void returnResource(SentinelRedisClient client) {

	}

	public void build() {
		try {
			redisClient = new SentinelRedisClient(config, parseRedisURI(config.getPassword()), namespace);
		} catch (Exception e) {
			log.error("redis sentinel server init error.", e);
		}
	}

	private RedisURI parseRedisURI(String password) {
		String host = config.getHost();
		RedisURI node = RedisURI.create(host);
		if (password != null && password.length() > 0) {
			node.setPassword(password);
		}
		return node;
	}

	public Redis4ClientConfig getConfig() {
		return config;
	}

	public void setConfig(Redis4ClientConfig config) {
		this.config = config;
	}

	/**
	 * Closes this stream and releases any system resources associated with it. If
	 * the stream is already closed then invoking this method has no effect.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		redisClient.close();
	}
}
