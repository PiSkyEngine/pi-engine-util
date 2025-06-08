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
 * The Class Config.
 */
public class Config {
    
    /** The properties. */
    protected final HierarchicalProperties properties;
    
    /** The schema. */
    protected ConfigSchema schema;
    
    /** The warning handler. */
    protected ConfigErrorHandler warningHandler;
    
    /** The error handler. */
    protected ConfigErrorHandler errorHandler;
    
    /** The critical handler. */
    protected ConfigErrorHandler criticalHandler;
    
    /** The sources. */
    protected final List<ConfigSource> sources;
    
    /** The listeners. */
    protected final List<ListenerEntry> listeners;

    /**
	 * Instantiates a new config.
	 */
    protected Config() {
        this.properties = new HierarchicalProperties();
        this.sources = new ArrayList<>();
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }

    /**
	 * Instantiates a new config.
	 *
	 * @param schema the schema.
	 */
    protected Config(ConfigSchema schema) {
        this();
        withSchema(schema);
    }

    /**
	 * Load.
	 *
	 * @param path the path
	 * @return the config
	 */
    public static final Config load(String path) {
        Config config = new Config();
        config.addSource(path);
        return config;
    }

    /**
	 * Load.
	 *
	 * @param <T>           the generic type
	 * @param path          the path
	 * @param configFactory the config factory
	 * @return the t
	 */
    public static final <T extends Config> T load(String path, Supplier<T> configFactory) {
        T config = configFactory.get();
        config.addSource(path);
        return config;
    }

    /**
	 * Load from classpath.
	 *
	 * @param path the path
	 * @return the config
	 */
    public static final Config loadFromClasspath(String path) {
        Config config = new Config();
        config.addClasspathSource(path);
        return config;
    }
    
    /**
	 * Load from classpath.
	 *
	 * @param <T>           the generic type
	 * @param path          the path
	 * @param configFactory the config factory
	 * @return the t
	 */
    public static final <T extends Config> T loadFromClasspath(String path, Supplier<T> configFactory) {
        T config = configFactory.get();
        config.addClasspathSource(path);
        return config;
    }

    /**
	 * With override config.
	 *
	 * @param subProperty the sub property
	 * @return the config
	 */
    public final Config withOverrideConfig(String subProperty) {
        sources.forEach(source -> source.loadOverride(subProperty));
        return this;
    }

    /**
	 * With schema.
	 *
	 * @param schema the schema
	 * @return the config
	 */
    public final Config withSchema(ConfigSchema schema) {
        this.schema = schema != null ? schema.getRootSchema() : null;
        return this;
    }

    /**
	 * With warning handler.
	 *
	 * @param handler the handler
	 * @return the config
	 */
    public final Config withWarningHandler(ConfigErrorHandler handler) {
        this.warningHandler = handler;
        return this;
    }

    /**
	 * With error handler.
	 *
	 * @param handler the handler
	 * @return the config
	 */
    public final Config withErrorHandler(ConfigErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    /**
	 * With critical handler.
	 *
	 * @param handler the handler
	 * @return the config
	 */
    public final Config withCriticalHandler(ConfigErrorHandler handler) {
        this.criticalHandler = handler;
        return this;
    }

    /**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 * @return the registration
	 */
    public final Registration addListener(ConfigListener listener) {
        return addListener(_ -> true, listener);
    }

    /**
	 * Adds the listener.
	 *
	 * @param key      the key
	 * @param listener the listener
	 * @return the registration
	 */
    public final Registration addListener(String key, ConfigListener listener) {
        return addListener(k -> k.equals(key), listener);
    }

    /**
	 * Adds the listener.
	 *
	 * @param keyFilter the key filter
	 * @param listener  the listener
	 * @return the registration
	 */
    public final Registration addListener(Predicate<String> keyFilter, ConfigListener listener) {
        ListenerEntry entry = new ListenerEntry(keyFilter, listener);
        listeners.add(entry);
        return Registration.of(() -> listeners.remove(entry));
    }

    /**
	 * Gets the.
	 *
	 * @param key the key
	 * @return the property
	 */
    public final Property get(String key) {
        if (schema != null && !schema.isDefined(key)) {
            throw new ConfigException("Property '" + key + "' not defined in schema");
        }
        return new Property(key, properties, schema, this);
    }

    /**
	 * Put.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the config
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
	 * Put instance.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the config
	 */
    public final Config putInstance(String key, Object value) {
        if (schema != null && !schema.isDefined(key)) {
            throw new ConfigException("Property '" + key + "' not defined in schema");
        }
        Object oldValue = properties.getRawProperty(key);
        properties.put(key, value);
        fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(), ConfigEvent.ChangeType.SET, true);
        return this;
    }

    /**
	 * Put list.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the config
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
                    throw new ConfigException("Element type mismatch for '" + key + "': expected " + elementType.getSimpleName());
                }
            }
        }
        Object oldValue = properties.getRawProperty(key);
        properties.put(key, value);
        fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(), ConfigEvent.ChangeType.SET, true);
        return this;
    }

    /**
	 * Put map.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the config
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
                    throw new ConfigException("Value type mismatch for '" + key + "': expected " + valueType.getSimpleName());
                }
            }
        }
        Object oldValue = properties.getRawProperty(key);
        properties.put(key, value);
        fireConfigEvent(key, oldValue != null ? oldValue.toString() : null, value.toString(), ConfigEvent.ChangeType.SET, true);
        return this;
    }

    /**
	 * Removes the.
	 *
	 * @param key the key
	 * @return the config
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
	 * Save.
	 *
	 * @param path the path
	 * @return the config
	 */
    public final Config save(String path) {
        ConfigSource source = createSource(path);
        source.save(properties);
        return this;
    }

    /**
	 * Merge.
	 *
	 * @param path the path
	 * @return the config
	 */
    public final Config merge(String path) {
        ConfigSource source = createSource(path);
        source.merge(properties);
        return this;
    }

    /**
	 * Adds the source.
	 *
	 * @param path the path
	 */
    protected void addSource(String path) {
        ConfigSource source = createSource(path);
        sources.add(source);
        source.load(properties);
    }

    /**
	 * Adds the classpath source.
	 *
	 * @param path the path
	 */
    protected void addClasspathSource(String path) {
        ConfigSource source = createClasspathSource(path);
        sources.add(source);
        source.load(properties);
    }

    /**
	 * Creates the source.
	 *
	 * @param path the path
	 * @return the config source
	 */
    protected ConfigSource createSource(String path) {
        if (path.endsWith(".properties")) {
            return new PropertiesSource(path, this);
        } else if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return new YamlSource(path, this);
        } else if (path.endsWith(".json")) {
            return new JsonSource(path, this);
        } else {
            for (String ext : new String[]{".properties", ".yaml", ".json"}) {
                if (new File(path + ext).exists()) {
                    return createSource(path + ext);
                }
            }
            throw new ConfigException("No config file found for: " + path);
        }
    }

    /**
	 * Creates the classpath source.
	 *
	 * @param path the path
	 * @return the config source
	 */
    protected ConfigSource createClasspathSource(String path) {
        if (path.endsWith(".properties")) {
            return new PropertiesSource(path, true, this);
        } else if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return new YamlSource(path, true, this);
        } else if (path.endsWith(".json")) {
            return new JsonSource(path, true, this);
        } else {
            for (String ext : new String[]{".properties", ".yaml", ".json"}) {
                if (Config.class.getClassLoader().getResource(path + ext) != null) {
                    return createClasspathSource(path + ext);
                }
            }
            throw new ConfigException("No classpath config file found for: " + path);
        }
    }

    /**
	 * Fire config event.
	 *
	 * @param key           the key
	 * @param oldValue      the old value
	 * @param newValue      the new value
	 * @param changeType    the change type
	 * @param isChangeEvent the is change event
	 */
    protected void fireConfigEvent(String key, String oldValue, String newValue, ConfigEvent.ChangeType changeType, boolean isChangeEvent) {
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
	 * Gets the warning handler.
	 *
	 * @return the warning handler
	 */
    protected final ConfigErrorHandler getWarningHandler() {
        return warningHandler;
    }

    /**
	 * Gets the error handler.
	 *
	 * @return the error handler
	 */
    protected final ConfigErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
	 * Gets the critical handler.
	 *
	 * @return the critical handler
	 */
    protected final ConfigErrorHandler getCriticalHandler() {
        return criticalHandler;
    }

    /**
	 * The Class ListenerEntry.
	 */
    protected static class ListenerEntry {
        
        /** The key filter. */
        final Predicate<String> keyFilter;
        
        /** The listener. */
        final ConfigListener listener;

        /**
		 * Instantiates a new listener entry.
		 *
		 * @param keyFilter the key filter.
		 * @param listener  the listener.
		 */
        ListenerEntry(Predicate<String> keyFilter, ConfigListener listener) {
            this.keyFilter = keyFilter;
            this.listener = listener;
        }
    }
}