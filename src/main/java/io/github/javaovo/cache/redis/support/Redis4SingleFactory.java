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
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.redis.client.RaeRedisClient;
import io.github.javaovo.cache.redis.client.SingleRedisClient;
import io.lettuce.core.RedisURI;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:41:59
 * @Since: 1.0
 */
public class Redis4SingleFactory implements Redis4ClientFactory<RaeRedisClient<String, Object>> {

	private static final Logger log = LoggerFactory.getLogger(Redis4SingleFactory.class);
	private Pattern p = Pattern.compile("^.+[:]\\d{1,5}\\s*$");

	private RaeRedisClient<String, Object> redisClient;
	private Redis4ClientConfig config;

	private String namespace;

	public Redis4SingleFactory(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public RaeRedisClient<String, Object> getResource() {
		return redisClient;
	}

	@Override
	public void returnResource(RaeRedisClient<String, Object> client) {

	}

	public void build() {
		try {
			redisClient = new SingleRedisClient(config, parseRedisURI(config.getPassword()), namespace);
		} catch (Exception e) {
			log.error("redis single server init error.", e);
		}
	}

	private RedisURI parseRedisURI(String password) {
		String host = config.getHost();
		boolean isIpPort = p.matcher(host).matches();
		if (!isIpPort) {
			throw new IllegalArgumentException("ip or port is illegal.");
		}
		String[] ipAndPort = host.split(":");
		RedisURI node = RedisURI.create(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
		node.setDatabase(config.getDatabase());
		node.setTimeout(Duration.ofNanos(TimeUnit.MILLISECONDS.toNanos(config.getTimeout())));
		if (password != null && password.length() > 0) {
			node.setPassword(password);
		}
		return node;
	}

	@Override
	public void close() throws IOException {
		redisClient.close();
	}

	public Redis4ClientConfig getConfig() {
		return config;
	}

	public void setConfig(Redis4ClientConfig config) {
		this.config = config;
	}

}
