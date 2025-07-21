/*
 * Copyright 2025 snood21
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.snood21.update_calculator_1c;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
    private final String name;
    private final int v1, v2, v3, v4;
    private final int hash;

    public Version(String versionString) throws IllegalArgumentException {
        if (versionString == null) {
            throw new IllegalArgumentException("versionString cannot be null");
        }

        this.name = versionString;

        int[] versionParts = parseVersion(versionString);
        this.v1 = versionParts[0];
        this.v2 = versionParts[1];
        this.v3 = versionParts[2];
        this.v4 = versionParts[3];

        this.hash = Objects.hash(v1, v2, v3, v4);
    }

    private static int[] parseVersion(String versionString) {
        String[] parts = versionString.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Переданная строка версии " + versionString + " имеет некорректный формат");
        }
        try {
            return new int[]{
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Переданная строка версии " + versionString + " имеет некорректный формат", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Version other)) return false;
        return v1 == other.v1 && v2 == other.v2 && v3 == other.v3 && v4 == other.v4;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(@NotNull Version other) {
        if (other == null) {
            throw new NullPointerException("Значение other не может быть null");
        }

        int cmp = Integer.compare(this.v1, other.v1);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.v2, other.v2);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.v3, other.v3);
        if (cmp != 0) return cmp;

        return Integer.compare(this.v4, other.v4);
    }

    @Override
    public String toString() {
        return name;
    }
}
