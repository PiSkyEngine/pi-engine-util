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
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class JsonSource.
 */
public class JsonSource implements ConfigSource {
    
    /** The path. */
    private final String path;
    
    /** The is classpath. */
    private final boolean isClasspath;
    
    /** The config. */
    private final Config config;
    
    /** The mapper. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
	 * Instantiates a new json source.
	 *
	 * @param path   the path
	 * @param config the config
	 */
    public JsonSource(String path, Config config) {
        this(path, false, config);
    }

    /**
	 * Instantiates a new json source.
	 *
	 * @param path        the path
	 * @param isClasspath the is classpath
	 * @param config      the config
	 */
    public JsonSource(String path, boolean isClasspath, Config config) {
        this.path = path;
        this.isClasspath = isClasspath;
        this.config = config;
    }

    /**
     * @see org.piengine.util.config.ConfigSource#load(org.piengine.util.config.HierarchicalProperties)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void load(HierarchicalProperties properties) {
        HierarchicalProperties temp = new HierarchicalProperties();
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(path) :
                new FileInputStream(path)) {
            if (is != null) {
                Map<String, Object> map = mapper.readValue(is, Map.class);
                flattenMap("", map, temp);
                temp.forEach((k, v) -> {
                    String key = k.toString();
                    String oldValue = properties.getProperty(key);
                    properties.setProperty(key, v.toString());
                    config.fireConfigEvent(key, oldValue, v.toString(), ConfigEvent.ChangeType.SET, false);
                });
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to load JSON: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#loadOverride(java.lang.String)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void loadOverride(String subProperty) {
        String overridePath = path.replaceFirst("\\.(\\w+)$", "-" + subProperty + ".$1");
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(overridePath) :
                new FileInputStream(overridePath)) {
            if (is != null) {
                Map<String, Object> map = mapper.readValue(is, Map.class);
                HierarchicalProperties overrideProps = new HierarchicalProperties();
                flattenMap("", map, overrideProps);
                overrideProps.forEach((k, v) -> {
                    String key = k.toString();
                    String oldValue = config.get(key).asString();
                    config.put(key, v.toString());
                    config.fireConfigEvent(key, oldValue, v.toString(), ConfigEvent.ChangeType.SET, false);
                });
            }
        } catch (IOException e) {
            // Silent if override not found
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#save(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void save(HierarchicalProperties properties) {
        Map<String, Object> map = unflattenProperties(properties);
        try (FileWriter writer = new FileWriter(path)) {
            mapper.writeValue(writer, map);
        } catch (IOException e) {
            throw new ConfigException("Failed to save JSON: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#merge(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void merge(HierarchicalProperties properties) {
        HierarchicalProperties existing = new HierarchicalProperties();
        try (InputStream is = new FileInputStream(path)) {
            @SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.readValue(is, Map.class);
            flattenMap("", map, existing);
        } catch (IOException e) {
            save(properties);
            return;
        }
        existing.putAll(properties);
        save(existing);
    }

    /**
	 * Flatten map.
	 *
	 * @param prefix the prefix
	 * @param map    the map
	 * @param props  the props
	 */
    @SuppressWarnings("unchecked")
	private void flattenMap(String prefix, Map<String, Object> map, Properties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, props);
            } else {
                props.setProperty(key, value.toString());
            }
        }
    }

    /**
	 * Unflatten properties.
	 *
	 * @param properties the properties
	 * @return the map
	 */
    @SuppressWarnings("unchecked")
	private Map<String, Object> unflattenProperties(Properties properties) {
        Map<String, Object> root = new java.util.HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            Map<String, Object> current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(parts[i], _ -> new java.util.HashMap<>());
            }
            current.put(parts[parts.length - 1], properties.getProperty(key));
        }
        return root;
    }
}