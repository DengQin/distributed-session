package com.dengqin.session.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 分布式session过滤器
 *
 * Created by dq on 2017/9/21.
 */
public class DistributedSessionFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(DistributedSessionFilter.class);// NOPMD

	private String cookieName;

	private DistributedSessionManager distributedSessionManager;

	// 密钥
	private String key;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws ServletException, IOException {
		DistributedHttpServletRequestWrapper distReq = null;
		try {
			distReq = createDistributedRequest(servletRequest, servletResponse);
			filterChain.doFilter(distReq, servletResponse);
		} catch (Throwable e) {
			log.error("doFilter:" + e.getMessage(), e);
			throw new DistributedSessionException(e.getMessage(), e);
		} finally {
			if (distReq != null) {
				try {
					dealSessionAfterRequest(distReq.getSession());
				} catch (Throwable e2) {
					log.error("dealSessionAfterRequest:" + e2.getMessage(), e2);
				}
			}
		}
	}

	/**
	 * request处理完时操作session
	 * 
	 * @param session
	 */
	private void dealSessionAfterRequest(DistributedHttpSessionWrapper session) {
		if (session == null) {
			return;
		}
		// 如果session改变了，就再次保存session
		if (session.changed) {
			distributedSessionManager.saveSession(session);
		} else if (session.invalidated) {
			// session失效了就从Redis里面删除
			distributedSessionManager.removeSession(session);
		} else {
			distributedSessionManager.expire(session);
		}
	}

	/**
	 * 创建分布式请求
	 * 
	 * @param servletRequest
	 * @param servletResponse
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	private DistributedHttpServletRequestWrapper createDistributedRequest(ServletRequest servletRequest,
			ServletResponse servletResponse) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String userSid = CookieUtil.getCookie(cookieName, request);
		String actualSid = distributedSessionManager.getActualSid(userSid, request, key);
		if (StringUtil.isBlank(actualSid)) {
			if (StringUtil.isNotBlank(userSid)) {
				log.info("userSid[{}]验证不通过", userSid);
			}
			// 写cookie
			String[] userSidArr = distributedSessionManager.createUserSid(request, key);
			userSid = userSidArr[0];
			CookieUtil.setCookie(cookieName, userSid, request, response);
			actualSid = userSidArr[1];
		}
		actualSid = "sid:" + actualSid;

		DistributedHttpSessionWrapper distSession = null;
		try {
			// 获取session的属性
			Map<String, Object> allAttribute = distributedSessionManager.getSession(actualSid,
					request.getSession().getMaxInactiveInterval());
			distSession = new DistributedHttpSessionWrapper(actualSid, request.getSession(), allAttribute);
		} catch (Throwable e) {
			// 出错，删掉缓存数据
			log.error("获取session出错：" + e.getMessage(), e);
			Map<String, Object> allAttribute = new HashMap<String, Object>();
			distSession = new DistributedHttpSessionWrapper(actualSid, request.getSession(), allAttribute);
			distributedSessionManager.removeSession(distSession);
		}

		DistributedHttpServletRequestWrapper requestWrapper = new DistributedHttpServletRequestWrapper(request,
				distSession);
		return requestWrapper;

	}

	/**
	 * 初始化相关数据
	 * 
	 * @param config
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		WebApplicationContext wac = WebApplicationContextUtils
				.getRequiredWebApplicationContext(config.getServletContext());
		String key = config.getInitParameter("key"); // 密钥
		String cookieName = config.getInitParameter("cookieName"); // cookieName
		String cacheBean = config.getInitParameter("cacheBean"); // 持久化对应的bean，即为Redis对应的bean
		// 获取bean的名称，配置是"bean:xxxxx"
		String redisBeanStr = cacheBean.substring(5);
		DistributedBaseInterFace distributedCache = (DistributedBaseInterFace) wac.getBean(redisBeanStr);

		// 获取key，有2种配置方式：（1）以“bean:”开头，格式为bean:key；（2）字符串,格式如:xxxxxx。
		if (key.startsWith("bean:")) {
			this.key = (String) wac.getBean(key.substring(5));
		} else {
			this.key = key;
		}
		this.cookieName = cookieName;
		this.distributedSessionManager = DistributedSessionManager.getInstance(distributedCache); // 实例化分布式session管理器

		// 初始化的内容以及相关的参数检查
		DistributedSessionException.assertNotBlank(key, "key不能为空");
		DistributedSessionException.assertNotBlank(cookieName, "cookieName不能为空");
		DistributedSessionException.assertNotBlank(cacheBean, "cacheBean不能为空");
		DistributedSessionException.isTrue(cacheBean.startsWith("bean:"), "cacheBean不是以[bean:]开头");
		DistributedSessionException.assertNotNull(distributedCache, "distributedCache初始化失败，为空了");
		DistributedSessionException.assertNotBlank(key, "key初始化失败，为空了");
	}

	@Override
	public void destroy() {

	}
}
