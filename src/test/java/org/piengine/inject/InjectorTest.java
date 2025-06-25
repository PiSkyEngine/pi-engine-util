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
package org.piengine.inject;

import static org.junit.jupiter.api.Assertions.*;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.piengine.inject.AbstractModule;
import org.piengine.inject.Binding;
import org.piengine.inject.Inject;
import org.piengine.inject.Injector;
import org.piengine.inject.Key;
import org.piengine.inject.Module;
import org.piengine.inject.Provider;

class InjectorTest {

    private Injector injector;

    // Test classes and interfaces
    interface Service {
        String getMessage();
    }

    static class ServiceImpl implements Service {
        private final String message;

        @Inject
        public ServiceImpl(@Named("message") String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    static class Client {
        @Inject
        private Service service;

        public Service getService() {
            return service;
        }
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Named.class, "message").toInstance("Hello, World!");
            bind(Service.class).to(ServiceImpl.class);
            bind(Client.class).to(Client.class);
        }
    }

    @BeforeEach
    void setUp() {
        injector = Injector.createInjector(new TestModule());
    }

    @Test
    void testCreateInjector() {
        assertNotNull(injector, "Injector should not be null");
    }

    @Test
    void testGetInstance() {
        Service service = injector.getInstance(Service.class);
        assertNotNull(service, "Service instance should not be null");
        assertTrue(service instanceof ServiceImpl, "Service should be instance of ServiceImpl");
        assertEquals("Hello, World!", service.getMessage(), "Service message should match");
    }

    @Test
    void testInjectMembers() {
        Client client = new Client();
        assertNull(client.getService(), "Service should be null before injection");
        injector.injectMembers(client);
        assertNotNull(client.getService(), "Service should not be null after injection");
        assertEquals("Hello, World!", client.getService().getMessage(), "Service message should match");
    }

    @Test
    void testCreateChildInjector() {
        Injector childInjector = injector.createChildInjector();
        assertNotNull(childInjector, "Child injector should not be null");
        Service service = childInjector.getInstance(Service.class);
        assertNotNull(service, "Service instance from child injector should not be null");
        assertEquals("Hello, World!", service.getMessage(), "Service message should match");
    }

    @Test
    void testGetBinding() {
        Binding<Service> binding = injector.getBinding(Service.class);
        assertNotNull(binding, "Binding should not be null");
        Service service = binding.getProvider().get();
        assertNotNull(service, "Service from binding should not be null");
        assertEquals("Hello, World!", service.getMessage(), "Service message should match");
    }

    @Test
    void testGetProvider() {
        Provider<Service> provider = injector.getProvider(Service.class);
        assertNotNull(provider, "Provider should not be null");
        Service service = provider.get();
        assertNotNull(service, "Service from provider should not be null");
        assertEquals("Hello, World!", service.getMessage(), "Service message should match");
    }

    @Test
    void testModuleConfiguration() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Named.class, "test").toInstance("Test Value");
            }
        };
        Injector newInjector = Injector.createInjector(module);
        Key<String> key = Key.of(String.class, Named.class, "test");
        String value = newInjector.getInstance(key);
        assertEquals("Test Value", value, "Bound string value should match");
    }

    @Test
    void testOptionalInjection() {
        class OptionalClient {
            @Inject(optional = true)
            private String nonExistent;

            public String getNonExistent() {
                return nonExistent;
            }
        }

        OptionalClient client = new OptionalClient();
        injector.injectMembers(client);
        assertNull(client.getNonExistent(), "Optional injection should not throw error and remain null");
    }

    @Test
    void testExternalInjector() {
        Injector externalInjector = injector.createExternalInjector(
            Named.class,
            key -> "external".equals(((Named) key.getAnnotation()).value()) ? "External Value" : null
        );
        Key<String> key = Key.of(String.class, Named.class, "external");
        String value = externalInjector.getInstance(key);
        assertEquals("External Value", value, "External injector should provide correct value");
    }

    @Test
    void testBindingRegistration() {
        Binding<Service> serviceBinding = injector.getBinding(Service.class);
        assertNotNull(serviceBinding, "Service binding should be registered");
        Binding<Client> clientBinding = injector.getBinding(Client.class);
        assertNotNull(clientBinding, "Client binding should be registered");
        Key<String> stringKey = Key.of(String.class, Named.class, "message");
        Binding<String> stringBinding = injector.getBinding(stringKey);
        assertNotNull(stringBinding, "String binding with @Named(\"message\") should be registered");
    }
}