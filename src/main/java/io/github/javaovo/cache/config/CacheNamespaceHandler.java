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
package io.github.javaovo.cache.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:37:57
 * @Since: 1.0
 */
class CacheNamespaceHandler extends NamespaceHandlerSupport {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	@Override
	public void init() {
		registerBeanDefinitionParser("redis", new RedisBeanDefinitionParser());
		registerBeanDefinitionParser("ehcache", new EhcacheBeanDefinitionParser());
		registerBeanDefinitionParser("channel", new CacheChannelBeanDefinitionParser());
	}
}
