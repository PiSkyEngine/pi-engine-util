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

/**
 * Represents a configuration load event. This event is triggered when a
 * configuration property is loaded from a source, providing details for
 * registered {@link ConfigListener} instances.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see ConfigEvent
 * @see ConfigListener
 */
public class ConfigLoadEvent extends ConfigEvent {

	/**
	 * Constructs a new configuration load event.
	 *
	 * @param source   the {@link Config} instance that triggered the event
	 * @param key      the configuration property key
	 * @param oldValue the previous value of the property, if any
	 * @param newValue the new value of the property
	 * @param type     the expected type of the property value
	 */
	public ConfigLoadEvent(Config source, String key, String oldValue, String newValue, Class<?> type) {
		super(source, key, oldValue, newValue, type, ChangeType.SET);
	}
}