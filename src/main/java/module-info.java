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

module io.github.snood21.update_calculator_1c {
    requires javafx.controls;
    requires javafx.fxml;

    // Apache POI (Работа с Excel/Word)
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    requires org.jetbrains.annotations;      // Аннотации JetBrains

    opens io.github.snood21.update_calculator_1c to javafx.fxml;
    exports io.github.snood21.update_calculator_1c;
}
