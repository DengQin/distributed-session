package com.dengqin.session.distributed;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by dq on 2017/9/21.
 */
public class CookieUtil {
	/**
	 * 设置cookie</br>
	 *
	 * @param name
	 *            cookie名称
	 * @param value
	 *            cookie值
	 * @param request
	 *            http请求
	 * @param response
	 *            http响应
	 */
	public static void setCookie(String name, String value, HttpServletRequest request, HttpServletResponse response) {
		int maxAge = -1;
		// 将cookie设置成HttpOnly是为了防止XSS攻击，窃取cookie内容，这样就增加了cookie的安全性，即便是这样，也不要将重要信息存入cookie
		boolean httpOnly = false;
		boolean currentDomain = true;
		setCookie(name, value, maxAge, httpOnly, currentDomain, request, response);
	}

	/**
	 * 设置cookie</br>
	 *
	 * @param name
	 *            cookie名称
	 * @param value
	 *            cookie值
	 * @param maxAge
	 *            最大生存时间
	 * @param httpOnly
	 *            cookie的路径
	 * @param currentDomain
	 *            是否使用当前的域名
	 * @param request
	 *            http请求
	 * @param response
	 *            http响应
	 */
	private static void setCookie(String name, String value, int maxAge, boolean httpOnly, boolean currentDomain,
			HttpServletRequest request, HttpServletResponse response) {
		if (StringUtil.isBlank(name)) {
			throw new RuntimeException("cookie名称不能空串.name[" + name + "]");
		}
		if (value == null) {
			throw new NullPointerException("cookie值不能为空.");
		}
		String serverName = request.getServerName();
		String domain;
		if (currentDomain) {
			domain = serverName;
		} else {
			domain = getDomain(serverName);
		}

		Cookie cookie = new Cookie(name, value);
		cookie.setDomain(domain);
		cookie.setMaxAge(maxAge);
		if (httpOnly) {
			cookie.setPath("/;HttpOnly");
		} else {
			cookie.setPath("/");
		}
		response.addHeader("P3P",
				"CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
		response.addCookie(cookie);

	}

	/**
	 * 获取cookie的值</br>
	 *
	 * @param name
	 *            cookie名称
	 * @param request
	 *            http请求
	 * @return cookie值
	 */
	public static String getCookie(String name, HttpServletRequest request) {
		if (StringUtil.isBlank(name)) {
			throw new RuntimeException("cookie名称不能空串.name[" + name + "]");
		}
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (int i = 0; i < cookies.length; i++) {
			if (name.equalsIgnoreCase(cookies[i].getName())) {
				return cookies[i].getValue();
			}
		}
		return null;
	}

	/**
	 * 返回域名</br>
	 *
	 * @param serverName
	 *            服务器地址
	 * @return 域名
	 */
	private static String getDomain(String serverName) {
		int index = serverName.indexOf(".");
		String domain = serverName.substring(index);
		return domain;
	}
}
