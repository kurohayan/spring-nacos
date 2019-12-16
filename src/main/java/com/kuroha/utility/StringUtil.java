package com.kuroha.utility;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * @author livecat
 * @date 2019-03-04 16:34:40
 */
public class StringUtil {

	private static final String NOT_STRING = "null";

	/**
	 * 判断不为空
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
	 *
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}
		int len = str.length();
		if (len == 0) {
			return true;
		}
		if (NOT_STRING.equals(str)) {
			return true;
		}
		for (int i = 0; i < len; i++) {
			switch (str.charAt(i)) {
				case ' ':
				case '\t':
				case '\n':
				case '\r':
					break;
				default:
					return false;
			}
		}
		return true;
	}
/**
	 * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
	 *
	 * @return
	 */
	public static boolean isBlankObjectField(Object object) {
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				Object value = field.get(object);
				if (isBlank(String.valueOf(value))){
					return true;
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean isBlank(String... strs) {
		if (strs == null) {
			return true;
		}
		int len = strs.length;
		if (len == 0) {
			return true;
		}
		for (String str : strs) {
			if (isBlank(str)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNull(Object... objects) {
		return !isNotNull(objects);
	}

	public static boolean isNotNull(Object... objects) {
		if (objects == null) {
			return false;
		}
		int len = objects.length;
		if (len == 0) {
			return false;
		}
		for (Object object : objects) {
			if (object == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 拼接字符串
	 *
	 * @param strs
	 * @return
	 */
	public static String splicingString(Object... strs) {
		StringBuilder builder = new StringBuilder();
		for (Object str : strs) {
			builder.append(str);
		}
		return builder.toString();
	}

	/**
	 * 首字母变小写
	 *
	 * @param str 需要转换的字符串
	 * @return 转换后的字符串
	 */
	public static String firstCharToLowerCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'A' && firstChar <= 'Z') {
			char[] arr = str.toCharArray();
			arr[0] += ('a' - 'A');
			return new String(arr);
		}
		return str;
	}

	/**
	 * 首字母变大写
	 *
	 * @param str 需要转换的字符串
	 * @return 转换后的字符串
	 */
	public static String firstCharToUpperCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'a' && firstChar <= 'z') {
			char[] arr = str.toCharArray();
			arr[0] -= ('a' - 'A');
			return new String(arr);
		}
		return str;
	}


	/**
	 * 获取uuid并转换成大写字母
	 *
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
	public static String uuid(int num) {
		return UUID.randomUUID().toString().replace("-", "").substring(0,num).toUpperCase();
	}


}
