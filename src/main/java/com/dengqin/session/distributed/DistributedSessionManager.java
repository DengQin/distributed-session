package com.dengqin.session.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 分布式session管理器
 *
 * Created by dq on 2017/9/21.
 */
public class DistributedSessionManager {
	protected static final Logger log = LoggerFactory.getLogger(DistributedSessionManager.class);

	private static DistributedSessionManager instance = null;

	private DistributedBaseInterFace distributedBaseInterFace;

	private static byte[] lock = new byte[1];

	private DistributedSessionManager(DistributedBaseInterFace distributedBaseInterFace) {
		this.distributedBaseInterFace = distributedBaseInterFace;
	}

	public static DistributedSessionManager getInstance(DistributedBaseInterFace redis) {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new DistributedSessionManager(redis);
				}
			}
		}
		return instance;
	}

	/**
	 * 根据sid获取持久化后的session，主要是session的全部属性，反序列化为map对象
	 * 
	 * @param sid
	 * @param second
	 * @return
	 */
	public Map<String, Object> getSession(String sid, int second) {
		String json = this.distributedBaseInterFace.get(sid, second);
		if (StringUtil.isNotBlank(json)) {
			return JsonUtil.unserializeMap(json);
		}
		return new HashMap<String, Object>(1);
	}

	/**
	 * 保存session，主要是持久化session的全部属性，并以session的最大有效期为持久化的过期时间
	 * 
	 * @param session
	 */
	public void saveSession(DistributedHttpSessionWrapper session) {
		Map<String, Object> map = session.allAttribute;
		if (MapUtil.isEmpty(map)) {
			return;
		}
		String json = JsonUtil.serializeMap(map);
		this.distributedBaseInterFace.set(session.getId(), json, session.getMaxInactiveInterval());
	}

	/**
	 * 删除session
	 * 
	 * @param session
	 */
	public void removeSession(DistributedHttpSessionWrapper session) {
		distributedBaseInterFace.del(session.getId());
	}

	/**
	 * 设置session的最大有效期为持久化的过期时间
	 * 
	 * @param session
	 */
	public void expire(DistributedHttpSessionWrapper session) {
		distributedBaseInterFace.expire(session.getId(), session.getMaxInactiveInterval());
	}

	/**
	 * 创建cookie的sid
	 */
	public String[] createUserSid(HttpServletRequest request, String key) {
		String sid = java.util.UUID.randomUUID().toString(); // 随机串sid
		sid = sid.replace("-", "");
		String ip = Util.getRealIp(request);
		String cookieSid = sid + "-" + md5(key, sid, ip);
		String actualSid = sid + "-" + ip;
		String[] result = new String[] { cookieSid, actualSid };
		return result;
	}

	private String md5(String key, String sid, String ip) {
		return EncryptUtil.getLittleMD5(sid + key + ip + key);
	}

	/**
	 * 获取真实的sid，userSid就是cookieSid
	 * 
	 * sessionid_in_cookie: 随机串+签名(签名中包含了IP、数字、key)<br>
	 * sessionid_in_redis: 随机串+ip
	 */
	public String getActualSid(String userSid, HttpServletRequest request, String key) {
		if (StringUtil.isBlank(userSid)) {
			return null;
		}
		String[] arr = userSid.split("-");
		if (arr.length != 2) {
			return null;
		}
		String ip = Util.getRealIp(request);
		// arr[0]是随机串sid，arr[1]是md5的签名
		if (!md5(key, arr[0], ip).equals(arr[1])) {
			log.error("userSid[{}]签名验证失败，IP[{}],userCookiePre[{}]", new Object[] { userSid, ip, arr[0] });
			return null;
		}
		return arr[0] + "-" + ip;
	}

}
