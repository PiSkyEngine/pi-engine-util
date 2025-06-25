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

import java.lang.reflect.Method;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InjectorMethodTest {

    private Injector injector;

    // Test interfaces and classes
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

    static class TestClass {
        String noParams() {
            return "No parameters";
        }

        String singleString(String value) {
            return "Received: " + value;
        }

        String annotatedString(@Named("message") String value) {
            return "Annotated: " + value;
        }

        String multipleParams(@Named("message") String message, int number, Service service) {
            return "Multiple: " + message + ", " + number + ", " + service.getMessage();
        }

        @Inject(optional = true)
        String optionalParam(@Named("nonexistent") String value) {
            return value == null ? "Optional: null" : "Optional: " + value;
        }
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Named.class, "message").toInstance("Hello, World!");
            bind(Integer.class).toInstance(42);
            bind(Service.class).to(ServiceImpl.class);
        }
    }

    @BeforeEach
    void setUp() {
        injector = Injector.createInjector(new TestModule());
    }

    @Test
    void testNoParamsMethod() throws Exception {
        TestClass instance = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("noParams");
        Object[] params = injector.getParameters(method);
        assertEquals(0, params.length, "No parameters should be returned");
        Object result = method.invoke(instance, params);
        assertEquals("No parameters", result, "Method result should match");
    }

    @Test
    void testSingleStringMethod() throws Exception {
        TestClass instance = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("singleString", String.class);
        assertThrows(InjectionException.class, () -> injector.getParameters(method),
                "Should throw InjectionException for unbound String parameter");
    }

    @Test
    void testAnnotatedStringMethod() throws Exception {
        TestClass instance = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("annotatedString", String.class);
        Object[] params = injector.getParameters(method);
        assertEquals(1, params.length, "One parameter should be returned");
        assertEquals("Hello, World!", params[0], "Parameter should match bound value");
        Object result = method.invoke(instance, params);
        assertEquals("Annotated: Hello, World!", result, "Method result should match");
    }

    @Test
    void testMultipleParamsMethod() throws Exception {
        TestClass instance = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("multipleParams", String.class, int.class, Service.class);
        Object[] params = injector.getParameters(method);
        assertEquals(3, params.length, "Three parameters should be returned");
        assertEquals("Hello, World!", params[0], "First parameter should match");
        assertEquals(42, params[1], "Second parameter should match");
        assertTrue(params[2] instanceof Service, "Third parameter should be a Service");
        Object result = method.invoke(instance, params);
        assertEquals("Multiple: Hello, World!, 42, Hello, World!", result, "Method result should match");
    }

    @Test
    void testOptionalParamMethod() throws Exception {
        TestClass instance = new TestClass();
        Method method = TestClass.class.getDeclaredMethod("optionalParam", String.class);
        Object[] params = injector.getParameters(method);
        assertEquals(1, params.length, "One parameter should be returned");
        assertNull(params[0], "Optional parameter should be null");
        Object result = method.invoke(instance, params);
        assertEquals("Optional: null", result, "Method result should match");
    }
}