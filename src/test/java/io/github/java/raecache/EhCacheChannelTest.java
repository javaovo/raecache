package io.github.java.raecache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import io.github.javaovo.cache.ehcache.EhCacheProvider;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:45:16
 * @Since: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class EhCacheChannelTest {

	@Autowired
	private EhCacheProvider ehCacheProvider;

	@Before
	public void put() {
		ehCacheProvider.put("hello", "ehcache cache");
	}

	@Test
	public void get() {
		Object result = ehCacheProvider.get("hello", String.class);
		Assert.isTrue("redis cache".equals(result), "The expected value should be 'ehcache cache'");
	}

	@After
	public void del() {
		ehCacheProvider.evict("hello");
		Object result = ehCacheProvider.get("hello", String.class);
		Assert.isNull(result, "del is ok.");
	}

}
