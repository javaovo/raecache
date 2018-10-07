package io.github.java.raecache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.github.javaovo.cache.ehcache.EhCacheProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class EhCacheChannelTest {

	@Autowired
	private EhCacheProvider ehCacheProvider;

	@Before
	public void put() {
		ehCacheProvider.put("hello", "ehcache");
	}

	@Test
	public void get() {
		Object result = ehCacheProvider.get("hello", String.class);
		System.out.println("get hello " + result);
	}

	@After
	public void del() {
		ehCacheProvider.evict("hello");
		Object result = ehCacheProvider.get("hello", String.class);
		System.out.println("get hello " + result);
	}

}
