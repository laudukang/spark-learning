package me.codz.redis;

import com.google.common.collect.Lists;
import me.codz.beans.Blog;
import org.apache.commons.lang3.SerializationUtils;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-8
 * <p>Time: 10:46
 * <p>Version: 1.0
 */
public class RedisJava {
	private static Jedis jedis;

	static {
		//jedis.auth("tmp");
		jedis = new Jedis("192.168.1.10", 6379);
		System.out.println("Server is running: " + jedis.ping());
	}

	public static void main(String[] args) {
		//stringTest();
		//listTest();
		//keysTest();
		//beanTest();
		beanListTest();
	}

	private static void stringTest() {
		System.out.println("\nString test:");
		jedis.set("hi", "lau");
		System.out.println(jedis.get("hi"));
	}

	private static void listTest() {
		System.out.println("\nList test:");
		jedis.del("tutorial-list");
		jedis.lpush("tutorial-list", "Redis");
		jedis.lpush("tutorial-list", "Mongodb");
		jedis.lpush("tutorial-list", "Mysql");
		List<String> list = jedis.lrange("tutorial-list", 0, -1);
		list.forEach(System.out::println);
	}

	private static void keysTest() {
		System.out.println("\nKey set:");
		Set<String> keySet = jedis.keys("*");
		keySet.forEach(System.out::println);
	}

	private static void beanTest() {
		Blog blog = new Blog();
		blog.setTitle("Spark is running");
		blog.setContent("Awesome spark");
		blog.setAuthor("apache");
		blog.setCreateTime(new Date());

		String blogCacheKey = "blog";
		jedis.set(blogCacheKey.getBytes(), SerializationUtils.serialize(blog));

		Blog blogFromRedis = SerializationUtils.deserialize(jedis.get(blogCacheKey.getBytes()));
		System.out.println(blogFromRedis);
	}

	private static void beanListTest() {
		Blog blog = new Blog();
		blog.setTitle("Awesome spark");
		blog.setContent("Spark is running");
		blog.setAuthor("apache");
		blog.setCreateTime(new Date());

		Blog blog2 = new Blog();
		blog2.setTitle("Redis Desktop Manager");
		blog2.setContent("Open source cross-platform Redis Desktop Manager based on Qt 5");
		blog2.setAuthor("user");
		blog2.setCreateTime(new Date());

		List<byte[]> blogList = Lists.newArrayList();
		blogList.add(SerializationUtils.serialize(blog));
		blogList.add(SerializationUtils.serialize(blog2));

		String blogListCacheKey = "bloglist";
		if (jedis.exists(blogListCacheKey.getBytes())) {
			jedis.del(blogListCacheKey.getBytes());
		}
		byte[][] data = blogList.toArray(new byte[blogList.size()][]);

		jedis.rpush(blogListCacheKey.getBytes(), data);

		List<byte[]> blogByteFromRedis = jedis.lrange(blogListCacheKey.getBytes(), 0, -1);
		blogByteFromRedis.forEach(b -> {
			Object object = SerializationUtils.deserialize(b);
			System.out.println(object);
		});
	}
}
