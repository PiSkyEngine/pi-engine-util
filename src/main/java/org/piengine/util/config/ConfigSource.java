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

import java.io.IOException;

/**
 * Defines a source for loading and saving configuration data in a configuration
 * system supporting YAML, JSON, and properties file formats. Implementations
 * handle interaction with configuration sources, such as files or classpath
 * resources, and integrate with the package-private
 * {@link HierarchicalProperties} for data storage.
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see Config
 */
interface ConfigSource {

	/**
	 * Loads configuration data from the source into the provided properties.
	 *
	 * @param properties the internal {@link HierarchicalProperties} to load data
	 *                   into
	 * @throws ConfigNotFound
	 */
	void load(HierarchicalProperties properties) throws ConfigNotFound, IOException;

	/**
	 * Loads override configuration data for a specific sub-property.
	 *
	 * @param subProperty the sub-property to override (e.g., a profile or context)
	 * @throws ConfigNotFound
	 * @throws IOException
	 */
	void loadOverride(String subProperty) throws ConfigNotFound, IOException;

	/**
	 * Saves the provided properties to the configuration source.
	 *
	 * @param properties the internal {@link HierarchicalProperties} to save
	 */
	void save(HierarchicalProperties properties) throws IOException;

	/**
	 * Merges configuration data from the source into the provided properties.
	 *
	 * @param properties the internal {@link HierarchicalProperties} to merge data
	 *                   into
	 */
	void merge(HierarchicalProperties properties) throws IOException;
}