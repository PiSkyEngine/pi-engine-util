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
 * The Class HierarchicalProperties.
 */
class HierarchicalProperties extends Properties {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 159363954497198993L;

	/**
	 * @see java.util.Properties#getProperty(java.lang.String)
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
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        Object oldValue = super.setProperty(key, value);
        return oldValue;
    }
}