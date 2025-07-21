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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.util.stream.Collectors;

import javafx.stage.Stage;

public class MainFormController implements Initializable {
    @FXML
    private TextField filePathField;
    @FXML
    private Button chooseFileButton;
    @FXML
    private ComboBox<String> firstVersionComboBox;
    @FXML
    private ComboBox<String> lastVersionComboBox;
    @FXML
    private Spinner<Integer> columnVersionSpinner;
    @FXML
    private Spinner<Integer> columnFromVersionsSpinner;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> resultListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePathField.setText(System.getProperty("user.home"));

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1);
        columnVersionSpinner.setValueFactory(valueFactory);
        SpinnerValueFactory<Integer> valueFactoryFrom = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3, 1);
        columnFromVersionsSpinner.setValueFactory(valueFactoryFrom);

        resultListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    protected void firstVersionComboBoxOnKeyPressed(KeyEvent event) {
        versionComboBoxOnKeyPressed(firstVersionComboBox, event);
    }

    @FXML
    protected void lastVersionComboBoxOnKeyPressed(KeyEvent event) {
        versionComboBoxOnKeyPressed(lastVersionComboBox, event);
    }

    private void versionComboBoxOnKeyPressed(ComboBox<String> versionComboBox, KeyEvent event) {
        int index = versionComboBox.getSelectionModel().getSelectedIndex();
        int size = versionComboBox.getItems().size();

        switch (event.getCode()) {
            case PAGE_UP:
                index = Math.max(index - 10, 0);
                versionComboBox.getSelectionModel().select(index);
                break;
            case PAGE_DOWN:
                index = Math.min(index + 10, size - 1);
                versionComboBox.getSelectionModel().select(index);
                break;
            case HOME:
                versionComboBox.getSelectionModel().selectFirst();
                break;
            case END:
                versionComboBox.getSelectionModel().selectLast();
                break;
        }
    }

    @FXML
    protected void resultListViewOnKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.A) {
                resultListView.getSelectionModel().selectAll();
                event.consume();
            } else if (event.getCode() == KeyCode.C) {
                ObservableList<String> selectedItems = resultListView.getSelectionModel().getSelectedItems();
                if (!selectedItems.isEmpty()) {
                    String copiedText = String.join("\n", selectedItems);
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(copiedText);
                    clipboard.setContent(content);
                }
                event.consume();
            }
        }
    }

    @FXML
    protected void showFileChooserDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");

        File selectedFile = new File(filePathField.getText());
        if (selectedFile.exists())
        {
            if (selectedFile.isFile())
            {
                fileChooser.setInitialDirectory(selectedFile.getParentFile());
            } else
            {
                fileChooser.setInitialDirectory(selectedFile);
            }
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Таблица excel (.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("Таблица excel 97-2003 (.xls)", "*.xls"));

        selectedFile = fileChooser.showOpenDialog(getStage(chooseFileButton));

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }

    }

    @FXML
    protected void readFile() {
        changeStatus("Выполняется чтение файла...", true);

        Task<Void> loadFileTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                UpdateCalculator.getInstance().ReadFile(filePathField.getText(),
                        columnVersionSpinner.getValue() - 1,
                        columnFromVersionsSpinner.getValue() - 1);
                List<String> versionStrings = UpdateCalculator.getInstance().getVersions().values().stream()
                        .map(Version::toString)
                        .collect(Collectors.toList());

                ObservableList<String> observableVersions = FXCollections.observableArrayList(versionStrings);

                Platform.runLater(() -> {
                    firstVersionComboBox.setItems(observableVersions);
                    lastVersionComboBox.setItems(observableVersions);
                });

                return null;
            }

            @Override
            protected void succeeded() {
                changeStatus("Файл успешно загружен", false);
            }

            @Override
            protected void failed() {
                changeStatus("Ошибка при чтении файла", true);

                Throwable ex = getException();
                showErrorDialog("Ошибка загрузки", "Ошибка при чтении файла", ex.getMessage());
            }
        };

        new Thread(loadFileTask).start();
    }

    private Stage getStage(Node control) throws IllegalStateException {
        if (control != null && control.getScene() != null) {
            return (Stage) control.getScene().getWindow();
        }
        throw new IllegalStateException("Контрол не прикреплён к сцене");
    }

    private void changeStatus(String text, boolean visible) {
        statusLabel.setText(text);
        statusLabel.setVisible(visible);
    }

    private void showErrorDialog(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    protected void calculateUpdates() {
        String strFirstVersion = firstVersionComboBox.getValue();
        String strLastVersion = lastVersionComboBox.getValue();

        if (strFirstVersion != null && strLastVersion != null) {
            changeStatus("Выполняется вычисление цепочки обновлений...", true);

            Task<Optional<LinkedList<String>>> calculateUpdatesTask = new Task<>() {
                @Override
                protected Optional<LinkedList<String>> call() {
                    return UpdateCalculator.getInstance().CalculateUpdates(
                            UpdateCalculator.getInstance().getVersions().get(strFirstVersion),
                            UpdateCalculator.getInstance().getVersions().get(strLastVersion));
                }

                @Override
                protected void succeeded() {
                    resultListView.getItems().clear();
                    Optional<LinkedList<String>> updateChain = getValue();
                    if (updateChain.isEmpty()) {
                        changeStatus("Не существует цепочек обновлений с версии " + strFirstVersion + " до версии " + strLastVersion, true);
                        return;
                    }
                    resultListView.getItems().addAll(updateChain.get());
                    changeStatus("", false);
                }

                @Override
                protected void failed() {
                    changeStatus("Ошибка при вычислении цепочки обновлений", true);

                    Throwable ex = getException();
                    showErrorDialog("Ошибка вычисления", "Ошибка при вычислении цепочки обновлений", ex.getMessage());
                }
            };
            new Thread(calculateUpdatesTask).start();
        }
    }

    @FXML
    protected void saveResult() {
        ObservableList<String> items = resultListView.getItems();
        if (items.isEmpty()) {
            changeStatus("Нечего сохранять", true);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл");
        fileChooser.setInitialDirectory(new File(filePathField.getText()).getParentFile());

        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Текстовый файл (*.txt)", "*.txt");
        FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("Таблица excel (*.xlsx)", "*.xlsx");
        FileChooser.ExtensionFilter xlsFilter = new FileChooser.ExtensionFilter("Таблица excel 97-2003 (*.xls)", "*.xls");

        fileChooser.getExtensionFilters().addAll(txtFilter, xlsxFilter, xlsFilter);

        File file = fileChooser.showSaveDialog(getStage(chooseFileButton));

        if (file != null) {
            String fileName = file.getName();
            String extension = "";

            FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();

            if (selectedFilter == txtFilter) {
                extension = ".txt";
            } else if (selectedFilter == xlsxFilter) {
                extension = ".xlsx";
            } else if (selectedFilter == xlsFilter) {
                extension = ".xls";
            }

            if (!fileName.toLowerCase().endsWith(extension)) {
                file = new File(file.getParent(), fileName + extension);
            }

            try {
                UpdateCalculator.getInstance().SaveToFile(file, items);
            } catch (IOException ex) {
                showErrorDialog("Ошибка сохранения", "Ошибка при сохранении результата в файл", ex.getMessage());
            }
        }
    }

    @FXML
    protected void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Калькулятор обновлений 1С");
        TextArea aboutText = new TextArea("""
                Версия: 1.0.0
                Автор: snood21
                Лицензия: Apache 2.0
                GitHub: https://github.com/snood21/update_calculator_1c
                """);
        aboutText.setWrapText(true);
        aboutText.setEditable(false);
        aboutText.setMaxWidth(Double.MAX_VALUE);
        aboutText.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(aboutText);

        ButtonType licenseButton = new ButtonType("LICENSE");
        ButtonType noticeButton = new ButtonType("NOTICE");
        alert.getButtonTypes().addAll(licenseButton, noticeButton, ButtonType.CLOSE);

        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(button -> {
            if (button == licenseButton) {
                showLegalFile("/io/github/snood21/update_calculator_1c/legal/LICENSE", "LICENSE");
            } else if (button == noticeButton) {
                showLegalFile("/io/github/snood21/update_calculator_1c/legal/NOTICE", "NOTICE");
            }
        });
    }

    private void showLegalFile(String resourcePath, String title) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                showErrorDialog("Ошибка чтения файла", "Файл не найден: " + resourcePath, "");
                return;
            }

            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            TextArea textArea = new TextArea(content);
            textArea.setWrapText(true);
            textArea.setEditable(false);
            textArea.setPrefWidth(600);
            textArea.setPrefHeight(400);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(640, 480);
            alert.showAndWait();
        } catch (IOException e) {
            showErrorDialog("Ошибка чтения файла", "Ошибка при чтении файла", e.getMessage());
        }
    }

}
