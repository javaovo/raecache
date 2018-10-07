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
package io.github.javaovo.cache.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.github.javaovo.cache.CacheProvider;
import io.github.javaovo.cache.redis.RedisCacheProvider;

public class RedisFactoryBean implements FactoryBean<CacheProvider>, InitializingBean, DisposableBean {

	private RedisCacheProvider redisCacheProvider;
	private String host;
	private String ns;
	private String password;
	private String compress;
	private int maxTotal = 30;
	private int maxIdle = 10;
	private int minIdle = 5;
	private int timeBetweenEvictionRunsMillis = 20000;
	private int minEvictableIdleTimeMillis = 50000;
	private int numTestsPerEvictionRun = -1;
	private boolean testOnBorrow = false;
	private boolean testOnReturn = false;
	private boolean testWhileIdle = true;

	@Override
	public void destroy() throws Exception {
		if (redisCacheProvider != null) {
			redisCacheProvider.destory();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("host", host);
		map.put("ns", ns);
		map.put("password", password);
		map.put("compress", compress);
		map.put("maxTotal", maxTotal);
		map.put("maxIdle", maxIdle);
		map.put("minIdle", minIdle);
		map.put("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
		map.put("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);
		map.put("numTestsPerEvictionRun", numTestsPerEvictionRun);
		map.put("testOnBorrow", testOnBorrow);
		map.put("testOnReturn", testOnReturn);
		map.put("testWhileIdle", testWhileIdle);

		redisCacheProvider = new RedisCacheProvider();
		redisCacheProvider.init(map);
	}

	@Override
	public CacheProvider getObject() throws Exception {
		return redisCacheProvider;
	}

	@Override
	public Class<?> getObjectType() {
		return RedisCacheProvider.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public RedisCacheProvider getRedisCacheProvider() {
		return redisCacheProvider;
	}

	public void setRedisCacheProvider(RedisCacheProvider redisCacheProvider) {
		this.redisCacheProvider = redisCacheProvider;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getNs() {
		return ns;
	}

	public void setNs(String ns) {
		this.ns = ns;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public int getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public int getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getCompress() {
		return compress;
	}

	public void setCompress(String compress) {
		this.compress = compress;
	}
}
