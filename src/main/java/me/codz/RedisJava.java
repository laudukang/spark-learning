package me.codz;

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

	public static void main(String[] args) {
		Jedis jedis = new Jedis("192.168.1.10", 6379);
		//jedis.auth("tmp");
		System.out.println("Server is running: " + jedis.ping());

		//String test
		System.out.println("\nString test:");
		jedis.set("hi", "lau");
		System.out.println(jedis.get("hi"));

		//List test
		System.out.println("\nList test:");
		jedis.del("tutorial-list");
		jedis.lpush("tutorial-list", "Redis");
		jedis.lpush("tutorial-list", "Mongodb");
		jedis.lpush("tutorial-list", "Mysql");
		List<String> list = jedis.lrange("tutorial-list", 0, -1);
		list.forEach(System.out::println);

		//Keys test
		System.out.println("\nKey set:");
		Set<String> keySet = jedis.keys("*");
		keySet.forEach(System.out::println);

		//Bean test
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
}
