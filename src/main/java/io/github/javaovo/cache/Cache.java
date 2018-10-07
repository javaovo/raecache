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

public interface Cache {

	/**
	 * Get an item from the cache, nontransactionally
	 * 
	 * @param key cache key
	 * @return the cached object or null
	 */
	public Object get(Object key);

	public <T> T get(Object key, Class<T> type);

	public boolean exists(Object key);

	/**
	 * Add an item to the cache, nontransactionally, with failfast semantics
	 * 
	 * @param key   cache key
	 * @param value cache value
	 */
	public void put(Object key, Object value);

	/**
	 * Add an item to the cache, nontransactionally, with failfast semantics
	 * 
	 * @param key
	 * @param value
	 * @param expireInSec expire time. (seconds) @
	 */
	public void put(Object key, Object value, Integer expireInSec);

	/**
	 * Add an item to the cache and add expireInSec param.
	 * 
	 * @param key
	 * @param value
	 * @param expireInSec the number of seconds to idle
	 */
	public void putEx(Object key, Object value, Integer expireInSec);

	public void expireIn(Object key, Integer expireInSec);

	/**
	 * Add an item to the cache
	 * 
	 * @param key   cache key
	 * @param value cache value
	 */
	public void update(Object key, Object value);

	/**
	 * Add an item to the cache
	 * 
	 * @param key
	 * @param value
	 * @param expireInSec expire time. (seconds) @
	 */
	public void update(Object key, Object value, Integer expireInSec);

	@SuppressWarnings("rawtypes")
	public List keys();

	/**
	 * @param key Cache key Remove an item from the cache
	 */
	public void evict(Object key);

	/**
	 * Batch remove cache objects
	 * 
	 * @param keys the cache keys to be evicted
	 */
	@SuppressWarnings("rawtypes")
	public void evict(List keys);

	/**
	 * Clear the cache
	 */
	public void clear();

	/**
	 * Clean up
	 */
	public void destroy();

}
