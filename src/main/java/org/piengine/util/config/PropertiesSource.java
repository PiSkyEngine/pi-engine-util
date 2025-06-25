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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A {@link ConfigSource} implementation for loading and saving configuration
 * properties from Java properties files. This class supports both file-based
 * and classpath resources, environment-specific overrides, and parsing of
 * schema-defined collection types (e.g., {@link List}, {@link Map}). It
 * integrates with a {@link Config} instance to manage properties and fire
 * events on updates, using a {@link ConfigSchema} for type validation when
 * defined.
 *
 * <p>
 * Properties are stored in a package-private {@link HierarchicalProperties}
 * instance, with keys following the standard dot-separated format (e.g.,
 * {@code parent.child=value}). The class supports saving and merging
 * configurations back to properties files, with special handling for formatting
 * collections as comma-separated strings.
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @since 1.0
 */
class PropertiesSource implements ConfigSource {

	/** The path to the properties file. */
	private final String path;

	/** Indicates whether the properties file is a classpath resource. */
	private final boolean isClasspath;

	/** The configuration instance managing properties. */
	private final Config config;

	/**
	 * Creates a new properties configuration source from a file path.
	 *
	 * @param path   the path to the properties file
	 * @param config the {@link Config} instance to manage properties
	 */
	public PropertiesSource(String path, Config config) {
		this(path, false, config);
	}

	/**
	 * Creates a new properties configuration source, specifying whether it is a
	 * classpath resource.
	 *
	 * @param path        the path to the properties file
	 * @param isClasspath true if the file is a classpath resource, false for a file
	 *                    system path
	 * @param config      the {@link Config} instance to manage properties
	 */
	public PropertiesSource(String path, boolean isClasspath, Config config) {
		this.path = path;
		this.isClasspath = isClasspath;
		this.config = config;
	}

	/**
	 * Loads configuration properties from the properties file into the provided
	 * properties store. Values are parsed according to schema-defined types (e.g.,
	 * {@link List}, {@link Map}), and events are fired for each loaded property.
	 *
	 * @param properties the {@link HierarchicalProperties} to store loaded
	 *                   properties
	 * @throws ConfigRuntimeException if the properties file cannot be read
	 * @see ConfigSource#load(HierarchicalProperties)
	 */
	@Override
	public void load(HierarchicalProperties properties) {
		Properties temp = new Properties();
		try (InputStream is = isClasspath
				? config.getClass().getResourceAsStream(path)
				: new FileInputStream(path)) {
			if (is != null) {
				temp.load(is);
				temp.forEach((k, v) -> {
					String key = k.toString();
					Object value = parsePropertyValue(key, v.toString());
					String oldValue = properties.getProperty(key);
					properties.put(key, value);
					config.fireConfigEvent(key, oldValue, value.toString(), ConfigEvent.ChangeType.SET, false);
				});
			}
		} catch (IOException e) {
			throw new ConfigRuntimeException("Failed to load properties: " + path, e);
		}
	}

	/**
	 * Loads environment-specific override properties from a properties file (e.g.,
	 * {@code config-prod.properties}). Overrides are applied to the {@link Config}
	 * instance, firing events for updated properties. If the override file is not
	 * found, the operation is silently ignored.
	 *
	 * @param subProperty the suffix for the override file (e.g., "prod" for
	 *                    {@code config-prod.properties})
	 * @see ConfigSource#loadOverride(String)
	 */
	@Override
	public void loadOverride(String subProperty) {
		String overridePath = path.replaceFirst("\\.(\\w+)$", "-" + subProperty + ".$1");
		try (InputStream is = isClasspath
				? config.getClass().getResourceAsStream(overridePath)
				: new FileInputStream(path)) {
			if (is != null) {
				Properties overrideProps = new Properties();
				overrideProps.load(is);
				overrideProps.forEach((k, v) -> {
					String key = k.toString();
					Object value = parsePropertyValue(key, v.toString());
					String oldValue = config.get(key).asString();
					config.putInstance(key, value);
					config.fireConfigEvent(key, oldValue, value.toString(), ConfigEvent.ChangeType.SET, false);
				});
			}
		} catch (IOException e) {
			// Silent if override not found
		}
	}

	/**
	 * Saves the provided properties to the properties file, formatting values as
	 * strings. Schema-defined collections (e.g., {@link List}, {@link Map}) are
	 * serialized as comma-separated strings.
	 *
	 * @param properties the {@link HierarchicalProperties} to save
	 * @throws ConfigRuntimeException if the properties file cannot be written
	 * @see ConfigSource#save(HierarchicalProperties)
	 */
	@Override
	public void save(HierarchicalProperties properties) {
		try (FileOutputStream fos = new FileOutputStream(path)) {
			Properties stringProps = new Properties();
			properties.forEach((k, v) -> {
				String key = k.toString();
				String value = formatPropertyValue(v);
				stringProps.setProperty(key, value);
			});
			stringProps.store(fos, "Configuration");
		} catch (IOException e) {
			throw new ConfigRuntimeException("Failed to save properties: " + path, e);
		}
	}

	/**
	 * Merges the provided properties with existing properties file content, saving
	 * the combined result. If the properties file does not exist, the provided
	 * properties are saved directly.
	 *
	 * @param properties the {@link HierarchicalProperties} to merge
	 * @throws ConfigRuntimeException if the properties file cannot be read or
	 *                                written
	 * @see ConfigSource#merge(HierarchicalProperties)
	 */
	@Override
	public void merge(HierarchicalProperties properties) {
		HierarchicalProperties existing = new HierarchicalProperties();
		try (FileInputStream fis = new FileInputStream(path)) {
			existing.load(fis);
		} catch (IOException e) {
			save(properties);
			return;
		}
		properties.forEach((k, v) -> existing.put(k, v));
		save(existing);
	}

	/**
	 * Parses a property value based on the schema-defined type. Supports
	 * {@link List} and {@link Map} types by parsing comma-separated strings or
	 * key-value pairs.
	 *
	 * @param key   the property key
	 * @param value the string value to parse
	 * @return the parsed value (e.g., String, List, Map)
	 */
	private Object parsePropertyValue(String key, String value) {
		if (config != null && config.schema != null && config.schema.isDefined(key)) {
			Class<?> type = config.schema.getType(key);
			if (List.class.isAssignableFrom(type)) {
				List<String> list = Arrays.stream(value.split("\\s*,\\s*"))
						.filter(s -> !s.isEmpty())
						.collect(Collectors.toList());
				return list;
			} else if (Map.class.isAssignableFrom(type)) {
				Map<String, String> map = new HashMap<>();
				if (!value.trim().isEmpty()) {
					String[] pairs = value.split("\\s*,\\s*");
					for (String pair : pairs) {
						String[] kv = pair.split("=");
						if (kv.length == 2) {
							map.put(kv[0].trim(), kv[1].trim());
						}
					}
				}
				return map;
			}
		}
		return value;
	}

	/**
	 * Formats a property value for saving to a properties file. Converts
	 * {@link List} and {@link Map} types to comma-separated strings or key-value
	 * pairs.
	 *
	 * @param value the value to format
	 * @return the formatted string representation
	 */
	private String formatPropertyValue(Object value) {
		if (value instanceof List<?> list) {
			return list.stream()
					.map(Object::toString)
					.collect(Collectors.joining(","));
		} else if (value instanceof Map<?, ?> map) {
			return map.entrySet().stream()
					.map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(","));
		}
		return value.toString();
	}
}