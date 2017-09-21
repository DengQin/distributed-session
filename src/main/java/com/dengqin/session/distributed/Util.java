package com.dengqin.session.distributed;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dq on 2017/9/21.
 */
public class Util {

	/**
	 * 做安全验证,需要配置nginx的proxy_set_header X-Real-IP $remote_addr;
	 *
	 * @param request
	 * @return
	 */
	public static String getRealIp(HttpServletRequest request) {
		return StringUtil.trim(request.getHeader("X-Real-IP"));
	}
}
