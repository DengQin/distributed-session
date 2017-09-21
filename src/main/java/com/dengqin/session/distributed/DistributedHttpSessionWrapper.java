package com.dengqin.session.distributed;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 分布式session包装器，实现了HttpSession
 * 
 * Created by dq on 2017/9/21.
 */
public class DistributedHttpSessionWrapper implements HttpSession {

	/** 原始的session */
	private HttpSession orgiSession;

	/** session的唯一标识id */
	private String sid;

	/** session是否有改变，默认false，即默认session未做变更 */
	boolean changed = false;

	/** 是否使session无效，默认false，即默认session是有效的 */
	boolean invalidated = false;

	/** 所有的属性 */
	Map<String, Object> allAttribute;

	public DistributedHttpSessionWrapper(String sid, HttpSession session, Map<String, Object> allAttribute) {
		this.orgiSession = session;
		this.sid = sid;
		this.allAttribute = allAttribute;
	}

	/**
	 * 获取session的唯一标识id
	 * 
	 * @return
	 */
	@Override
	public String getId() {
		return this.sid;
	}

	/**
	 * 设置session属性，session改变
	 * 
	 * @param name
	 * @param value
	 */
	@Override
	public void setAttribute(String name, Object value) {
		changed = true;
		allAttribute.put(name, value);
	}

	/**
	 * 获取指定的session属性
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public Object getAttribute(String name) {
		return allAttribute.get(name);
	}

	/**
	 * 获取全部的属性名称，枚举实现
	 * 
	 * @return
	 */
	@Override
	public Enumeration<String> getAttributeNames() {
		Set<String> set = allAttribute.keySet();
		Iterator<String> iterator = set.iterator();
		return new MyEnumeration<String>(iterator);
	}

	private class MyEnumeration<T> implements Enumeration<T> {
		Iterator<T> iterator;

		public MyEnumeration(Iterator<T> iterator) {
			super();
			this.iterator = iterator;
		}

		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		@Override
		public T nextElement() {
			return iterator.next();
		}

	}

	/**
	 * 使无效
	 */
	@Override
	public void invalidate() {
		this.invalidated = true;
	}

	/**
	 * 删除指定的属性，session改变
	 * 
	 * @param name
	 */
	@Override
	public void removeAttribute(String name) {
		changed = true;
		allAttribute.remove(name);
	}

	/**
	 * session被创建的时间，毫秒
	 * 
	 * @return
	 */
	@Override
	public long getCreationTime() {
		return orgiSession.getCreationTime();
	}

	/**
	 * 一个客户端请求的最后的活动时间
	 * 
	 * @return
	 */
	@Override
	public long getLastAccessedTime() {
		return orgiSession.getLastAccessedTime();
	}

	/**
	 * 获取客户端的servlet最大不活跃时间间隔，过了这个时间，将会把session置为无效（单位秒）
	 * 
	 * @return
	 */
	@Override
	public int getMaxInactiveInterval() {
		return orgiSession.getMaxInactiveInterval();
	}

	/**
	 * Returns the ServletContext to which this session belongs., The
	 * ServletContext object for the web application
	 * 
	 * @return
	 */
	@Override
	public ServletContext getServletContext() {
		return orgiSession.getServletContext();
	}

	/**
	 * @deprecated As of Version 2.2, this method is replaced by
	 *             {@link #getAttribute}.
	 * @param arg0
	 * @return
	 */
	@Override
	public Object getValue(String arg0) {
		return orgiSession.getValue(arg0);
	}

	/**
	 * As of Version 2.2, this method is replaced by {@link #getAttributeNames}
	 * 
	 * @return
	 */
	@Override
	public String[] getValueNames() {
		return orgiSession.getValueNames();
	}

	/**
	 * Returns <code>true</code> if the client does not yet know about the
	 * session or if the client chooses not to join the session. For example, if
	 * the server used only cookie-based sessions, and the client had disabled
	 * the use of cookies, then a session would be new on each request.
	 *
	 * @return <code>true</code> if the server has created a session, but the
	 *         client has not yet joined
	 *
	 * @exception IllegalStateException
	 *                if this method is called on an already invalidated session
	 */
	@Override
	public boolean isNew() {
		return orgiSession.isNew();
	}

	/**
	 * As of Version 2.2, this method is replaced by {@link #setAttribute}
	 * 
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void putValue(String arg0, Object arg1) {
		orgiSession.putValue(arg0, arg1);
	}

	/**
	 * As of Version 2.2, this method is replaced by {@link #removeAttribute}
	 * 
	 * @param arg0
	 */
	@Override
	public void removeValue(String arg0) {
		orgiSession.removeValue(arg0);
	}

	/**
	 * Specifies the time, in seconds, between client requests before the
	 * servlet container will invalidate this session.
	 * 即客户端在请求之前，设置容器使session失效的时间间隔，秒
	 * 
	 * @param arg0
	 */
	@Override
	public void setMaxInactiveInterval(int arg0) {
		orgiSession.setMaxInactiveInterval(arg0);
	}

	/**
	 * As of Version 2.1, this method is deprecated and has no replacement. It
	 * will be removed in a future version of the Java Servlet API.
	 * 
	 * @return
	 */
	@Override
	public HttpSessionContext getSessionContext() {
		return orgiSession.getSessionContext();
	}
}
