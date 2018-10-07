
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
import io.github.javaovo.cache.ehcache.EhCacheProvider;

public class EhCacheFactoryBean implements FactoryBean<CacheProvider>, InitializingBean, DisposableBean {

	private EhCacheProvider ehCacheProvider;

	private String configXml;

	private String cacheName;

	@Override
	public void destroy() throws Exception {
		ehCacheProvider.destory();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ehCacheProvider = new EhCacheProvider();

		Map<String, Object> map = new HashMap<String, Object>();
		if (configXml != null) {
			map.put("configXml", configXml);
		}
		if (cacheName != null) {
			map.put("cacheName", cacheName);
		}
		ehCacheProvider.init(map);
	}

	@Override
	public CacheProvider getObject() throws Exception {
		return ehCacheProvider;
	}

	@Override
	public Class<?> getObjectType() {
		return EhCacheProvider.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public EhCacheProvider getEhCacheProvider() {
		return ehCacheProvider;
	}

	public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
		this.ehCacheProvider = ehCacheProvider;
	}

	public String getConfigXml() {
		return configXml;
	}

	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
}
