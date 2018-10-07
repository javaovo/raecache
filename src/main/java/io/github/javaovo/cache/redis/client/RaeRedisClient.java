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
package io.github.javaovo.cache.redis.client;

import java.io.Closeable;

import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisGeoCommands;
import io.lettuce.core.api.sync.RedisHLLCommands;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisListCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.lettuce.core.api.sync.RedisSetCommands;
import io.lettuce.core.api.sync.RedisSortedSetCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.api.sync.RedisTransactionalCommands;

public interface RaeRedisClient<K, V> extends RedisHashCommands<K, V>, RedisKeyCommands<K, V>, RedisStringCommands<K, V>, RedisListCommands<K, V>, RedisSetCommands<K, V>,
		RedisSortedSetCommands<K, V>, RedisScriptingCommands<K, V>, RedisServerCommands<K, V>, RedisHLLCommands<K, V>, BaseRedisCommands<K, V>,
		RedisTransactionalCommands<K, V>, RedisGeoCommands<K, V>, Closeable {

	String getNs();
}
