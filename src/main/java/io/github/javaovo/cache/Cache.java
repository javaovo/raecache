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
 * @Date: 2018-10-07 21:42:43
 * @Since: 1.0
 */
public interface Cache {

	public Object get(Object key);

	public <T> T get(Object key, Class<T> type);

	public boolean exists(Object key);

	public void put(Object key, Object value);

	public void put(Object key, Object value, Integer expireInSec);

	public void putEx(Object key, Object value, Integer expireInSec);

	public void expireIn(Object key, Integer expireInSec);

	public void update(Object key, Object value);

	public void update(Object key, Object value, Integer expireInSec);

	@SuppressWarnings("rawtypes")
	public List keys();

	public void evict(Object key);

	@SuppressWarnings("rawtypes")
	public void evict(List keys);

	public void clear();

	public void destroy();
}
