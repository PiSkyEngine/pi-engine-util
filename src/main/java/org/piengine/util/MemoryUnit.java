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

import java.math.BigInteger;

import org.piengine.util.UnitUtils.ConvertableUnit;


/**
 * Enumeration of memory size units. Storage units are always based on 1024 byte
 * multiples.
 * 
 * <p>
 * Note that a 64-bit long overflows after {@link #TERABYTES} bytes, thus other
 * higher units of PETABYTES and EXABYTES may have limited precision. Future
 * extensions may convert to {@link BigInteger} for storage, especially for the
 * larger units.
 * </p>
 *
 * @author Mark Bednarczyk
 */
public enum MemoryUnit implements ConvertableUnit<MemoryUnit>, Unit {

	/** Represents bits. */
	BITS("bit") {
		@Override
		public long convert(long size, MemoryUnit sourceUnit) {
			return sourceUnit.toBits(size);
		}

		@Override
		public long toBits(long size) {
			return size;
		}

		@Override
		public long toBytes(long size) {
			return size / 8;
		}
	},

	/** Represents bytes. */
	BYTES("b", "byte"),

	/** Represents kilobytes (1024 bytes). */
	KILOBYTES("kb", "kbytes", "kilo"),

	/** Represents megabytes (1,048,576 bytes). */
	MEGABYTES("mb", "mbytes", "mega"),

	/** Represents gigabytes (1,073,741,824 bytes). */
	GIGABYTES("gb", "gbytes", "gig", "giga"),

	/** Represents terabytes (1,099,511,627,776 bytes). */
	TERABYTES("tb", "tbytes", "tera"),

	/** Represents petabytes (1,125,899,906,842,624 bytes). */
	PETABYTES("pb", "pbytes", "peta"),

	/** Represents exabytes (1,152,921,504,606,846,976 bytes). */
	EXABYTES("eb", "ebytes", "exa");

	/**
	 * Formats the given number of bytes according to the specified format string.
	 *
	 * @param fmt     The format string
	 * @param inBytes The number of bytes
	 * @return The formatted string
	 */
	public static String format(String fmt, long inBytes) {
		return UnitUtils.format(fmt, inBytes, MemoryUnit.class, BYTES);
	}
	/**
	 * Finds the nearest MemoryUnit for the given number of bytes.
	 *
	 * @param inBytes The number of bytes
	 * @return The nearest MemoryUnit
	 */
	public static MemoryUnit nearest(long inBytes) {
		return UnitUtils.nearest(inBytes, MemoryUnit.class, BYTES);
	}
	
	/** The base. */
	private final long base;

	/** The basef. */
	private final double basef;

	/** The symbols. */
	private final String[] symbols;

	/**
	 * Constructs a MemoryUnit with the given symbols.
	 *
	 * @param symbols The symbols representing this unit
	 */
	MemoryUnit(String... symbols) {
		final int ordinal = ordinal() - 1;
		long t = 1;

		for (int i = 0; i < ordinal; i++) {
			t *= 1024;
		}

		this.base = t;
		this.basef = t;
		this.symbols = symbols != null ? symbols : new String[] { "" + name().charAt(0) };
	}

	/**
	 * Converts the given value from the source unit to this unit.
	 *
	 * @param size       The value to convert
	 * @param sourceUnit The source unit
	 * @return The converted value in this unit
	 */
	@Override
	public long convert(long size, MemoryUnit sourceUnit) {
		return sourceUnit.toBytes(size) / this.base;
	}

	/**
	 * Converts the given value in bytes to this unit.
	 *
	 * @param inBytes The value in bytes
	 * @return The converted value in this unit
	 */
	@Override
	public double convertf(double inBytes) {
		return convertf(inBytes, MemoryUnit.BYTES);
	}

	/**
	 * Converts the given value from the source unit to this unit.
	 *
	 * @param size       The value to convert
	 * @param sourceUnit The source unit
	 * @return The converted value in this unit
	 */
	@Override
	public double convertf(double size, MemoryUnit sourceUnit) {
		return sourceUnit.toBytesAsDouble(size) / this.base;
	}

	/**
	 * Gets the primary symbol for this unit.
	 *
	 * @return The primary symbol
	 */
	@Override
	public String getSymbol() {
		return symbols.length == 0 ? name() : symbols[0];
	}

	/**
	 * Gets all symbols for this unit.
	 *
	 * @return An array of symbols
	 */
	@Override
	public String[] getSymbols() {
		return symbols;
	}

	/**
	 * Converts the given value to the base unit (bytes).
	 *
	 * @param value The value to convert
	 * @return The value in bytes
	 */
	@Override
	public long toBase(long value) {
		return toBytes(value);
	}

	/**
	 * Converts the given size to bits.
	 *
	 * @param size The size to convert
	 * @return The size in bits
	 */
	public long toBits(long size) {
		return (toBytes(size) * 8);
	}

	/**
	 * Converts the given size to bits as an integer.
	 *
	 * @param size The size to convert
	 * @return The size in bits as an integer
	 * @throws IllegalArgumentException if the result would overflow an integer
	 */
	public int toBitsAsInt(long size) {
		long bits = (toBytes(size) * 8);

		if (bits > Integer.MAX_VALUE)
			throw new IllegalArgumentException("integer overflow on conversion from long to int");

		return (int) bits;
	}

	/**
	 * Converts the given size to bytes.
	 *
	 * @param size The size to convert
	 * @return The size in bytes
	 */
	public long toBytes(long size) {
		return (size * base);
	}

	/**
	 * To bytes as double.
	 *
	 * @param size the size
	 * @return the double
	 */
	private double toBytesAsDouble(double size) {
		return size * basef;
	}

	/**
	 * Converts the given size to bytes as an integer.
	 *
	 * @param size The size to convert
	 * @return The size in bytes as an integer
	 * @throws IllegalArgumentException if the result would overflow an integer
	 */
	public int toBytesAsInt(long size) {
		if (size > Integer.MAX_VALUE)
			throw new IllegalArgumentException("integer overflow on conversion from long to int");

		return (int) (size * base);
	}

	/**
	 * Converts the given size to gigabytes.
	 *
	 * @param size The size to convert
	 * @return The size in gigabytes
	 */
	public long toGigabytes(long size) {
		return GIGABYTES.convert(size, this);
	}

	/**
	 * Converts the given size to kilobytes.
	 *
	 * @param size The size to convert
	 * @return The size in kilobytes
	 */
	public long toKilo(long size) {
		return KILOBYTES.convert(size, this);
	}

	/**
	 * Converts the given size to megabytes.
	 *
	 * @param size The size to convert
	 * @return The size in megabytes
	 */
	public long toMegabytes(long size) {
		return MEGABYTES.convert(size, this);
	}

	/**
	 * Converts the given size to terabytes.
	 *
	 * @param size The size to convert
	 * @return The size in terabytes
	 */
	public long toTerabytes(long size) {
		return TERABYTES.convert(size, this);
	}
}