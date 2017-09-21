package com.dengqin.session.distributed;

/**
 * 分布式session异常
 *
 * Created by dq on 2017/9/21.
 */
public class DistributedSessionException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = -5680287893422435319L;

	public static void assertNotNull(Object obj, String msg) {
		if (obj == null) {
			throw new DistributedSessionException(msg);
		}
	}

	public static void assertNotBlank(String str, String msg) {
		if (StringUtil.isBlank(str)) {
			throw new DistributedSessionException(msg);
		}
	}

	public static void isTrue(boolean bool, String msg) {
		if (!bool) {
			throw new DistributedSessionException(msg);
		}
	}

	public DistributedSessionException() {
		super();
	}

	public DistributedSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DistributedSessionException(String message) {
		super(message);
	}

	public DistributedSessionException(Throwable cause) {
		super(cause);
	}
}
