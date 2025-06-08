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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.piengine.util.Registration;

/**
 * Manages configuration data in a configuration system supporting YAML, JSON,
 * and properties file formats. This class serves as the central interface for
 * loading, storing, and accessing configuration properties in a type-safe
 * manner. It supports multiple configuration sources, schema validation via
 * {@link ConfigSchema}, and event-driven updates through
 * {@link ConfigListener}, enabling runtime customization for any application.
 *
 * <p>
 * Configuration data is stored internally in a package-private hierarchical
 * properties structure, allowing nested property resolution. The class provides
 * methods for setting and retrieving properties via {@link Property},
 * configuring error handlers, and registering listeners for real-time
 * configuration changes.
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 * @see ConfigSchema
 * @see Property
 * @see ConfigSource
 * @see ConfigListener
 */
public class Config {

	/**
	 * The internal hierarchical properties storing the configuration data
	 * (package-private).
	 */
	protected final HierarchicalProperties properties;

	/** The schema defining property types and constraints. */
	protected ConfigSchema schema;

	/** The handler for warning-level configuration errors. */
	protected ConfigErrorHandler warningHandler;

	/** The handler for error-level configuration errors. */
	protected ConfigErrorHandler errorHandler;

	/** The handler for critical-level configuration errors. */
	protected ConfigErrorHandler criticalHandler;

	/** The list of configuration sources (e.g., files, classpath resources). */
	protected final List<ConfigSource> sources;

	/** The list of registered configuration change listeners. */
	protected final List<ListenerEntry> listeners;

	/**
	 * Constructs a new {@code Config} instance with an empty internal properties
	 * store.
	 */
	public Config() {
		this.properties = new HierarchicalProperties();
		this.sources = new ArrayList<>();
		this.listeners = Collections.synchronizedList(new ArrayList<>());
	}

	/**
	 * Constructs a new {@code Config} instance with the specified schema.
	 *
	 * @param schema the {@link ConfigSchema} defining property types and
	 *               constraints
	 */
	protected Config(ConfigSchema schema) {
		this();
		withSchema(schema);
	}

	/**
	 * Loads a configuration from a file at the specified path, supporting YAML,
	 * JSON, or properties formats.
	 *
	 * @param path the path to the configuration file
	 * @return a new {@code Config} instance with the loaded configuration
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	public static final Config load(String path) {
		Config config = new Config();
		config.addSource(path);
		return config;
	}

	/**
	 * Loads a configuration from a file using a custom config factory.
	 *
	 * @param <T>           the type of the config instance
	 * @param path          the path to the configuration file
	 * @param configFactory the factory to create the config instance
	 * @return the config instance with the loaded configuration
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	public static final <T extends Config> T load(String path, Supplier<T> configFactory) {
		T config = configFactory.get();
		config.addSource(path);
		return config;
	}

	/**
	 * Loads a configuration from a classpath resource at the specified path.
	 *
	 * @param path the path to the classpath resource
	 * @return a new {@code Config} instance with the loaded configuration
	 * @throws ConfigException if the resource is not found or has an unsupported
	 *                         format
	 */
	public static final Config loadFromClasspath(String path) {
		Config config = new Config();
		config.addClasspathSource(path);
		return config;
	}

	/**
	 * Loads a configuration from a classpath resource using a custom config
	 * factory.
	 *
	 * @param <T>           the type of the config instance
	 * @param path          the path to the classpath resource
	 * @param configFactory the factory to create the config instance
	 * @return the config instance with the loaded configuration
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	public static final <T extends Config> T loadFromClasspath(String path, Supplier<T> configFactory) {
		T config = configFactory.get();
		config.addClasspathSource(path);
		return config;
	}

	/**
	 * Applies an override configuration for a specific sub-property across all
	 * sources.
	 *
	 * @param subProperty the sub-property to override (e.g., a profile or context)
	 * @return this {@code Config} instance for method chaining
	 */
	public final Config withOverrideConfig(String subProperty) {
		sources.forEach(source -> source.loadOverride(subProperty));
		return this;
	}

	/**
	 * Sets the schema for validating configuration properties.
	 *
	 * @param schema the {@link ConfigSchema} to use, or {@code null} to disable
	 *               schema validation
	 * @return this {@code Config} instance for method chaining
	 */
	public final Config withSchema(ConfigSchema schema) {
		this.schema = schema != null ? schema.getRootSchema() : null;
		return this;
	}

	/**
	 * Sets the handler for warning-level configuration errors.
	 *
	 * @param handler the {@link ConfigErrorHandler} for warnings
	 * @return this {@code Config} instance for method chaining
	 */
	public final Config withWarningHandler(ConfigErrorHandler handler) {
		this.warningHandler = handler;
		return this;
	}

	/**
	 * Sets the handler for error-level configuration errors.
	 *
	 * @param handler the {@link ConfigErrorHandler} for errors
	 * @return this {@code Config} instance for method chaining
	 */
	public final Config withErrorHandler(ConfigErrorHandler handler) {
		this.errorHandler = handler;
		return this;
	}

	/**
	 * Sets the handler for critical-level configuration errors.
	 *
	 * @param handler the {@link ConfigErrorHandler} for critical errors
	 * @return this {@code Config} instance for method chaining
	 */
	public final Config withCriticalHandler(ConfigErrorHandler handler) {
		this.criticalHandler = handler;
		return this;
	}

	/**
	 * Registers a listener to receive configuration change events for all
	 * properties.
	 *
	 * @param listener the {@link ConfigListener} to register
	 * @return a {@link Registration} for managing the listener's lifecycle
	 */
	public final Registration addListener(ConfigListener listener) {
		return addListener(_ -> true, listener);
	}

	/**
	 * Registers a listener to receive configuration change events for a specific
	 * property key.
	 *
	 * @param key      the property key to monitor
	 * @param listener the {@link ConfigListener} to register
	 * @return a {@link Registration} for managing the listener's lifecycle
	 */
	public final Registration addListener(String key, ConfigListener listener) {
		return addListener(k -> k.equals(key), listener);
	}

	/**
	 * Registers a listener to receive configuration change events for properties
	 * matching a filter.
	 *
	 * @param keyFilter the filter to select properties
	 * @param listener  the {@link ConfigListener} to register
	 * @return a {@link Registration} for managing the listener's lifecycle
	 */
	public final Registration addListener(Predicate<String> keyFilter, ConfigListener listener) {
		ListenerEntry entry = new ListenerEntry(keyFilter, listener);
		listeners.add(entry);
		return Registration.of(() -> listeners.remove(entry));
	}

	/**
	 * Retrieves a property instance for the specified key, enabling type-safe
	 * access.
	 *
	 * @param key the property key
	 * @return a {@link Property} instance for accessing the property's value
	 * @throws ConfigException if the key is not defined in the schema
	 */
	public final Property get(String key) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		return new Property(key, properties, schema, this);
	}

	/**
	 * Sets a property value, converting it to a string representation.
	 *
	 * @param key   the property key
	 * @param value the property value
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the key is not defined in the schema
	 */
	public final Config put(String key, Object value) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		String oldValue = properties.getProperty(key);
		properties.setProperty(key, value.toString());
		fireConfigEvent(key, oldValue, value.toString(), ConfigEvent.ChangeType.SET, true);
		return this;
	}

	/**
	 * Sets a property value as a raw instance, preserving its object form.
	 *
	 * @param key   the property key
	 * @param value the property value
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the key is not defined in the schema
	 */
	public final Config putInstance(String key, Object value) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		Object oldValue = properties.getRawProperty(key);
		properties.put(key, value);
		fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(),
				ConfigEvent.ChangeType.SET, true);
		return this;
	}

	/**
	 * Sets a property value as a list, validating element types against the schema.
	 *
	 * @param key   the property key
	 * @param value the list value
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the key is not defined, not a list type, or
	 *                         elements mismatch the schema
	 */
	public final Config putList(String key, List<?> value) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		if (schema != null && !List.class.isAssignableFrom(schema.getType(key))) {
			throw new ConfigException("Property '" + key + "' is not a List type");
		}
		if (schema != null && schema.getElementType(key) != null) {
			Class<?> elementType = schema.getElementType(key);
			for (Object item : value) {
				if (item != null && !elementType.isAssignableFrom(item.getClass())) {
					throw new ConfigException("Element type mismatch for '" + key + "': expected " + elementType
							.getSimpleName());
				}
			}
		}
		Object oldValue = properties.getRawProperty(key);
		properties.put(key, value);
		fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(),
				ConfigEvent.ChangeType.SET, true);
		return this;
	}

	/**
	 * Sets a property value as a map, validating value types against the schema.
	 *
	 * @param key   the property key
	 * @param value the map value with string keys
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the key is not defined, not a map type, or values
	 *                         mismatch the schema
	 */
	public final Config putMap(String key, Map<String, ?> value) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		if (schema != null && !Map.class.isAssignableFrom(schema.getType(key))) {
			throw new ConfigException("Property '" + key + "' is not a Map type");
		}
		if (schema != null && schema.getElementType(key) != null) {
			Class<?> valueType = schema.getElementType(key);
			for (Object item : value.values()) {
				if (item != null && !valueType.isAssignableFrom(item.getClass())) {
					throw new ConfigException("Value type mismatch for '" + key + "': expected " + valueType
							.getSimpleName());
				}
			}
		}
		Object oldValue = properties.getRawProperty(key);
		properties.put(key, value);
		fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(),
				ConfigEvent.ChangeType.SET, true);
		return this;
	}

	/**
	 * Removes a property from the configuration.
	 *
	 * @param key the property key
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the key is not defined in the schema
	 */
	public final Config remove(String key) {
		if (schema != null && !schema.isDefined(key)) {
			throw new ConfigException("Property '" + key + "' not defined in schema");
		}
		String oldValue = properties.getProperty(key);
		if (oldValue != null) {
			properties.remove(key);
			fireConfigEvent(key, oldValue, null, ConfigEvent.ChangeType.REMOVE, true);
		}
		return this;
	}

	/**
	 * Saves the current configuration to a file at the specified path.
	 *
	 * @param path the path to save the configuration file
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the file cannot be written or has an unsupported
	 *                         format
	 */
	public final Config save(String path) {
		ConfigSource source = createSource(path);
		source.save(properties);
		return this;
	}

	/**
	 * Merges configuration data from a file into the current configuration.
	 *
	 * @param path the path to the configuration file
	 * @return this {@code Config} instance for method chaining
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	public final Config merge(String path) {
		ConfigSource source = createSource(path);
		source.merge(properties);
		return this;
	}

	/**
	 * Adds a configuration source from a file at the specified path.
	 *
	 * @param path the path to the configuration file
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	protected void addSource(String path) {
		ConfigSource source = createSource(path);
		sources.add(source);
		source.load(properties);
	}

	/**
	 * Adds a configuration source from a classpath resource at the specified path.
	 *
	 * @param path the path to the classpath resource
	 * @throws ConfigException if the resource is not found or has an unsupported
	 *                         format
	 */
	protected void addClasspathSource(String path) {
		ConfigSource source = createClasspathSource(path);
		sources.add(source);
		source.load(properties);
	}

	/**
	 * Creates a configuration source for a file at the specified path.
	 *
	 * @param path the path to the configuration file
	 * @return the {@link ConfigSource} instance
	 * @throws ConfigException if the file is not found or has an unsupported format
	 */
	protected ConfigSource createSource(String path) {
		if (path.endsWith(".properties")) {
			return new PropertiesSource(path, this);
		} else if (path.endsWith(".yaml") || path.endsWith(".yml")) {
			return new YamlSource(path, this);
		} else if (path.endsWith(".json")) {
			return new JsonSource(path, this);
		} else {
			for (String ext : new String[] {
					".properties",
					".yaml",
					".json"
			}) {
				if (new File(path + ext).exists()) {
					return createSource(path + ext);
				}
			}
			throw new ConfigException("No config file found for: " + path);
		}
	}

	/**
	 * Creates a configuration source for a classpath resource at the specified
	 * path.
	 *
	 * @param path the path to the classpath resource
	 * @return the {@link ConfigSource} instance
	 * @throws ConfigException if the resource is not found or has an unsupported
	 *                         format
	 */
	protected ConfigSource createClasspathSource(String path) {
		if (path.endsWith(".properties")) {
			return new PropertiesSource(path, true, this);
		} else if (path.endsWith(".yaml") || path.endsWith(".yml")) {
			return new YamlSource(path, true, this);
		} else if (path.endsWith(".json")) {
			return new JsonSource(path, true, this);
		} else {
			for (String ext : new String[] {
					".properties",
					".yaml",
					".json"
			}) {
				if (Config.class.getClassLoader().getResource(path + ext) != null) {
					return createClasspathSource(path + ext);
				}
			}
			throw new ConfigException("No classpath config file found for: " + path);
		}
	}

	/**
	 * Notifies registered listeners of a configuration change event.
	 *
	 * @param key           the property key
	 * @param oldValue      the previous value, if any
	 * @param newValue      the new value
	 * @param changeType    the type of change (set or remove)
	 * @param isChangeEvent whether this is a change event (true) or load event
	 *                      (false)
	 */
	protected void fireConfigEvent(String key, String oldValue, String newValue, ConfigEvent.ChangeType changeType,
			boolean isChangeEvent) {
		Class<?> type = schema != null && schema.isDefined(key) ? schema.getType(key) : String.class;
		ConfigEvent event = isChangeEvent
				? new ConfigChangeEvent(this, key, oldValue, newValue, type, changeType)
				: new ConfigLoadEvent(this, key, oldValue, newValue, type);
		synchronized (listeners) {
			listeners.stream()
					.filter(entry -> entry.keyFilter.test(key))
					.forEach(entry -> entry.listener.onConfigChanged(event));
		}
	}

	/**
	 * Retrieves the warning error handler.
	 *
	 * @return the {@link ConfigErrorHandler} for warnings, or {@code null} if not
	 *         set
	 */
	protected final ConfigErrorHandler getWarningHandler() {
		return warningHandler;
	}

	/**
	 * Retrieves the error handler.
	 *
	 * @return the {@link ConfigErrorHandler} for errors, or {@code null} if not set
	 */
	protected final ConfigErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Retrieves the critical error handler.
	 *
	 * @return the {@link ConfigErrorHandler} for critical errors, or {@code null}
	 *         if not set
	 */
	protected final ConfigErrorHandler getCriticalHandler() {
		return criticalHandler;
	}

	/**
	 * Inner class representing a configuration listener entry with a key filter.
	 */
	protected static class ListenerEntry {

		/** The filter for selecting properties to monitor. */
		final Predicate<String> keyFilter;

		/** The listener to notify of configuration changes. */
		final ConfigListener listener;

		/**
		 * Constructs a new listener entry.
		 *
		 * @param keyFilter the filter for selecting properties
		 * @param listener  the {@link ConfigListener} to notify
		 */
		ListenerEntry(Predicate<String> keyFilter, ConfigListener listener) {
			this.keyFilter = keyFilter;
			this.listener = listener;
		}
	}
}