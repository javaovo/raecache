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

public class Redis4ClientFactoryAdapter {

	private Redis4ClientFactory<?> redis4ClientFactory;

	private Redis4ClientConfig config;
	private Redis2Policy policy = Redis2Policy.single; // 缓存策略，single:单机,sentinel:哨兵,cluster:集群

	private String namepsace;

	public Redis4ClientFactoryAdapter(Redis4ClientConfig config, Redis2Policy policy, String namespace) {
		if (policy != null) {
			this.policy = policy;
		}
		this.config = config;
		initRedisFactory();
		this.namepsace = namespace;
	}

	private void initRedisFactory() {
		switch (getPolicy()) {
		case single:
			initSingleRedis();
			break;
		case sentinel:
			initSentinelRedis();
			break;
		case cluster:
			initClusterRedis();
			break;
		default:
			initSingleRedis();
		}
	}

	private void initSingleRedis() {
		Redis4SingleFactory factory = new Redis4SingleFactory(namepsace);
		factory.setConfig(config);
		factory.build();
		this.setRedisClientFactory(factory);
	}

	private void initSentinelRedis() {
		Redis4SentinelFactory factory = new Redis4SentinelFactory(namepsace);
		factory.setConfig(config);
		factory.build();
		this.setRedisClientFactory(factory);
	}

	private void initClusterRedis() {
		Redis4ClusterFactory factory = new Redis4ClusterFactory(namepsace);
		factory.setConfig(config);
		factory.build();
		this.setRedisClientFactory(factory);
	}

	public void setHost(String host) {
		config.setHost(host);
	}

	public void setPort(int port) {
		config.setPort(port);
	}

	public void setPassword(String password) {
		config.setPassword(password);
	}

	public void setTimeout(int timeout) {
		config.setTimeout(timeout);
	}

	public int getMaxTotal() {
		return config.getMaxTotal();
	}

	public void setMaxTotal(int maxTotal) {
		config.setMaxTotal(maxTotal);
	}

	public int getMaxIdle() {
		return config.getMaxIdle();
	}

	public void setMaxIdle(int maxIdle) {
		config.setMaxIdle(maxIdle);
	}

	public int getMinIdle() {
		return config.getMinIdle();
	}

	public void setMinIdle(int minIdle) {
		config.setMinIdle(minIdle);
	}

	public boolean getLifo() {
		return config.getLifo();
	}

	public boolean getFairness() {
		return config.getFairness();
	}

	public void setLifo(boolean lifo) {
		config.setLifo(lifo);
	}

	public void setFairness(boolean fairness) {
		config.setFairness(fairness);
	}

	public long getMaxWaitMillis() {
		return config.getMaxWaitMillis();
	}

	public void setMaxWaitMillis(long maxWaitMillis) {
		config.setMaxWaitMillis(maxWaitMillis);
	}

	public long getMinEvictableIdleTimeMillis() {
		return config.getMinEvictableIdleTimeMillis();
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	public long getSoftMinEvictableIdleTimeMillis() {
		return config.getSoftMinEvictableIdleTimeMillis();
	}

	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
	}

	public int getNumTestsPerEvictionRun() {
		return config.getNumTestsPerEvictionRun();
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	public boolean getTestOnCreate() {
		return config.getTestOnCreate();
	}

	public void setTestOnCreate(boolean testOnCreate) {
		config.setTestOnCreate(testOnCreate);
	}

	public boolean getTestOnBorrow() {
		return config.getTestOnBorrow();
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		config.setTestOnBorrow(testOnBorrow);
	}

	public boolean getTestOnReturn() {
		return config.getTestOnReturn();
	}

	public void setTestOnReturn(boolean testOnReturn) {
		config.setTestOnReturn(testOnReturn);
	}

	public boolean getTestWhileIdle() {
		return config.getTestWhileIdle();
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		config.setTestWhileIdle(testWhileIdle);
	}

	public long getTimeBetweenEvictionRunsMillis() {
		return config.getTimeBetweenEvictionRunsMillis();
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	public String getEvictionPolicyClassName() {
		return config.getEvictionPolicyClassName();
	}

	public void setEvictionPolicyClassName(String evictionPolicyClassName) {
		config.setEvictionPolicyClassName(evictionPolicyClassName);
	}

	public boolean getBlockWhenExhausted() {
		return config.getBlockWhenExhausted();
	}

	public void setBlockWhenExhausted(boolean blockWhenExhausted) {
		config.setBlockWhenExhausted(blockWhenExhausted);
	}

	public boolean getJmxEnabled() {
		return config.getJmxEnabled();
	}

	public void setJmxEnabled(boolean jmxEnabled) {
		config.setJmxEnabled(jmxEnabled);
	}

	public String getJmxNameBase() {
		return config.getJmxNameBase();
	}

	public void setJmxNameBase(String jmxNameBase) {
		config.setJmxNameBase(jmxNameBase);
	}

	public String getJmxNamePrefix() {
		return config.getJmxNamePrefix();
	}

	public void setJmxNamePrefix(String jmxNamePrefix) {
		config.setJmxNamePrefix(jmxNamePrefix);
	}

	public Redis2Policy getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = Redis2Policy.format(policy);
	}

	public void setRedisClientFactory(Redis4ClientFactory<?> redis4ClientFactory) {
		this.redis4ClientFactory = redis4ClientFactory;
	}

	public Redis4ClientFactory<?> getRedisClientFactory() {
		return redis4ClientFactory;
	}

	public void close() {
		try {
			getRedisClientFactory().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * redis使用策略
	 */
	public enum Redis2Policy {
		single, // 单机
		sentinel, // 哨兵
		cluster;// 集群

		Redis2Policy() {
		}

		public static Redis2Policy format(String policy) {
			switch (policy) {
			case "single":
				return single;
			case "sentinel":
				return sentinel;
			case "cluster":
				return cluster;
			default:
				return single;
			}
		}
	}
}
