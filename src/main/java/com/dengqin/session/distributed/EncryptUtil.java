package com.dengqin.session.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * Created by dq on 2017/9/21.
 */
public class EncryptUtil {
	private static Logger log = LoggerFactory.getLogger(EncryptUtil.class);

	public static String getMD5(String str) {
		return encode(str, "MD5");
	}

	public static String getSHA1(String str) {
		return encode(str, "SHA-1");
	}

	public static String getLittleMD5(String str) {
		String estr = encode(str, "MD5");
		return estr.substring(0, 20);
	}

	public static String getLittleSHA1(String str) {
		String estr = encode(str, "SHA-1");
		return estr.substring(0, 20);
	}

	private static String encode(String str, String type) {
		try {
			MessageDigest alga = java.security.MessageDigest.getInstance(type);
			alga.update(str.getBytes("UTF-8"));
			byte[] digesta = alga.digest();
			return byte2hex(digesta);
		} catch (Exception e) {
			log.error("encode:" + e.getMessage(), e);
			return "";
		}
	}

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toUpperCase();
	}
}
