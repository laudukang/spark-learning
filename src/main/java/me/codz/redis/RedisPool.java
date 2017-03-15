package me.codz.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-14
 * <p>Time: 14:59
 * <p>Version: 1.0
 */
public class RedisPool {
	private static JedisPool pool;

	static {
		ResourceBundle bundle = ResourceBundle.getBundle("redis_pool");
		if (bundle == null) {
			throw new IllegalArgumentException("[redis_pool.properties] not found");
		}
		JedisPoolConfig config = new JedisPoolConfig();

		config.setMaxTotal(Integer.valueOf(bundle.getString("redis.pool.maxTotal")));
		config.setMaxIdle(Integer.valueOf(bundle.getString("redis.pool.maxIdle")));
		config.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWaitMillis")));
		config.setTestOnBorrow(Boolean.valueOf(bundle.getString("redis.pool.testOnBorrow")));
		config.setTestOnReturn(Boolean.valueOf(bundle.getString("redis.pool.testOnReturn")));

		pool = new JedisPool(config, bundle.getString("redis.ip"), Integer.valueOf(bundle.getString("redis.port")));
	}

	public static Jedis getJedis() {
		return pool.getResource();
	}

	/**
	 * @deprecated starting from Jedis 3.0 this method will not be exposed. Resource cleanup should be
	 * done using @see {@link redis.clients.jedis.Jedis#close()}
	 */
	@Deprecated
	public static void returnResource(Jedis jedis) {
		pool.returnResource(jedis);
	}
}
