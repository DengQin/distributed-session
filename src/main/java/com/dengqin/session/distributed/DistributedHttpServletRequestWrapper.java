package com.dengqin.session.distributed;

import javax.servlet.http.HttpServletRequest;

/**
 * 分布式请求包装器
 *
 * Created by dq on 2017/9/21.
 */
public class DistributedHttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
	// 原始请求
	private HttpServletRequest orgiRequest;
	private DistributedHttpSessionWrapper session;

	public DistributedHttpServletRequestWrapper(HttpServletRequest request, DistributedHttpSessionWrapper session) {
		super(request);
		if (session == null)
			throw new DistributedSessionException("session实例不能为空");
		if (request == null)
			throw new DistributedSessionException("request实例不能为空");
		this.orgiRequest = request;
		this.session = session;
	}

	public DistributedHttpSessionWrapper getSession(boolean create) {
		// 如果create设置true,返回当前的HttpSession,如果没有会话，创建一个新的会话。
		// 如果create设置false,如果当前有会话那就返回，如果没有会话，就返回null
		orgiRequest.getSession(create);
		return session;
	}

	public DistributedHttpSessionWrapper getSession() {
		return session;
	}
}
