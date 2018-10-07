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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.javaovo.cache.redis.client.ClusterRedisClient;

import io.lettuce.core.RedisURI;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:41:28
 * @Since: 1.0
 */
public class Redis4ClusterFactory implements Redis4ClientFactory<ClusterRedisClient> {

	private static final Logger log = LoggerFactory.getLogger(Redis4ClusterFactory.class);

	private ClusterRedisClient redisClient;
	private Redis4ClientConfig config;

	private int maxRedirections = 6;

	private Pattern p = Pattern.compile("^.+[:]\\d{1,5}\\s*$");

	private String namespace;

	public Redis4ClusterFactory(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public ClusterRedisClient getResource() {
		return redisClient;
	}

	@Override
	public void returnResource(ClusterRedisClient client) {

	}

	public void build() {
		try {
			Set<RedisURI> hostAndPorts = parseRedisURI(config.getPassword());
			if (maxRedirections < 6) {
				maxRedirections = hostAndPorts.size();
			}
			redisClient = new ClusterRedisClient(config, namespace, maxRedirections, hostAndPorts);
		} catch (Exception e) {
			log.error("redis cluster server init error.", e);
		}
	}

	private Set<RedisURI> parseRedisURI(String password) {
		String host = this.config.getHost();
		Set<RedisURI> haps = new HashSet<RedisURI>();
		String[] hosts = host.split(",");
		for (String val : hosts) {
			boolean isIpPort = p.matcher(val).matches();
			if (!isIpPort) {
				throw new IllegalArgumentException("ip or port is illegal.");
			}
			String[] ipAndPort = val.split(":");
			RedisURI node = RedisURI.create(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
			if (password != null && password.length() > 0) {
				node.setPassword(password);
			}
			haps.add(node);
		}
		return haps;
	}

	public void setMaxRedirections(int maxRedirections) {
		this.maxRedirections = maxRedirections;
	}

	public Redis4ClientConfig getConfig() {
		return config;
	}

	public void setConfig(Redis4ClientConfig config) {
		this.config = config;
	}

	@Override
	public void close() throws IOException {
		redisClient.close();
	}
}
