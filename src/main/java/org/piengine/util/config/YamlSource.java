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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * A {@link ConfigSource} implementation for loading and saving configuration properties
 * from YAML files. This class supports both file-based and classpath resources, hierarchical
 * property resolution, and environment-specific overrides. It integrates with a {@link Config}
 * instance to manage properties and fire events on updates, using a {@link ConfigSchema} for
 * type validation when defined.
 *
 * <p>Properties are stored in a package-private {@link HierarchicalProperties} instance,
 * with nested YAML objects flattened into dot-separated keys (e.g., {@code parent.child=value}).
 * The class supports saving and merging configurations back to YAML files, preserving schema-defined
 * types for collections like {@link List} and {@link Map}.</p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @since 1.0
 */
class YamlSource implements ConfigSource {

	/** The path to the YAML file. */
	private final String path;

	/** Indicates whether the YAML file is a classpath resource. */
	private final boolean isClasspath;

	/** The configuration instance managing properties. */
	private final Config config;

	/** The YAML parser and serializer. */
	private final Yaml yaml = new Yaml();

	/**
	 * Creates a new YAML configuration source from a file path.
	 *
	 * @param path   the path to the YAML file
	 * @param config the {@link Config} instance to manage properties
	 */
	public YamlSource(String path, Config config) {
		this(path, false, config);
	}

	/**
	 * Creates a new YAML configuration source, specifying whether it is a classpath resource.
	 *
	 * @param path        the path to the YAML file
	 * @param isClasspath true if the file is a classpath resource, false for a file system path
	 * @param config      the {@link Config} instance to manage properties
	 */
	public YamlSource(String path, boolean isClasspath, Config config) {
		this.path = path;
		this.isClasspath = isClasspath;
		this.config = config;
	}

	/**
	 * Loads configuration properties from the YAML file into the provided properties store.
	 * Nested YAML objects are flattened into dot-separated keys, and schema-defined types
	 * (e.g., {@link List}, {@link Map}) are preserved.
	 *
	 * @param properties the {@link HierarchicalProperties} to store loaded properties
	 * @throws ConfigException if the YAML file cannot be read or parsed
	 * @see ConfigSource#load(HierarchicalProperties)
	 */
	@Override
	public void load(HierarchicalProperties properties) {
		try (InputStream is = isClasspath ? Config.class.getClassLoader().getResourceAsStream(path)
				: new FileInputStream(path)) {
			if (is != null) {
				Map<String, Object> map = yaml.load(is);
				flattenMap("", map, properties);
			}
		} catch (IOException e) {
			throw new ConfigException("Failed to load YAML: " + path, e);
		}
	}

	/**
	 * Loads environment-specific override properties from a YAML file (e.g., {@code config-prod.yaml}).
	 * Overrides are applied to the {@link Config} instance, firing events for updated properties.
	 * If the override file is not found, the operation is silently ignored.
	 *
	 * @param subProperty the suffix for the override file (e.g., "prod" for {@code config-prod.yaml})
	 * @see ConfigSource#loadOverride(String)
	 */
	@Override
	public void loadOverride(String subProperty) {
		String overridePath = path.replaceFirst("\\.(\\w+)$", "-" + subProperty + ".$1");
		try (InputStream is = isClasspath ? Config.class.getClassLoader().getResourceAsStream(overridePath)
				: new FileInputStream(overridePath)) {
			if (is != null) {
				Map<String, Object> map = yaml.load(is);
				HierarchicalProperties overrideProps = new HierarchicalProperties();
				flattenMap("", map, overrideProps);
				overrideProps.forEach((k, v) -> {
					String key = k.toString();
					String oldValue = config.get(key).asString();
					config.putInstance(key, v);
					config.fireConfigEvent(key, oldValue, v.toString(), ConfigEvent.ChangeType.SET, false);
				});
			}
		} catch (IOException e) {
			// Silent if override not found
		}
	}

	/**
	 * Saves the provided properties to the YAML file, unflattening dot-separated keys into
	 * nested YAML objects. Schema-defined types for collections are preserved.
	 *
	 * @param properties the {@link HierarchicalProperties} to save
	 * @throws ConfigException if the YAML file cannot be written
	 * @see ConfigSource#save(HierarchicalProperties)
	 */
	@Override
	public void save(HierarchicalProperties properties) {
		Map<String, Object> map = unflattenProperties(properties);
		try (FileWriter writer = new FileWriter(path)) {
			yaml.dump(map, writer);
		} catch (IOException e) {
			throw new ConfigException("Failed to save YAML: " + path, e);
		}
	}

	/**
	 * Merges the provided properties with existing YAML file content, saving the combined result.
	 * If the YAML file does not exist, the provided properties are saved directly.
	 *
	 * @param properties the {@link HierarchicalProperties} to merge
	 * @throws ConfigException if the YAML file cannot be read or written
	 * @see ConfigSource#merge(HierarchicalProperties)
	 */
	@Override
	public void merge(HierarchicalProperties properties) {
		HierarchicalProperties existing = new HierarchicalProperties();
		try (InputStream is = new FileInputStream(path)) {
			Map<String, Object> map = yaml.load(is);
			flattenMap("", map, existing);
		} catch (IOException e) {
			save(properties);
			return;
		}
		properties.forEach((k, v) -> existing.put(k, v));
		save(existing);
	}

	/**
	 * Flattens a nested YAML map into dot-separated property keys for storage.
	 * Schema-defined types (e.g., {@link List}, {@link Map}) are preserved as objects,
	 * while other values are converted to strings.
	 *
	 * @param prefix the current key prefix (e.g., "parent" for "parent.child")
	 * @param map    the YAML map to flatten
	 * @param props  the {@link HierarchicalProperties} to store flattened properties
	 */
	@SuppressWarnings("unchecked")
	private void flattenMap(String prefix, Map<String, Object> map, HierarchicalProperties props) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			Object value = entry.getValue();
			if (config.schema != null && config.schema.isDefined(key)) {
				Class<?> schemaType = config.schema.getType(key);
				if (Map.class.isAssignableFrom(schemaType)) {
					props.put(key, new HashMap<>((Map<String, Object>) value));
					continue;
				} else if (List.class.isAssignableFrom(schemaType)) {
					props.put(key, new ArrayList<>((List<?>) value));
					continue;
				}
			}
			if (value instanceof Map) {
				flattenMap(key, (Map<String, Object>) value, props);
			} else if (value instanceof List) {
				props.put(key, new ArrayList<>((List<?>) value));
			} else {
				props.setProperty(key, value.toString());
			}
		}
	}

	/**
	 * Unflattens dot-separated property keys into a nested YAML map for saving.
	 * Schema-defined types are preserved as objects in the resulting map.
	 *
	 * @param properties the {@link HierarchicalProperties} to unflatten
	 * @return the nested map representing the YAML structure
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> unflattenProperties(HierarchicalProperties properties) {
		Map<String, Object> root = new HashMap<>();
		properties.forEach((k, _) -> {
			String key = k.toString();
			String[] parts = key.split("\\.");
			Map<String, Object> current = root;
			for (int i = 0; i < parts.length - 1; i++) {
				current = (Map<String, Object>) current.computeIfAbsent(parts[i], _ -> new HashMap<>());
			}
			Object value = properties.getRawProperty(key);
			current.put(parts[parts.length - 1], value);
		});
		return root;
	}
}