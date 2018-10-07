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
import java.util.Map;

public interface CacheProvider {

	public String name();

	public String getNs();

	public boolean supportExpired();

	public void registerExpired(List<CacheProvider> otherCacheList);

	public Cache getCache() throws CacheException;

	public Cache getCache(String cacheName) throws CacheException;

	public void init(Map<String, Object> map) throws CacheException;

	public void destory();

}
