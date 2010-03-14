package com.shadowolf.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;

public class Data {
	static final byte[] HEX_CHAR_TABLE = { 
		(byte) '0', 
		(byte) '1', 
		(byte) '2', 
		(byte) '3', 
		(byte) '4', 
		(byte) '5', 
		(byte) '6', 
		(byte) '7', 
		(byte) '8',
		(byte) '9', 
		(byte) 'a', 
		(byte) 'b', 
		(byte) 'c', 
		(byte) 'd', 
		(byte) 'e', 
		(byte) 'f' 
	};
	
	public static String byteArrayToHexString(byte[] raw) {
		final byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			final int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		try {
			return new String(hex, "ASCII");
		} catch (UnsupportedEncodingException e) {
			// this will never really happen
			return "";
		}
	}

	public static byte[] addByteArrays(byte[] array1, byte[] array2) {
		if (array1 == null) {
			return array2.clone();
		} else if (array2 == null) {
			return array1.clone();
		}

		final byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	public static Object[] addObjectArrays(Object[] array1, Object[] array2) {
		if (array1 == null) {
			return array2.clone();
		} else if (array2 == null) {
			return array1.clone();
		}

		Object[] joinedArray = (Object[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		
		return joinedArray;
	}
}
