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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.github.javaovo.cache.CacheProvider;
import io.github.javaovo.cache.RaeCacheChannel;

public class RaeCacheFactoryBean implements FactoryBean<RaeCacheChannel>, InitializingBean, DisposableBean, ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(RaeCacheFactoryBean.class);

	private String L1;
	private String L2;
	private String name;

	private ApplicationContext ctx = null;

	private RaeCacheChannel cacheChannel;

	@Override
	public void destroy() throws Exception {
		if (cacheChannel != null) {
			cacheChannel.destroy();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		CacheProvider L1_Cache = null;
		CacheProvider L2_Cache = null;
		if (ctx.containsBean(L1)) {
			try {
				L1_Cache = ctx.getBean(L1, CacheProvider.class);
			} catch (Exception e1) {
				log.warn("platform L1 cache init error!,ex:", e1);
			}
		} else {
			log.warn("platform L1 cache not exists! Ignore...");
		}

		if (ctx.containsBean(L2)) {
			try {
				L2_Cache = ctx.getBean(L2, CacheProvider.class);
			} catch (Exception e1) {
				log.warn("platform L2 cache init error!,ex:", e1);
			}
		} else {
			log.warn("platform L2 cache not exists! Ignore...");
		}

		try {
			cacheChannel = new RaeCacheChannel(L1_Cache, L2_Cache, name, false);
			log.info("Cache Platform init Success. L1 Cache {}, L2 Cache {}", L1, L2);
		} catch (Exception e) {
			log.warn("cache platform init error!,ex:", e);
		}
	}

	@Override
	public RaeCacheChannel getObject() throws Exception {
		return cacheChannel;
	}

	@Override
	public Class<?> getObjectType() {
		return RaeCacheChannel.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}

	public String getL1() {
		return L1;
	}

	public void setL1(String l1) {
		L1 = l1;
	}

	public String getL2() {
		return L2;
	}

	public void setL2(String l2) {
		L2 = l2;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
