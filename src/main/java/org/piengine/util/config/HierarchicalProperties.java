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
package org.piengine.util.config;

import java.util.Properties;

/**
 * Package-private extension of {@link Properties} for storing configuration
 * data in a configuration system. This class supports hierarchical property
 * resolution by checking parent keys in a dot-separated namespace, used
 * internally by {@link Config} and {@link ConfigSource}.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
class HierarchicalProperties extends Properties {

	/** The serial version UID for serialization. */
	private static final long serialVersionUID = 159363954497198993L;

	/**
	 * Retrieves a property value, checking parent keys in the namespace if not
	 * found.
	 *
	 * @param key the property key
	 * @return the property value, or {@code null} if not found
	 * @see Properties#getProperty(String)
	 */
	@Override
	public String getProperty(String key) {
		String value = super.getProperty(key);
		if (value != null) {
			return value;
		}
		// Check parent packages
		int lastDot = key.lastIndexOf('.');
		while (lastDot > 0) {
			key = key.substring(0, lastDot);
			value = super.getProperty(key);
			if (value != null) {
				return value;
			}
			lastDot = key.lastIndexOf('.');
		}
		return null;
	}

	/**
	 * Retrieves a raw property value, checking parent keys if not found.
	 *
	 * @param key the property key
	 * @return the raw property value, or {@code null} if not found
	 */
	public Object getRawProperty(String key) {
		Object value = super.get(key);
		if (value != null) {
			return value;
		}
		// Check parent packages
		int lastDot = key.lastIndexOf('.');
		while (lastDot > 0) {
			key = key.substring(0, lastDot);
			value = super.get(key);
			if (value != null) {
				return value;
			}
			lastDot = key.lastIndexOf('.');
		}
		return null;
	}

	/**
	 * Sets a property value.
	 *
	 * @param key   the property key
	 * @param value the property value
	 * @return the previous value, or {@code null} if none
	 * @see Properties#setProperty(String, String)
	 */
	@Override
	public synchronized Object setProperty(String key, String value) {
		return super.setProperty(key, value);
	}
}