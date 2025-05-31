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
 * The Class UnitUtils.
 *
 * @author Sly Technologies
 */
class UnitUtils {

	/**
	 * The Interface ConvertableUnit.
	 *
	 * @param <T> the generic type
	 * @author Sly Technologies
	 */
	interface ConvertableUnit<T extends Enum<T>> extends Unit {

		/**
		 * Convert.
		 *
		 * @param source the source
		 * @param unit   the unit
		 * @return the long
		 */
		long convert(long source, T unit);

		/**
		 * Convertf.
		 *
		 * @param source the source
		 * @return the double
		 */
		double convertf(double source);

		/**
		 * Convertf.
		 *
		 * @param source     the source
		 * @param sourceUnit the source unit
		 * @return the double
		 */
		double convertf(double source, T sourceUnit);

	}

	/**
	 * Instantiates a new unit utils.
	 */
	private UnitUtils() {
	}

	/**
	 * Nearest.
	 *
	 * @param <T>   the generic type
	 * @param value the value
	 * @param type  the type
	 * @param base  the base
	 * @return the t
	 */
	static <T extends Enum<T> & ConvertableUnit<T>> T nearest(long value, Class<T> type, T base) {
		T[] values = type.getEnumConstants();

		for (int i = values.length - 1; i >= 0; i--) {
			T u = values[i];

			if (u.convert(value, base) > 0) {
				return u;
			}
		}

		return base;
	}

	/**
	 * Format.
	 *
	 * @param <T>   the generic type
	 * @param fmt   the fmt
	 * @param value the value
	 * @param type  the type
	 * @param base  the base
	 * @return the string
	 */
	static <T extends Enum<T> & ConvertableUnit<T>> String format(String fmt,
			long value,
			Class<T> type,
			T base) {
		T unit = nearest(value, type, base);
		return String.format(fmt, unit.convertf(value), unit.getSymbol());
	}

	/**
	 * Values.
	 *
	 * @param <U> the generic type
	 * @param cl  the cl
	 * @return the u[]
	 */
	static <U extends Unit> U[] values(Class<U> cl) {
		if (!(Enum.class.isAssignableFrom(cl)))
			throw new IllegalArgumentException("only enum based units are supported [%s]"
					.formatted(cl));

		U[] values = cl.getEnumConstants();

		return values;
	}

	/**
	 * Parses the units.
	 *
	 * @param <U>           the generic type
	 * @param valueAndUnits the value and units
	 * @param cl            the cl
	 * @return the u
	 */
	static <U extends Unit> U parseUnits(String valueAndUnits, Class<U> cl) {
		U[] values = values(cl);
		String str = valueAndUnits.toLowerCase().strip();

		for (U u : values) {
			String[] symbols = u.getSymbols();
			for (String sym : symbols) {
				String regex = "[^\\s-_@]+[\\s-_@]*\\(?" + sym + "\\)?$";
				if (str.matches(regex))
					return u;
			}
		}

		return null;
	}

	/**
	 * Strip units.
	 *
	 * @param valueAndUnits the value and units
	 * @param cl            the cl
	 * @return the string
	 */
	static String stripUnits(String valueAndUnits, Class<? extends Unit> cl) {
		Unit[] values = values(cl);
		String str = valueAndUnits.toLowerCase();

		for (Unit u : values) {
			String[] symbols = u.getSymbols();
			for (String sym : symbols) {
				String regex = "([^\\s-_@]+)[\\s-_@]*\\(?" + sym + "\\)?$";
				if (str.matches(regex))
					return str.replaceFirst(regex, "$1");
			}
		}

		return valueAndUnits;
	}
}