package io.github.java.raecache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.github.javaovo.cache.redis.RedisCacheProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class RedisChannelTest {

	@Autowired
	private RedisCacheProvider redisCacheProvider;

	@Before
	public void put() {
		redisCacheProvider.put("hello", "redis cache");
	}

	@Test
	public void get() {
		Object result = redisCacheProvider.get("hello", String.class);
		System.out.println("get hello " + result);
	}

	@After
	public void del() {
		redisCacheProvider.evict("hello");
		Object result = redisCacheProvider.get("hello", String.class);
		System.out.println("get hello " + result);
	}

}
