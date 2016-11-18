package com.phoenixkahlo.util;

public class StringUtil {

	public static String toBitString(byte[] arr) {
		StringBuilder builder = new StringBuilder();
		for (byte n : arr) {
			builder.append(Integer.toBinaryString(n));
			builder.append(' ');
		}
		return builder.toString();
	}
	
}
