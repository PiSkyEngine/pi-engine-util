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

/**
 * An interface for objects that can be prioritized and compared based on their
 * priority. Implementing classes must provide a priority value, where lower
 * values typically indicate higher precedence (e.g., for sorting or task
 * scheduling).
 */
public interface Prioritizable extends Comparable<Prioritizable> {

	/**
	 * Returns the priority of this object. Lower values indicate higher precedence.
	 *
	 * @return the priority as an integer
	 */
	int priority();

	/**
	 * Compares this object with another {@code Prioritizable} based on their
	 * priority values. A lower priority value indicates this object precedes the
	 * other.
	 *
	 * @param other the {@code Prioritizable} to compare with
	 * @return a negative integer if this priority is lower, zero if equal, or a
	 *         positive integer if this priority is higher
	 * @throws NullPointerException if the other object is null
	 */
	@Override
	default int compareTo(Prioritizable other) {
		if (other == null) {
			throw new NullPointerException("Cannot compare to null Prioritizable");
		}

		return Integer.compare(priority(), other.priority());
	}
}