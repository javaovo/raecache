package io.github.java.raecache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import io.github.javaovo.cache.RaeCacheChannel;

/**
 * @Author: javaovo@163.com
 * @Date: 2018-10-07 21:45:11
 * @Since: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class CacheChannelTest {

	@Autowired
	private RaeCacheChannel raeCacheChannel;

	@Before
	public void put() {
		raeCacheChannel.put("hello", "RAE Cache");
	}

	@Test
	public void get() {
		Object result = raeCacheChannel.get("hello", String.class);
		Assert.isTrue("RAE Cache".equals(result), "The expected value should be 'RAE Cache'");
	}

	@After
	public void del() {
		raeCacheChannel.evict("hello");
		Object result = raeCacheChannel.get("hello", String.class);
		Assert.isNull(result, "del is ok.");
	}

}
