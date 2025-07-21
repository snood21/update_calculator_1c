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

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class UpdateCalculatorApp extends javafx.application.Application {
    private static final String APP_TITLE = "Калькулятор обновлений 1С";
    private static final String MAIN_FORM_FXML = "mainForm.fxml";
    private static final String ICON_PATH = "/io/github/snood21/update_calculator_1c/icons/icon.png";


    @Override
    public void start(Stage stage) {
        URL fxml = UpdateCalculatorApp.class.getResource("MAIN_FORM_FXML");
        if (fxml == null) {
            System.err.println("FXML-файл " + MAIN_FORM_FXML + " не найден");
            Platform.exit();
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(fxml);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream(ICON_PATH)));
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки FXML: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
