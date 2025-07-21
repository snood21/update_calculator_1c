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

import javafx.collections.ObservableList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UpdateCalculator {
    private static UpdateCalculator instance;
    private Map<String, Version> versions;
    private final Map<Version, Release> releases;

    private UpdateCalculator() {
        versions = new LinkedHashMap<>();
        releases = new HashMap<>();
    }

    public static synchronized UpdateCalculator getInstance() {
        if (instance == null) {
            instance = new UpdateCalculator();
        }
        return instance;
    }

    public Map<String, Version> getVersions() {
        return Collections.unmodifiableMap(versions);
    }

    private String getFileExtension(String path) {
        String fileName = Paths.get(path).getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        } else {
            return "";
        }
    }

    private void readExcel(String filePath, int columnVersion, int columnFromVersions) throws IOException, IllegalArgumentException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0); // Первый лист

        for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell cellVersion = row.getCell(columnVersion);
            Cell cellFromVersions = row.getCell(columnFromVersions);

            if (cellVersion == null) {
                throw new IllegalArgumentException("Неверный формат таблицы, отсутствует столбец версии");
            }

            if (cellFromVersions == null) {
                throw new IllegalArgumentException("Неверный формат таблицы, отсутствует столбец обновляемых версий");
            }

            DataFormatter formatter = new DataFormatter();
            String strVersion = formatter.formatCellValue(cellVersion).trim();
            String strFromVersions = formatter.formatCellValue(cellFromVersions).trim();

            if (strVersion.isEmpty()) {
                throw new IllegalArgumentException("Неверный формат таблицы, версия не может быть пустой");
            }

            Version version = getOrCreateVersion(strVersion);

            Release release = new Release(version);
            if (!strFromVersions.isEmpty()) {
                String[] strFromVersionsParts = strFromVersions.split(",");

                for (String strFromVersion : strFromVersionsParts) {
                    strFromVersion = strFromVersion.trim();
                    Version fromVersion = getOrCreateVersion(strFromVersion);
                    release.addFromVersion(fromVersion);
                }
            }
            releases.put(release.getVersion(), release);
        }
        versions = versions.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // сравнение по значениям
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // сохраняем порядок
                ));
    }

    private Version getOrCreateVersion(String strVersion) {
        return versions.computeIfAbsent(strVersion, Version::new);
    }

    void FillToVersions() {
        for (Map.Entry<Version, Release> release : releases.entrySet()) {
            for (Map.Entry<Version, Release> releaseFrom : releases.entrySet()) {
                for (Version fromVersion : releaseFrom.getValue().getFromVersions()) {
                    if (fromVersion.equals(release.getValue().getVersion())) {
                        release.getValue().addToVersion(releaseFrom.getKey());
                        break;
                    }
                }
            }
        }
    }

    void FillEmptyReleases() {
        for (Map.Entry<String, Version> entry : versions.entrySet()) {
            if (!releases.containsKey(entry.getValue())) {
                Release release = new Release(entry.getValue());

                for (Map.Entry<Version, Release> releaseFrom : releases.entrySet()) {
                    Optional<Version> find_version = releaseFrom.getValue().getFromVersions().stream().filter(v -> v.equals(release.getVersion())).findFirst();
                    find_version.ifPresent(version -> release.addToVersion(releaseFrom.getValue().getVersion()));
                }
                releases.put(release.getVersion(), release);
            }
        }
    }

    public void ReadFile(String filePath, int columnVersion, int columnFromVersions) throws IOException, IllegalArgumentException {
        String extension = getFileExtension(filePath).toLowerCase();

        if (extension.equals("xls") || extension.equals("xlsx")) {
            versions.clear();
            releases.clear();
            readExcel(filePath, columnVersion, columnFromVersions);
            FillEmptyReleases();
            FillToVersions();
        } else {
            throw new IllegalArgumentException("Неподдерживаемое расширение файла: " + extension);
        }
    }

    public Optional<LinkedList<String>> CalculateUpdates(Version firstVersion, Version lastVersion) {
        if (firstVersion.equals(lastVersion)) {
            LinkedList<String> result = new LinkedList<>();
            result.add(firstVersion.toString());
            return Optional.of(result);
        }

        Queue<LinkedList<Version>> queue = new LinkedList<>();
        Set<Version> visited = new HashSet<>();

        LinkedList<Version> startChain = new LinkedList<>();
        startChain.add(firstVersion);
        queue.add(startChain);
        visited.add(firstVersion);

        while (!queue.isEmpty()) {
            LinkedList<Version> currentChain = queue.poll();
            Version currentVersion = currentChain.getLast();

            Release currentRelease = releases.get(currentVersion);
            if (currentRelease == null) {
                continue;
            }

            for (Version nextVersion : currentRelease.getToVersions()) {
                if (nextVersion.compareTo(lastVersion) > 0 || visited.contains(nextVersion)) {
                    continue;
                }

                LinkedList<Version> newChain = new LinkedList<>(currentChain);
                newChain.add(nextVersion);

                if (nextVersion.equals(lastVersion)) {
                    LinkedList<String> result = newChain.stream()
                            .map(Version::toString)
                            .collect(Collectors.toCollection(LinkedList::new));
                    return Optional.of(result);
                }

                visited.add(nextVersion);
                queue.add(newChain);
            }
        }

        return Optional.empty(); // не найдено
    }

    private void saveAsTextFile(ObservableList<String> items, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String item : items) {
            writer.write(item);
            writer.newLine();
        }
        writer.close();
    }

    private void saveAsExcel(ObservableList<String> items, File file, boolean isXlsx) throws IOException {
        Workbook workbook = isXlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ListView Data");

        for (int i = 0; i < items.size(); i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(items.get(i));
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        workbook.close();
    }

    public void SaveToFile(File file, ObservableList<String> items) throws IOException {
        if (file != null) {
            String path = file.getAbsolutePath();

            if (path.endsWith(".txt")) {
                saveAsTextFile(items, file);
            } else if (path.endsWith(".xls")) {
                saveAsExcel(items, file, false); // HSSFWorkbook
            } else if (path.endsWith(".xlsx")) {
                saveAsExcel(items, file, true); // XSSFWorkbook
            }
        }
    }
}
