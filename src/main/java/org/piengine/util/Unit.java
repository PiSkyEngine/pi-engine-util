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
 * The Unit interface represents a unit of measurement and provides methods for
 * conversion, parsing, and symbol retrieval.
 *
 * @author Mark Bednarczyk
 */
public interface Unit {

	/**
	 * Gets the primary symbol for this unit.
	 *
	 * @return the primary symbol or an empty string if no symbols are defined
	 */
	default String getSymbol() {
		return getSymbols().length == 0 ? "" : getSymbols()[0];
	}

	/**
	 * Gets all symbols associated with this unit.
	 *
	 * @return an array of strings representing the symbols for this unit
	 */
	String[] getSymbols();

	/**
	 * Gets the name of this unit.
	 *
	 * @return the name of the unit
	 */
	String name();

	/**
	 * Parses the unit from a string containing both value and unit.
	 * 
	 * <p>
	 * For example, "10MB" when using MemoryUnit will ignore the "10" value and
	 * convert "MB" into MemoryUnit.MEGA_BYTES. Spaces are ignored during parsing.
	 * </p>
	 *
	 * @param <U>           the generic type of the unit
	 * @param valueAndUnits the string containing both value and unit
	 * @param defaultUnits  the default unit to return if parsing fails
	 * @return the parsed unit or the default unit if parsing fails
	 */
	@SuppressWarnings("unchecked")
	default <U extends Unit> U parseUnits(String valueAndUnits, U defaultUnits) {
		Unit newUnits = UnitUtils.parseUnits(valueAndUnits, this.getClass());
		if (newUnits == null)
			return defaultUnits;
		return (U) newUnits;
	}

	/**
	 * Strips the unit component from a string containing both value and unit.
	 *
	 * @param valueAndUnits the string containing both value and unit
	 * @return a string containing only the value without the unit
	 */
	default String stripUnits(String valueAndUnits) {
		return UnitUtils.stripUnits(valueAndUnits, this.getClass());
	}

	/**
	 * Converts the given value to the base unit.
	 *
	 * @param value the value to convert
	 * @return the value in the base unit
	 */
	long toBase(long value);

	/**
	 * Converts the given value to the base unit and returns it as an integer.
	 *
	 * @param value the value to convert to the base unit
	 * @return the base unit value as a 32-bit integer
	 * @throws IllegalArgumentException if the conversion results in an integer
	 *                                  overflow
	 */
	default int toBaseAsInt(long value) throws IllegalArgumentException {
		long longValue = toBase(value);
		if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE)
			throw new IllegalArgumentException("integer conversion overflow [%d]"
					.formatted(longValue));
		return (int) longValue;
	}
}