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

import org.junit.jupiter.api.Test;
import org.piengine.inject.matcher.Matcher;

/**
 * Unit tests for the {@link Matcher} interface, focusing on the default
 * implementations of the {@code and} and {@code or} methods.
 */
class MatcherTest {

    /**
     * Tests the {@code and} method, ensuring it combines two matchers correctly
     * using logical AND.
     */
    @Test
    void testAndCombinesMatchersCorrectly() {
        Matcher<String> startsWithA = s -> s != null && s.startsWith("A");
        Matcher<String> endsWithZ = s -> s != null && s.endsWith("Z");

        Matcher<String> combined = startsWithA.and(endsWithZ);

        assertTrue(combined.matches("AZ"), "Should match string starting with A and ending with Z");
        assertFalse(combined.matches("AB"), "Should not match string not ending with Z");
        assertFalse(combined.matches("YZ"), "Should not match string not starting with A");
        assertFalse(combined.matches(null), "Should not match null");
    }

    /**
     * Tests the {@code or} method, ensuring it combines two matchers correctly
     * using logical OR.
     */
    @Test
    void testOrCombinesMatchersCorrectly() {
        Matcher<String> startsWithA = s -> s != null && s.startsWith("A");
        Matcher<String> endsWithZ = s -> s != null && s.endsWith("Z");

        Matcher<String> combined = startsWithA.or(endsWithZ);

        assertTrue(combined.matches("AZ"), "Should match string starting with A and ending with Z");
        assertTrue(combined.matches("AB"), "Should match string starting with A");
        assertTrue(combined.matches("YZ"), "Should match string ending with Z");
        assertFalse(combined.matches("BC"), "Should not match string neither starting with A nor ending with Z");
        assertFalse(combined.matches(null), "Should not match null");
    }

    /**
     * Tests that the {@code and} method handles null inputs gracefully.
     */
    @Test
    void testAndWithNullMatcher() {
        Matcher<String> startsWithA = s -> s != null && s.startsWith("A");
        assertThrows(NullPointerException.class, () -> startsWithA.and(null),
                "Should throw NullPointerException for null matcher");
    }

    /**
     * Tests that the {@code or} method handles null inputs gracefully.
     */
    @Test
    void testOrWithNullMatcher() {
        Matcher<String> startsWithA = s -> s != null && s.startsWith("A");
        assertThrows(NullPointerException.class, () -> startsWithA.or(null),
                "Should throw NullPointerException for null matcher");
    }
}