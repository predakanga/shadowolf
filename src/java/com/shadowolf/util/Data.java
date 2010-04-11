package com.shadowolf.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;

import org.apache.log4j.Logger;

/**
 * "Static" Data utility classes that wraps some useful methods.
 *
 */
final public class Data {
	private static final Logger LOGGER = Logger.getLogger(Data.class);

	private Data() {

	}

	static final private byte[] HEX_CHAR_TABLE = {
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

	/**
	 * Converts a hex-encoded string to a byte array
	 * @param hexString the hex-encoded string to conver
	 * @return the resultant byte array
	 */
	public static byte[] hexStringToByteArray(final String hexString) {
		final int NumberChars = hexString.length();
		final byte[] bytes = new byte[NumberChars / 2];
		for (int i = 0; i < NumberChars; i += 2) {
			bytes[i / 2] =
				(byte) ((Character.digit(hexString.charAt(i), 16) << 4)
						+ Character.digit(hexString.charAt(i+1), 16));
		}

		//Byte.valueOf("0x" + hexString.substring(i, 2), 16);
		return bytes;
	}
	/**
	 * Converts a byte array to a hex-encoded string.
	 * @param raw the byte array to convert.
	 * @return the hex-encoded resultant string.
	 */
	public static String byteArrayToHexString(final byte[] raw) {
		final byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (final byte bite : raw) {
			final int vector = bite & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[vector >>> 4];
			hex[index++] = HEX_CHAR_TABLE[vector & 0xF];
		}

		String finalRes;
		try {
			finalRes = new String(hex, "ASCII");
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Impossible exception thrown!");
			finalRes = "";
		}

		return finalRes;
	}

	/**
	 * Adds two byte arrays.
	 * @param array1 the first byte array.
	 * @param array2 the second byte array.
	 * @return the array result of adding both arrays.
	 */
	public static byte[] addByteArrays(final byte[] array1, final byte[] array2) {
		if (array1 == null) {
			return array2.clone(); //NOPMD ... single exit point makes no sense
		} else if (array2 == null) {
			return array1.clone(); //NOPMD ... see above
		}

		final byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * Adds two Object arrays.
	 * @param array1 the first Object array.
	 * @param array2 the second Object array.
	 * @return the array result of adding both arrays.
	 */
	public static Object[] addObjectArrays(final Object[] array1, final Object[] array2) {
		if (array1 == null) {
			return array2.clone(); //NOPMD ... see above
		} else if (array2 == null) {
			return array1.clone(); //NOPMD ... see above
		}

		final Object[] joinedArray = (Object[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);

		return joinedArray;
	}
}
