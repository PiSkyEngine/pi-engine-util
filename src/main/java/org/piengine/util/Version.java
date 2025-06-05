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
package org.piengine.util;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The Class Version.
 */
public class Version implements Comparable<Version> {

	/** The major. */
	private final int major;
	
	/** The minor. */
	private final int minor;
	
	/** The patch. */
	private final int patch;
	
	/** The pre release. */
	private final String preRelease; // Optional pre-release tag (e.g., "alpha.1")
	
	/** The build meta. */
	private final String buildMeta; // Optional build metadata (e.g., "build.123")

	/** The Constant VERSION_PATTERN. */
	private static final String VERSION_PATTERN = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)"
			+ "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
			+ "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
			+ "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

	/** The Constant SIMPLE_VERSION_PATTERN. */
	private static final String SIMPLE_VERSION_PATTERN = "(\\d+\\.\\d+\\.\\d+)";

	/**
	 * Extract from string.
	 *
	 * @param str the str
	 * @return the optional
	 */
	public static Optional<String> extractFromString(String str) {
		Pattern pattern = Pattern.compile(SIMPLE_VERSION_PATTERN);
		var matcher = pattern.matcher(str);

		return Optional.ofNullable(matcher.find() ? matcher.group(1) : null);
	}

	/**
	 * Minimal check.
	 *
	 * @param name       the name
	 * @param libVersion the lib version
	 * @param appVersion the app version
	 * @throws InvalidVersionException the invalid version exception
	 */
	public static void minimalCheck(String name, String libVersion, String appVersion) throws InvalidVersionException {
		var lib = new Version(libVersion);
		var app = new Version(appVersion);

		if (app.major == lib.major && app.minor <= lib.minor)
			return;

		throw new InvalidVersionException("%s library version [%s] is incompatible with app version [%s]"
				.formatted(name, libVersion, appVersion));
	}

	/**
	 * Strict check.
	 *
	 * @param name       the name
	 * @param libVersion the lib version
	 * @param appVersion the app version
	 * @throws InvalidVersionException the invalid version exception
	 */
	public static void strictCheck(String name, String libVersion, String appVersion) throws InvalidVersionException {
		var lib = new Version(libVersion);
		var app = new Version(appVersion);

		if (app.major == lib.major && app.minor == lib.minor && app.patch <= lib.patch)
			return;

		throw new InvalidVersionException("%s library version [%s] is incompatible with app version [%s]"
				.formatted(name, libVersion, appVersion));
	}

	/**
	 * Parses the version.
	 *
	 * @param version the version
	 * @return the string[]
	 * @throws InvalidVersionException the invalid version exception
	 */

	private static String[] parseVersion(String version) throws InvalidVersionException {
		Objects.requireNonNull(version, "Version string cannot be null");

		if (!version.matches(VERSION_PATTERN)) {
			throw new InvalidVersionException("Invalid version format: " + version);
		}

		String[] components = new String[5];

		// Split on +, handling build metadata
		String[] buildParts = version.split("\\+", 2);
		String versionPart = buildParts[0];
		components[4] = buildParts.length > 1 ? buildParts[1] : null;

		// Split on -, handling pre-release
		String[] preParts = versionPart.split("-", 2);
		String numberPart = preParts[0];
		components[3] = preParts.length > 1 ? preParts[1] : null;

		// Split version numbers
		String[] numbers = numberPart.split("\\.");
		components[0] = numbers[0];
		components[1] = numbers[1];
		components[2] = numbers[2];

		return components;
	}

	/**
	 * Instantiates a new version.
	 *
	 * @param version the version
	 * @throws InvalidVersionException the invalid version exception
	 */
	public Version(String version) throws InvalidVersionException {
		String[] components = parseVersion(version);

		try {
			this.major = Integer.parseInt(components[0]);
			this.minor = Integer.parseInt(components[1]);
			this.patch = Integer.parseInt(components[2]);
			this.preRelease = components[3];
			this.buildMeta = components[4];
		} catch (NumberFormatException e) {
			throw new InvalidVersionException("Invalid version numbers in: " + version, e);
		}
	}

	/**
	 * Instantiates a new version.
	 *
	 * @param major the major
	 * @param minor the minor
	 * @param patch the patch
	 */
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, null, null);
	}

	/**
	 * Instantiates a new version.
	 *
	 * @param major      the major
	 * @param minor      the minor
	 * @param patch      the patch
	 * @param preRelease the pre release
	 */
	public Version(int major, int minor, int patch, String preRelease) {
		this(major, minor, patch, preRelease, null);
	}

	/**
	 * Instantiates a new version.
	 *
	 * @param major      the major
	 * @param minor      the minor
	 * @param patch      the patch
	 * @param preRelease the pre release
	 * @param buildMeta  the build meta
	 */
	public Version(int major, int minor, int patch, String preRelease, String buildMeta) {
		if (major < 0 || minor < 0 || patch < 0) {
			throw new IllegalArgumentException("Version numbers cannot be negative");
		}

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preRelease;
		this.buildMeta = buildMeta;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Version other) {
		// Compare major.minor.patch
		int result = Integer.compare(this.major, other.major);
		if (result != 0)
			return result;

		result = Integer.compare(this.minor, other.minor);
		if (result != 0)
			return result;

		result = Integer.compare(this.patch, other.patch);
		if (result != 0)
			return result;

		// If versions are equal, pre-release versions have lower precedence
		if (this.preRelease == null && other.preRelease == null)
			return 0;
		if (this.preRelease == null)
			return 1; // No pre-release > pre-release
		if (other.preRelease == null)
			return -1;

		// Compare pre-release versions
		String[] thisParts = this.preRelease.split("\\.");
		String[] otherParts = other.preRelease.split("\\.");

		int length = Math.min(thisParts.length, otherParts.length);
		for (int i = 0; i < length; i++) {
			String thisPart = thisParts[i];
			String otherPart = otherParts[i];

			boolean thisIsNum = thisPart.matches("\\d+");
			boolean otherIsNum = otherPart.matches("\\d+");

			if (thisIsNum && otherIsNum) {
				// Compare numerically
				int thisNum = Integer.parseInt(thisPart);
				int otherNum = Integer.parseInt(otherPart);
				result = Integer.compare(thisNum, otherNum);
			} else {
				// Compare lexically
				result = thisPart.compareTo(otherPart);
			}

			if (result != 0)
				return result;
		}

		// If all parts match, longer version has higher precedence
		return Integer.compare(thisParts.length, otherParts.length);
	}

	/**
	 * Checks if is compatible with.
	 *
	 * @param libraryVersion the library version
	 * @return true, if is compatible with
	 */
	public boolean isCompatibleWith(Version libraryVersion) {
		Objects.requireNonNull(libraryVersion, "Library version cannot be null");

		// For 0.x.x versions, require exact match of major and minor
		if (this.major == 0) {
			return this.major == libraryVersion.major &&
					this.minor == libraryVersion.minor;
		}

		// For stable versions (1.0.0 and above):
		// 1. Major versions must match exactly
		// 2. Library's minor version must be >= than what we require
		return this.major == libraryVersion.major &&
				this.minor <= libraryVersion.minor;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(major).append('.').append(minor).append('.').append(patch);

		if (preRelease != null) {
			sb.append('-').append(preRelease);
		}

		if (buildMeta != null) {
			sb.append('+').append(buildMeta);
		}

		return sb.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Version))
			return false;
		Version other = (Version) obj;

		return this.major == other.major &&
				this.minor == other.minor &&
				this.patch == other.patch &&
				Objects.equals(this.preRelease, other.preRelease);
		// Note: Build metadata is not part of equality comparison per SemVer spec
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch, preRelease);
		// Note: Build metadata is not part of hashCode per SemVer spec
	}

	/**
	 * Gets the major.
	 *
	 * @return the major
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Gets the minor.
	 *
	 * @return the minor
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Gets the patch.
	 *
	 * @return the patch
	 */
	public int getPatch() {
		return patch;
	}

	/**
	 * Gets the pre release.
	 *
	 * @return the pre release
	 */
	public String getPreRelease() {
		return preRelease;
	}

	/**
	 * Gets the builds the metadata.
	 *
	 * @return the builds the metadata
	 */
	public String getBuildMetadata() {
		return buildMeta;
	}
}