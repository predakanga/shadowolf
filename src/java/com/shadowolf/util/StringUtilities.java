package com.shadowolf.util;

public final class StringUtilities {
	private StringUtilities() {

	}

	/**
	 * Joins a string array with the default separator (",").
	 * @param input the string array to join
	 * @return the conjoined string.
	 */
	public static String stringJoin(final String[] input) {
		return stringJoin(input, ",");
	}

	/**
	 * Joins a string array separated by </i>separator</i>; functionally equivalent to php's implode().
	 * @param input the string array to join
	 * @param separator the separator to join string elements with
	 * @return the conjoined string.
	 */
	public static  String stringJoin(final String[] input, final String separator) {
		final StringBuilder builder = new StringBuilder();
		builder.append(input[0]);

		for (int i = 1; i < input.length; i++) {
			builder.append(separator);
			builder.append(input[i]);
		}

		return builder.toString();
	}
}
