package org.piengine.util.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigListMapTest {

    private static final String PROP_CONTENT = """
            app.servers=server1,server2,server3
            app.settings=key1=value1,key2=value2,key3=value3
            """;

    private static final String YAML_CONTENT = """
            app:
              servers:
                - server1
                - server2
                - server3
              settings:
                key1: value1
                key2: value2
                key3: value3
            """;

    private static final String JSON_CONTENT = """
            {
              "app": {
                "servers": ["server1", "server2", "server3"],
                "settings": {
                  "key1": "value1",
                  "key2": "value2",
                  "key3": "value3"
                }
              }
            }
            """;

    @TempDir
    Path tempDir;
    private Path propFile;
    private Path yamlFile;
    private Path jsonFile;

    @BeforeEach
    void setUp() throws IOException {
        propFile = tempDir.resolve("config.properties");
        yamlFile = tempDir.resolve("config.yaml");
        jsonFile = tempDir.resolve("config.json");

        Files.writeString(propFile, PROP_CONTENT);
        Files.writeString(yamlFile, YAML_CONTENT);
        Files.writeString(jsonFile, JSON_CONTENT);

        // Clear system properties
        System.clearProperty("app.servers");
        System.clearProperty("app.settings");
    }

    @Test
    void testLoadListProperties() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList());
        Config config = new Config().withSchema(schema);
        config.addSource(propFile.toString());

        List<String> servers = config.get("app.servers").asList(String.class);
        assertEquals(Arrays.asList("server1", "server2", "server3"), servers);
    }

    @Test
    void testLoadListYaml() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList());
        Config config = new Config().withSchema(schema);
        config.addSource(yamlFile.toString());

        List<String> servers = config.get("app.servers").asList(String.class);
        assertEquals(Arrays.asList("server1", "server2", "server3"), servers);
    }

    @Test
    void testLoadListJson() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        List<String> servers = config.get("app.servers").asList(String.class);
        assertEquals(Arrays.asList("server1", "server2", "server3"), servers);
    }

    @Test
    void testLoadMapProperties() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(propFile.toString());

        Map<String, String> settings = config.get("app.settings").asMap(String.class);
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        assertEquals(expected, settings);
    }

    @Test
    void testLoadMapYaml() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(yamlFile.toString());

        Map<String, String> settings = config.get("app.settings").asMap(String.class);
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        assertEquals(expected, settings);
    }

    @Test
    void testLoadMapJson() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        Map<String, String> settings = config.get("app.settings").asMap(String.class);
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        assertEquals(expected, settings);
    }

    @Test
    void testListSchemaConstraints() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Arrays.asList("default")).critical()
                .defineList("app.ports", Integer.class, Collections.emptyList()).warning();
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        assertEquals(Arrays.asList("server1", "server2", "server3"), config.get("app.servers").asList(String.class));

        // Type mismatch
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.servers").asList(Integer.class));

        // Undefined key
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.unknown").asList(String.class));
    }

    @Test
    void testMapSchemaConstraints() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap()).critical()
                .defineMap("app.configs", Integer.class, Collections.emptyMap()).warning();
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");
        assertEquals(expected, config.get("app.settings").asMap(String.class));

        // Type mismatch
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.settings").asMap(Integer.class));

        // Undefined key
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.unknown").asMap(String.class));
    }

    @Test
    void testListErrorHandlers() throws ConfigNotFound, IOException {
        AtomicInteger warnings = new AtomicInteger();
        AtomicInteger criticals = new AtomicInteger();

        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList()).critical()
                .defineList("app.ports", Integer.class, Collections.emptyList()).warning();
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());
        config.withWarningHandler((key, msg, critical) -> warnings.incrementAndGet())
              .withCriticalHandler((key, msg, critical) -> criticals.incrementAndGet());

        // Trigger warning
        config.get("app.ports").asWarning().asList(String.class);
        assertEquals(1, warnings.get());

        // Trigger critical
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.servers").asCritical().asList(Integer.class));
        assertEquals(1, criticals.get());
    }

    @Test
    void testMapErrorHandlers() throws ConfigNotFound, IOException {
        AtomicInteger warnings = new AtomicInteger();
        AtomicInteger criticals = new AtomicInteger();

        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap()).critical()
                .defineMap("app.configs", Integer.class, Collections.emptyMap()).warning();
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());
        config.withWarningHandler((key, msg, critical) -> warnings.incrementAndGet())
              .withCriticalHandler((key, msg, critical) -> criticals.incrementAndGet());

        // Trigger warning
        config.get("app.configs").asWarning().asMap(String.class);
        assertEquals(1, warnings.get());

        // Trigger critical
        assertThrows(ConfigRuntimeException.class, () -> config.get("app.settings").asCritical().asMap(Integer.class));
        assertEquals(1, criticals.get());
    }

    @Test
    void testListOrListDefault() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.missing", String.class, Arrays.asList("default1", "default2"));
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        List<String> servers = config.get("app.missing").orList(String.class, Collections.emptyList());
        assertEquals(Arrays.asList("default1", "default2"), servers);
    }

    @Test
    void testMapOrMapDefault() throws ConfigNotFound, IOException {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("defaultKey", "defaultValue");

        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.missing", String.class, defaultMap);
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        Map<String, String> settings = config.get("app.missing").orMap(String.class, Collections.emptyMap());
        assertEquals(defaultMap, settings);
    }

    @Test
    void testPutList() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        List<String> newServers = Arrays.asList("new1", "new2");
        config.putList("app.servers", newServers);
        assertEquals(newServers, config.get("app.servers").asList(String.class));

        // Type mismatch
        assertThrows(ConfigRuntimeException.class, () -> config.putList("app.servers", Arrays.asList(1, 2)));
    }

    @Test
    void testPutMap() throws ConfigNotFound, IOException {
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        Map<String, String> newSettings = new HashMap<>();
        newSettings.put("newKey", "newValue");
        config.putMap("app.settings", newSettings);
        assertEquals(newSettings, config.get("app.settings").asMap(String.class));

        // Type mismatch
        Map<String, Integer> wrongMap = new HashMap<>();
        wrongMap.put("key", 1);
        assertThrows(ConfigRuntimeException.class, () -> config.putMap("app.settings", wrongMap));
    }

    @Test
    void testListSystemPropertyOverride() throws ConfigNotFound, IOException {
        System.setProperty("app.servers", "override1,override2");
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        List<String> servers = config.get("app.servers").useSystemProperty().asList(String.class);
        assertEquals(Arrays.asList("override1", "override2"), servers);

        // Without override
        List<String> original = config.get("app.servers").asList(String.class);
        assertEquals(Arrays.asList("server1", "server2", "server3"), original);
    }

    @Test
    void testMapSystemPropertyOverride() throws ConfigNotFound, IOException {
        System.setProperty("app.settings", "overrideKey=overrideValue");
        ConfigSchema schema = ConfigSchema.create()
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        Map<String, String> settings = config.get("app.settings").useSystemProperty().asMap(String.class);
        Map<String, String> expected = new HashMap<>();
        expected.put("overrideKey", "overrideValue");
        assertEquals(expected, settings);

        // Without override
        Map<String, String> original = config.get("app.settings").asMap(String.class);
        Map<String, String> expectedOriginal = new HashMap<>();
        expectedOriginal.put("key1", "value1");
        expectedOriginal.put("key2", "value2");
        expectedOriginal.put("key3", "value3");
        assertEquals(expectedOriginal, original);
    }

    @Test
    void testSaveListAndMap() throws IOException, ConfigNotFound {
        ConfigSchema schema = ConfigSchema.create()
                .defineList("app.servers", String.class, Collections.emptyList())
                .defineMap("app.settings", String.class, Collections.emptyMap());
        Config config = new Config().withSchema(schema);
        config.addSource(jsonFile.toString());

        List<String> newServers = Arrays.asList("new1", "new2");
        Map<String, String> newSettings = new HashMap<>();
        newSettings.put("newKey", "newValue");
        config.putList("app.servers", newServers);
        config.putMap("app.settings", newSettings);

        Path newFile = tempDir.resolve("new.json");
        config.save(newFile.toString());

        Config loaded = new Config().withSchema(schema);
        loaded.addSource(newFile.toString());
        assertEquals(newServers, loaded.get("app.servers").asList(String.class));
        assertEquals(newSettings, loaded.get("app.settings").asMap(String.class));
    }
}