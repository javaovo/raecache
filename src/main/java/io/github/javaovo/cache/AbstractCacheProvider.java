package io.github.javaovo.cache;

import java.util.List;

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
