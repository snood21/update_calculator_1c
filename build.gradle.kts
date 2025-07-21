import org.gradle.internal.os.OperatingSystem

plugins {
    java
    application
    `maven-publish`
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "3.1.1"
}

group = property("group") as String
val os: OperatingSystem = OperatingSystem.current()

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("io.github.snood21.update_calculator_1c")
    mainClass.set("io.github.snood21.update_calculator_1c.UpdateCalculatorApp")
}

javafx {
    version = "21.0.7"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    // Уменьшаем размер дистрибутива
    options.set(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"
    ))

    addExtraDependencies("javafx")

    launcher {
        name = project.name
    }

    jpackage {
        imageName = project.name
        installerType = findProperty("installerType") as String? ?: "app-image"
        println(">>>> installerType = $installerType")
        skipInstaller = (installerType == "app-image")
        appVersion = property("version") as String
        vendor = property("vendor") as String
        installerName = project.name
        when {
            os.isWindows -> icon = "src/main/resources/io/github/snood21/update_calculator_1c/icons/icon.ico"
            os.isLinux -> icon = "src/main/resources/io/github/snood21/update_calculator_1c/icons/icon.png"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Update Calculator for 1C")
                description.set("JavaFX приложение для расчёта обновлений 1С")
                url.set("https://github.com/snood21/update-calculator-1c")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("snood21")
                        name.set("snood21")
                        email.set("snood21@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/snood21/update-calculator-1c.git")
                    developerConnection.set("scm:git:ssh://github.com:snood21/update-calculator-1c.git")
                    url.set("https://github.com/snood21/update-calculator-1c")
                }
            }
        }
    }
}