/*
 * MIT License
 * 
 * Copyright (c) 2025 Sly Technologies Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.piengine.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Various enum constant related utility classes.
 *
 * @author mark
 */
public final class Enums {

	/**
	 * Find enum.
	 *
	 * @param <T>      the generic type
	 * @param enumType the enum type
	 * @param name     the name
	 * @return the optional
	 */
	public static <T extends Enum<T>> Optional<T> findEnum(Class<T> enumType, String name) {
		for (T e : enumType.getEnumConstants()) {
			boolean isMatch = false
					|| e.name().equalsIgnoreCase(name)
					|| e.name().equals(toEnumName(name));

			if (isMatch)
				return Optional.of(e);
		}

		return Optional.empty();
	}

	/**
	 * Gets the enum.
	 *
	 * @param <T>      the generic type
	 * @param enumType the enum type
	 * @param name     the name
	 * @return the enum
	 */
	public static <T extends Enum<T>> T getEnum(Class<T> enumType, String name) {
		for (T e : enumType.getEnumConstants()) {
			boolean isMatch = false
					|| e.name().equalsIgnoreCase(name)
					|| e.name().equals(toEnumName(name));

			if (isMatch)
				return e;
		}

		return null;
	}

	/**
	 * Gets the enum or throw.
	 *
	 * @param <T>           the generic type
	 * @param <T_EXCEPTION> the generic type
	 * @param enumType      the enum type
	 * @param name          the name
	 * @param onNotFound    the on not found
	 * @return the enum or throw
	 * @throws T_EXCEPTION the t exception
	 */
	public static <T extends Enum<T>, T_EXCEPTION extends Throwable> T getEnumOrThrow(Class<T> enumType, String name,
			Function<String, T_EXCEPTION> onNotFound)
			throws T_EXCEPTION {
		T e = getEnum(enumType, name);
		if (e == null)
			throw onNotFound.apply(name);

		return e;
	}

	/**
	 * Gets the enum or throw.
	 *
	 * @param <T>           the generic type
	 * @param <T_EXCEPTION> the generic type
	 * @param enumType      the enum type
	 * @param name          the name
	 * @param onNotFound    the on not found
	 * @return the enum or throw
	 * @throws T_EXCEPTION the t exception
	 */
	public static <T extends Enum<T>, T_EXCEPTION extends Throwable> T getEnumOrThrow(Class<T> enumType, String name,
			Supplier<T_EXCEPTION> onNotFound)
			throws T_EXCEPTION {
		return getEnumOrThrow(enumType, name, n -> onNotFound.get());
	}

	/**
	 * Resolve.
	 *
	 * @param <E>   the element type
	 * @param value the value
	 * @param cl    the cl
	 * @return the string
	 */
	public static <E extends Enum<E> & IntSupplier> String resolve(Object value, Class<E> cl) {
		if (!(value instanceof Number n))
			return null;

		E e = valueOf(n.intValue(), cl);
		if (e == null)
			return null;

		return e.name();
	}

	/**
	 * To dot name.
	 *
	 * @param enumName the enum name
	 * @return the string
	 */
	public static String toDotName(String enumName) {
		return enumName
				.replace('_', '.')
				.replace('$', '_')
				.toLowerCase();
	}

	/**
	 * To enum name.
	 *
	 * @param javaName the java name
	 * @return the string
	 */
	public static String toEnumName(String javaName) {
		return javaName
				.replace('_', '$')
				.replace('.', '_')
				.toUpperCase();
	}

	/**
	 * Value of.
	 *
	 * @param <E>   the element type
	 * @param value the value
	 * @param cl    the cl
	 * @return the e
	 */
	public static <E extends Enum<E> & IntSupplier> E valueOf(int value, Class<E> cl) {
		E[] constants = cl.getEnumConstants();
		for (E e : constants)
			if (e.getAsInt() == value)
				return e;

		return null;
	}

	/**
	 * Instantiates a new enum utils.
	 */
	private Enums() {}

}