package me.codz;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import me.codz.entity.Blog;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * <p>Created with IDEA
 * <p>Author: dukang.liu
 * <p>Date: 2017/12/13
 * <p>Time: 15:13
 * <p>Version: 1.0
 */
public class RedisTests {

    private static Jedis jedis;

    @Before
    public void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("redis_pool");
        if (bundle == null) {
            throw new IllegalArgumentException("Resource redis_pool.properties not found");
        }

        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(Integer.valueOf(bundle.getString("redis.pool.maxTotal")));
        config.setMaxIdle(Integer.valueOf(bundle.getString("redis.pool.maxIdle")));
        config.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWaitMillis")));
        config.setTestOnBorrow(Boolean.valueOf(bundle.getString("redis.pool.testOnBorrow")));
        config.setTestOnReturn(Boolean.valueOf(bundle.getString("redis.pool.testOnReturn")));

        JedisPool jedisPool = new JedisPool(config, bundle.getString("redis.ip"),
                Integer.valueOf(bundle.getString("redis.port")), Protocol.DEFAULT_TIMEOUT,
                bundle.getString("redis.password"), Integer.valueOf(bundle.getString("redis.database")));

        jedis = jedisPool.getResource();
    }

    @After
    public void after() {
        jedis.close();
    }

    @Test
    public void testStringSetGet() {
        String key = "hi";
        String value = "lau";
        jedis.set(key, value);
        Assert.assertEquals(value, jedis.get(key));
    }

    @Test
    public void testListPushGet() {
        jedis.del("tutorial-list");

        jedis.lpush("tutorial-list", "Redis");
        jedis.lpush("tutorial-list", "Mongodb");
        jedis.lpush("tutorial-list", "Mysql");

        List<String> list = jedis.lrange("tutorial-list", 0, -1);

        list.forEach(System.out::println);
    }

    @Test
    public void testKeysGet() {
        Set<String> keySet = jedis.keys("*");
        keySet.forEach(System.out::println);
    }

    @Test
    public void testEntitySave() {
        Blog blog = new Blog();
        blog.setTitle("Spark is running");
        blog.setContent("Awesome spark");
        blog.setAuthor("apache");
        blog.setCreateTime(new Date());

        String blogCacheKey = "blog";
        jedis.set(blogCacheKey.getBytes(), SerializationUtils.serialize(blog));

        Blog blogFromRedis = SerializationUtils.deserialize(jedis.get(blogCacheKey.getBytes()));

        System.out.println(JSON.toJSONString(blogFromRedis));
    }

    @Test
    public void testEntityListSave() {
        Blog blog = new Blog();
        blog.setTitle("Awesome spark");
        blog.setContent("Spark is running");
        blog.setAuthor("apache");
        blog.setCreateTime(new Date());

        Blog blog2 = new Blog();
        blog2.setTitle("Redis Desktop Manager");
        blog2.setContent("Open source cross-platform Redis Desktop Manager based on Qt 5");
        blog2.setAuthor("null");
        blog2.setCreateTime(new Date());

        List<byte[]> blogList = Lists.newArrayList();
        blogList.add(SerializationUtils.serialize(blog));
        blogList.add(SerializationUtils.serialize(blog2));

        String blogListCacheKey = "blog-list";
        if (jedis.exists(blogListCacheKey.getBytes())) {
            jedis.del(blogListCacheKey.getBytes());
        }
        byte[][] data = blogList.toArray(new byte[blogList.size()][]);

        jedis.rpush(blogListCacheKey.getBytes(), data);

        List<byte[]> blogByteFromRedis = jedis.lrange(blogListCacheKey.getBytes(), 0, -1);

        blogByteFromRedis.forEach(e -> {
            Blog blogFromRedis = SerializationUtils.deserialize(e);
            System.out.println(JSON.toJSONString(blogFromRedis));
        });
    }
}
