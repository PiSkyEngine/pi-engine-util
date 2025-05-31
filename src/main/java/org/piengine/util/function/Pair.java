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
package org.piengine.util.function;

import org.piengine.util.function.Tuple.Tuple2;

/**
 * Represents a pair of values, with one value of type {@code T1} and the other
 * of type {@code T2}. This interface provides a convenient way to group two
 * related values together. Both elements of the pair may be null.
 * 
 * <p>
 * The record implementation provides standard {@code equals()},
 * {@code hashCode()} and {@code toString()} behavior where:
 * <ul>
 * <li>equals() performs value-based comparison of all components, correctly
 * handling nulls
 * <li>hashCode() combines component hash codes consistently with equals()
 * <li>toString() formats as "Pair [value1, value2]"
 * </ul>
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * Pair<String, Integer> pair = Pair.of("Hello", 42);
 * System.out.println("First: " + pair.value1()); // Outputs "Hello"
 * System.out.println("Second: " + pair.value2()); // Outputs 42
 * 
 * // Null values are allowed
 * Pair<String, Integer> nullablePair = Pair.of(null, null);
 * }</pre>
 *
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 * 
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */

public interface Pair<T1, T2> extends Tuple2<T1, T2> {

	/**
	 * A record implementation of the {@code Pair} interface that stores two values.
	 */
	record PairRecord<T1, T2>(int size, @Nullable T1 value1, @Nullable T2 value2) implements Pair<T1, T2> {

		/**
		 * Returns an array containing both values of this pair.
		 *
		 * @return an array containing both values in order
		 * @see com.slytechs.jnet.platform.api.util.function.Tuple#values()
		 */
		@Override
		public Object[] values() {
			return new Object[] {
					value1,
					value2
			};
		}

		/**
		 * Returns a string representation of this pair.
		 *
		 * @return string in the format "Pair [value1, value2]"
		 */
		@Override
		public String toString() {
			return "Pair [" + String.valueOf(value1) + ", " + String.valueOf(value2) + "]";
		}
	}

	/**
	 * Creates a new {@code Pair} from an array of values.
	 *
	 * @param values an array containing exactly two values to create the pair
	 * @return a new {@code Pair} containing the specified values
	 * @throws IllegalStateException if the array does not contain exactly two
	 *                               values
	 */
	static Pair<?, ?> of(Object... values) {
		if (values.length != 2)
			throw new IllegalStateException("invalid number of arguments for a Pair");

		return new PairRecord<>(2, values[0], values[1]);
	}

	/**
	 * Creates a new {@code Pair} with the specified values.
	 *
	 * @param <T1>   the type of the first value
	 * @param <T2>   the type of the second value
	 * @param value1 the first value, may be null
	 * @param value2 the second value, may be null
	 * @return a new {@code Pair} containing the specified values
	 */
	static <T1, T2> Pair<T1, T2> of(@Nullable T1 value1, @Nullable T2 value2) {
		return new PairRecord<>(2, value1, value2);
	}

	/**
	 * Casts this pair to a pair with different generic type parameters.
	 *
	 * @param <C1> the new type for the first value
	 * @param <C2> the new type for the second value
	 * @return this pair cast to the new types
	 */
	@SuppressWarnings("unchecked")
	default <C1, C2> Pair<C1, C2> cast() {
		return (Pair<C1, C2>) this;
	}

	/**
	 * Returns the first value of the pair.
	 *
	 * @return the first value, may be null
	 */
	@Override
	@Nullable
	T1 value1();

	/**
	 * Returns the second value of the pair.
	 *
	 * @return the second value, may be null
	 */
	@Override
	@Nullable
	T2 value2();
}