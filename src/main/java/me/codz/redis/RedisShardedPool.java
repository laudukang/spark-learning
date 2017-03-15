package me.codz.redis;

import com.google.common.collect.Lists;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;
import java.util.ResourceBundle;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-14
 * <p>Time: 15:22
 * <p>Version: 1.0
 */
public class RedisShardedPool {
	private static ShardedJedisPool pool;

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

		JedisShardInfo jedisShardInfo1 = new JedisShardInfo(
				bundle.getString("redis.shard.ip1"), Integer.valueOf(bundle.getString("redis.shard.port1")));
		JedisShardInfo jedisShardInfo2 = new JedisShardInfo(
				bundle.getString("redis.shard.ip2"), Integer.valueOf(bundle.getString("redis.shard.port2")));
		List<JedisShardInfo> list = Lists.newLinkedList();
		list.add(jedisShardInfo1);
		list.add(jedisShardInfo2);

		pool = new ShardedJedisPool(config, list);
	}

	public static ShardedJedis getJedis() {
		return pool.getResource();
	}

	/**
	 * @deprecated starting from Jedis 3.0 this method will not be exposed. Resource cleanup should be
	 * done using @see {@link redis.clients.jedis.Jedis#close()}
	 */
	@Deprecated
	public static void returnResource(ShardedJedis shardedJedis) {
		pool.returnResource(shardedJedis);
	}
}
