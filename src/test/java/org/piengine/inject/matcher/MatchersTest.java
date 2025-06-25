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

package org.piengine.inject.matcher;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.piengine.inject.matcher.Matcher;
import org.piengine.inject.matcher.Matchers;

/**
 * Unit tests for the {@link Matchers} utility class, covering all static factory
 * methods that create {@link Matcher} instances.
 */
class MatchersTest {

    // Test annotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TestAnnotation {}

    // Test classes and methods
    static class SuperClass {}
    static class SubClass extends SuperClass {}
    static class UnrelatedClass {}

    static class AnnotatedClass {
        @TestAnnotation
        void annotatedMethod() {}
        void unannotatedMethod() {}
    }

    static Method annotatedMethod;
    static Method unannotatedMethod;

    @BeforeAll
    static void setup() throws NoSuchMethodException {
        annotatedMethod = AnnotatedClass.class.getDeclaredMethod("annotatedMethod");
        unannotatedMethod = AnnotatedClass.class.getDeclaredMethod("unannotatedMethod");
    }

    /**
     * Tests the {@code any} matcher, which should match any non-null object.
     */
    @Test
    void testAnyMatchesAll() {
        Matcher<Object> matcher = Matchers.any();
        assertTrue(matcher.matches("test"), "Should match string");
        assertTrue(matcher.matches(123), "Should match integer");
        assertTrue(matcher.matches(null), "Should match null");
    }

    /**
     * Tests the {@code annotatedWith(Class)} matcher for annotation presence.
     */
    @Test
    void testAnnotatedWithClass() {
        Matcher<AnnotatedElement> matcher = Matchers.annotatedWith(TestAnnotation.class);
        assertTrue(matcher.matches(annotatedMethod), "Should match annotated method");
        assertFalse(matcher.matches(unannotatedMethod), "Should not match unannotated method");
        assertThrows(NullPointerException.class, () -> Matchers.annotatedWith((Class<? extends Annotation>) null),
                "Should throw NullPointerException for null annotation type");
    }

    /**
     * Tests the {@code annotatedWith(Annotation)} matcher for exact annotation instance.
     */
    @Test
    void testAnnotatedWithAnnotation() {
        TestAnnotation annotation = annotatedMethod.getAnnotation(TestAnnotation.class);
        Matcher<AnnotatedElement> matcher = Matchers.annotatedWith(annotation);
        assertTrue(matcher.matches(annotatedMethod), "Should match method with exact annotation");
        assertFalse(matcher.matches(unannotatedMethod), "Should not match unannotated method");
        assertThrows(NullPointerException.class, () -> Matchers.annotatedWith((Annotation) null),
                "Should throw NullPointerException for null annotation");
    }

    /**
     * Tests the {@code subclassesOf} matcher for class hierarchy.
     */
    @Test
    void testSubclassesOf() {
        Matcher<Class<?>> matcher = Matchers.subclassesOf(SuperClass.class);
        assertTrue(matcher.matches(SubClass.class), "Should match subclass");
        assertTrue(matcher.matches(SuperClass.class), "Should match superclass itself");
        assertFalse(matcher.matches(UnrelatedClass.class), "Should not match unrelated class");
        assertThrows(NullPointerException.class, () -> Matchers.subclassesOf(null),
                "Should throw NullPointerException for null superclass");
    }

    /**
     * Tests the {@code only} matcher for equality-based matching.
     */
    @Test
    void testOnly() {
        String value = "test";
        Matcher<Object> matcher = Matchers.only(value);
        assertTrue(matcher.matches("test"), "Should match equal string");
        assertFalse(matcher.matches("other"), "Should not match different string");
        assertFalse(matcher.matches(null), "Should not match null");
    }

    /**
     * Tests the {@code identicalTo} matcher for reference equality.
     */
    @Test
    void testIdenticalTo() {
        String value = new String("test");
        Matcher<Object> matcher = Matchers.identicalTo(value);
        assertTrue(matcher.matches(value), "Should match identical object");
        assertFalse(matcher.matches(new String("test")), "Should not match different object with same value");
        assertFalse(matcher.matches(null), "Should not match null");
    }

    /**
     * Tests the {@code inPackage} matcher for exact package matching.
     */
    @Test
    void testInPackage() {
        Package targetPackage = SuperClass.class.getPackage();
        Matcher<Class<?>> matcher = Matchers.inPackage(targetPackage);
        assertTrue(matcher.matches(SuperClass.class), "Should match class in package");
        assertFalse(matcher.matches(String.class), "Should not match class in different package");
        assertThrows(NullPointerException.class, () -> Matchers.inPackage(null),
                "Should throw NullPointerException for null package");
    }

    /**
     * Tests the {@code inSubpackage} matcher for package and subpackage matching.
     */
    @Test
    void testInSubpackage() {
        Matcher<Class<?>> matcher = Matchers.inSubpackage("org.piengine");
        assertTrue(matcher.matches(SuperClass.class), "Should match class in package");
        assertTrue(matcher.matches(Matcher.class), "Should match class in subpackage");
        assertFalse(matcher.matches(String.class), "Should not match class in different package");
        assertFalse(matcher.matches(null), "Should not match null class");
        assertThrows(NullPointerException.class, () -> Matchers.inSubpackage(null),
                "Should throw NullPointerException for null package name");
    }

    /**
     * Tests the {@code returns} matcher for method return types.
     * @throws NoSuchMethodException 
     */
    @Test
    void testReturns() throws NoSuchMethodException {
        Matcher<Method> matcher = Matchers.returns(void.class);
        assertTrue(matcher.matches(annotatedMethod), "Should match method with void return type");
        assertFalse(matcher.matches(Object.class.getDeclaredMethod("toString")), 
                "Should not match method with different return type");
        assertThrows(NullPointerException.class, () -> Matchers.returns(null),
                "Should throw NullPointerException for null return type");
    }
}