package com.dengqin.session.distributed;

/**
 * 分布式基础接口
 *
 * Created by dq on 2017/9/21.
 */
public interface DistributedBaseInterFace {
	/**
	 * 根据key获取缓存数据
	 * 
	 * @param key
	 * @param seconds
	 * @return
	 */
	public String get(String key, int seconds);

	/**
	 * 更新缓存数据
	 * 
	 * @param key
	 * @param json
	 * @param seconds
	 */
	public void set(String key, String json, int seconds);

	/**
	 * 删除缓存
	 * 
	 * @param key
	 */
	public void del(String key);

	/**
	 * 设置过期数据
	 * 
	 * @param key
	 * @param seconds
	 */
	public void expire(String key, int seconds);
}
