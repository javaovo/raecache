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
package io.github.javaovo.cache;

import java.util.List;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:42:24
 * @Since: 1.0
 */
public abstract class AbstractCacheProvider implements CacheProvider, Cache {

	@Override
	public Object get(Object key) {
		return getCache().get(key);
	}

	@Override
	public <T> T get(Object key, Class<T> type) {
		return getCache().get(key, type);
	}

	@Override
	public boolean exists(Object key) {
		return getCache().exists(key);
	}

	@Override
	public void put(Object key, Object value) {
		getCache().put(key, value);
	}

	@Override
	public void put(Object key, Object value, Integer expireInSec) {
		getCache().put(key, value, expireInSec);
	}

	@Override
	public void putEx(Object key, Object value, Integer expireInSec) {
		getCache().putEx(key, value, expireInSec);
	}

	@Override
	public void expireIn(Object key, Integer expireInSec) {
		getCache().expireIn(key, expireInSec);
	}

	@Override
	public void update(Object key, Object value) {
		getCache().update(key, value);
	}

	@Override
	public void update(Object key, Object value, Integer expireInSec) {
		getCache().update(key, value, expireInSec);
	}

	@Override
	public List keys() {
		return getCache().keys();
	}

	@Override
	public void evict(Object key) {
		getCache().evict(key);
	}

	@Override
	public void evict(List keys) {
		getCache().evict(keys);
	}

	@Override
	public void clear() {
		getCache().clear();
	}

	@Override
	public void destroy() {
		getCache().destroy();
	}
}
