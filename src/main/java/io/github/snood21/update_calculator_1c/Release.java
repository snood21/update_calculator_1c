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

import java.util.*;

public class Release {
    private final Version version;
    private final Set<Version> fromVersions = new TreeSet<>();
    private final Set<Version> toVersions = new TreeSet<>();

    public Release(Version version) {
        if (version == null) throw new IllegalArgumentException("Version не может быть null");
        this.version = version;
    }

    public final Version getVersion() {
        return version;
    }

    public Set<Version> getFromVersions() {
        return Collections.unmodifiableSet(fromVersions);
    }

    public Set<Version> getToVersions() {
        return Collections.unmodifiableSet(toVersions);
    }

    public void addFromVersion(Version version) {
        if (version == null) throw new IllegalArgumentException("Version не может быть null");
        fromVersions.add(version);
    }

    public void addToVersion(Version version) {
        if (version == null) throw new IllegalArgumentException("Version не может быть null");
        toVersions.add(version);
    }

}
