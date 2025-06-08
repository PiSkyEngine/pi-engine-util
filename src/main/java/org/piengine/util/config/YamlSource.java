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
 * The Class YamlSource.
 */
public class YamlSource implements ConfigSource {
    
    /** The path. */
    private final String path;
    
    /** The is classpath. */
    private final boolean isClasspath;
    
    /** The config. */
    private final Config config;
    
    /** The yaml. */
    private final Yaml yaml = new Yaml();

    /**
	 * Instantiates a new yaml source.
	 *
	 * @param path   the path
	 * @param config the config
	 */
    public YamlSource(String path, Config config) {
        this(path, false, config);
    }

    /**
	 * Instantiates a new yaml source.
	 *
	 * @param path        the path
	 * @param isClasspath the is classpath
	 * @param config      the config
	 */
    public YamlSource(String path, boolean isClasspath, Config config) {
        this.path = path;
        this.isClasspath = isClasspath;
        this.config = config;
    }

    /**
     * @see org.piengine.util.config.ConfigSource#load(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void load(HierarchicalProperties properties) {
        System.out.println("YamlSource: Loading from " + path + ", schema present: " + (config.schema != null));
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(path) :
                new FileInputStream(path)) {
            if (is != null) {
                Map<String, Object> map = yaml.load(is);
                flattenMap("", map, properties);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to load YAML: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#loadOverride(java.lang.String)
     */
    @Override
    public void loadOverride(String subProperty) {
        String overridePath = path.replaceFirst("\\.(\\w+)$", "-" + subProperty + ".$1");
        try (InputStream is = isClasspath ?
                Config.class.getClassLoader().getResourceAsStream(overridePath) :
                new FileInputStream(overridePath)) {
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
     * @see org.piengine.util.config.ConfigSource#save(org.piengine.util.config.HierarchicalProperties)
     */
    @Override
    public void save(HierarchicalProperties properties) {
        Map<String, Object> map = unflattenProperties(properties);
        try (FileWriter writer = new FileWriter(path)) {
            yaml.dump(map, writer);
        } catch (IOException e) {
            throw new ConfigException("Failed to save YAML: " + path);
        }
    }

    /**
     * @see org.piengine.util.config.ConfigSource#merge(org.piengine.util.config.HierarchicalProperties)
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
     * Flatten map.
     *
     * @param prefix the prefix
     * @param map    the map
     * @param props  the props
     */
    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, HierarchicalProperties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            System.out.println("YamlSource: Flattening key=" + key + ", value=" + value + ", type=" + (value != null ? value.getClass().getSimpleName() : "null") + ", schema defined: " + (config.schema != null && config.schema.isDefined(key)));
            if (config.schema != null && config.schema.isDefined(key)) {
                Class<?> schemaType = config.schema.getType(key);
                if (Map.class.isAssignableFrom(schemaType)) {
                    System.out.println("YamlSource: Storing map for key=" + key + " (defined as Map in schema)");
                    props.put(key, new HashMap<>((Map<String, Object>) value));
                    continue;
                } else if (List.class.isAssignableFrom(schemaType)) {
                    System.out.println("YamlSource: Storing list for key=" + key + " (defined as List in schema)");
                    props.put(key, new ArrayList<>((List<?>) value));
                    continue;
                }
            }
            if (value instanceof Map) {
                System.out.println("YamlSource: Flattening nested map for key=" + key + " (undefined in schema or not a Map)");
                flattenMap(key, (Map<String, Object>) value, props);
            } else if (value instanceof List) {
                System.out.println("YamlSource: Storing list for key=" + key);
                props.put(key, new ArrayList<>((List<?>) value));
            } else {
                System.out.println("YamlSource: Storing scalar for key=" + key);
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
    private Map<String, Object> unflattenProperties(HierarchicalProperties properties) {
        Map<String, Object> root = new HashMap<>();
        properties.forEach((k, v) -> {
            String key = k.toString();
            String[] parts = key.split("\\.");
            Map<String, Object> current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(parts[i], _ -> new HashMap<>());
            }
            Object value = properties.getRawProperty(key);
            System.out.println("YamlSource: Unflattening key=" + key + ", value=" + value + ", type=" + (value != null ? value.getClass().getSimpleName() : "null"));
            current.put(parts[parts.length - 1], value);
        });
        return root;
    }
}