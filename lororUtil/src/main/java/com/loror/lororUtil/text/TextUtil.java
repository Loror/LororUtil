package com.loror.lororUtil.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

	private TextUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 是否为空
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证字符串是否为IP地址
	 */
	public static boolean isIP(String ip) {
		if (ip == null) {
			return false;
		}
		String[] test = ip.split("\\.");
		if (test.length != 4)
			return false;
		for (int i = 0; i < test.length; i++) {
			if (!isNumber(test[i]))
				return false;
		}
		return true;
	}

	/**
	 * 验证字符串是否为纯数字
	 */
	public static boolean isNumber(String number) {
		if (number == null) {
			return false;
		}
		for (int i = 0; i < number.length(); i++) {
			char get = number.charAt(i);
			if (get < '0' || get > '9')
				return false;
		}
		return true;
	}

	/**
	 * 验证是否是手机号
	 */
	public static boolean isMobile(String mobile) {
		String REGEX_MOBILE = "^1(3|5|7|8)[0-9]{9}$";
		Pattern PATTERN_MOBILE = Pattern.compile(REGEX_MOBILE);
		return PATTERN_MOBILE.matcher(mobile).matches();
	}

	/**
	 * 验证是否为邮箱格式
	 */
	public static boolean isEmail(String email) {
		String regular = "\\w+@(\\w+\\.){1,3}\\w+";
		Pattern pattern = Pattern.compile(regular);
		boolean flag = false;
		if (!email.isEmpty()) {
			Matcher matcher = pattern.matcher(email);
			flag = matcher.matches();
		} else {
			return true;
		}
		return flag;
	}

	/**
	 * 驼峰转_小写
	 */
	public static String humpTo_lowercase(String hump) {
		if (hump == null) {
			return hump;
		}
		char[] chars = hump.toCharArray();
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isUpperCase(c)) {
				stringBuffer.append("_");
				stringBuffer.append(Character.toLowerCase(c));
			} else {
				stringBuffer.append(c);
			}
		}
		return stringBuffer.toString();
	}
}
