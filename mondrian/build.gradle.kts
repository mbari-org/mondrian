/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/8.0.2/userguide/building_java_projects.html
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
    id("com.github.ben-manes.versions") version "0.46.0"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.beryx.jlink") version "2.26.0"
}

version = "1.0.1"

//java {
//    sourceCompatibility = JavaVersion.VERSION_20
//    targetCompatibility = JavaVersion.VERSION_20
//}

repositories {
    maven {
        name = "MBARI"
        url = uri("https://maven.pkg.github.com/mbari-org/maven")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

javafx {
    version = "20.0.1"
    modules("javafx.controls", "javafx.fxml", "javafx.media")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven {
        name = "MBARI"
        url = uri("https://maven.pkg.github.com/mbari-org/maven")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

//Resolve the used operating system
var currentOS = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()
var platform = ""
if (currentOS.isMacOsX) {
    platform = "mac"
} else if (currentOS.isLinux) {
    platform = "linux"
} else if (currentOS.isWindows) {
    platform = "win"
}

dependencies {

    // This dependency is used by the application.
    implementation("com.fatboyindustrial.gson-javatime-serialisers:gson-javatime-serialisers:1.1.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
    implementation("com.github.mizosoft.methanol:methanol:1.7.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.4")

    // This has to match the okhttp version used in org.mbari.vars.services or
    // we get java.lang.NoClassDefFoundError: kotlin/jvm/internal/Intrinsics
    implementation("com.squareup.okhttp3:okhttp:3.14.9")

    implementation("org.controlsfx:controlsfx:11.1.2")
    implementation("org.mbari.commons:jcommons:0.0.6")
    implementation("org.mbari.vars:org.mbari.vars.core:1.2.3")
    implementation("org.mbari.vars:org.mbari.vars.services:1.2.3")
    implementation("org.mbari.vcr4j:vcr4j-core:5.2.0")
    implementation("org.mbari:imgfx:0.0.15")
    runtimeOnly("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
//    implementation("io.github.palexdev:materialfx:11.16.1")
    implementation("org.slf4j:slf4j-jdk-platform-logging:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.7")
}


testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.1")

        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

val runtimeJvmArgs = arrayListOf(
    "-XX:+TieredCompilation",
    "-XX:TieredStopAtLevel=1",
    "-Xms1g",
    "--add-opens", "java.base/java.lang.invoke=retrofit2",
    "--add-opens", "java.base/java.lang.invoke=mondrian.merged.module",
    "--add-opens", "org.mbari.vars.services/org.mbari.vars.services.model=com.google.gson",
    "--add-reads", "mondrian.merged.module=org.slf4j",
    "--add-reads", "mondrian.merged.module=com.google.gson"
)


application {
    // Define the main class for the application.
    mainModule.set("org.mbari.mondrian")
    mainClass.set("org.mbari.mondrian.App")
    if(platform.equals("mac")) {
        applicationDefaultJvmArgs = listOf("-Dsun.java2d.metal=true")
    }
    applicationDefaultJvmArgs = runtimeJvmArgs
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = "Mondrian"
        jvmArgs = runtimeJvmArgs
    }

    mergedModule {
        additive = true
    }

//    launcher {
//        noConsole = true
//    }
    jpackage {
        val customInstallerOptions = arrayListOf(
            "--app-version", project.version.toString(),
            "--copyright", "Monterey Bay Aquarium Research Institute 2023",
            "--name", "Mondrian",
            "--vendor", "org.mbari"
        )

        if (platform == "mac") {
            installerType = "dmg"
            customInstallerOptions.addAll(listOf(
                "--mac-package-name", "Mondrian",
                "--mac-package-identifier", project.name.toString()
            ))
            imageOptions = listOf("--icon", "src/jpackage/macos/Mondrian.icns")
        }
        else if (platform == "linux") {
            installerType = "deb"
            customInstallerOptions.addAll(listOf(
                "--linux-shortcut", "true",
                "--linux-menu-group", "VARS"
            ))
            imageOptions = listOf("--icon", "src/jpackage/linux/icon_256x256.png")
        }
        else if (platform == "win") {
            installerType = "msi"
            customInstallerOptions.addAll(listOf(
                "--win-upgrade-uuid", "049e10bd-47c3-42b4-b39d-f37c3c94c689",
                "--win-menu-group", "VARS",
                "--win-menu"
            ))
            imageOptions = listOf("--icon", "src/jpackage/win/icon_256x256.ico")
        }
        installerOptions.addAll(customInstallerOptions)
    }
}

tasks.jpackageImage.get().doLast {
    if (platform == "mac") {
        val signer = System.getenv("MAC_CODE_SIGNER")
        if (signer != null) {
            val dirsToBeSigned = arrayListOf(
                file("${projectDir}/build/jpackage/Mondrian.app/Contents/runtime/Contents/Home/bin"),
                file("${projectDir}/build/jpackage/Mondrian.app/Contents/runtime/Contents/Home/lib"),
                file("${projectDir}/build/jpackage/Mondrian.app/Contents/runtime/Contents/Home/lib/server"),
                file("${projectDir}/build/jpackage/Mondrian.app/Contents/runtime/Contents/MacOS")
            )

            dirsToBeSigned.forEach { dir ->
                println("Signing $dir")
                val files = layout.files(dir.listFiles())
                files
                    .filter { it.isFile }
                    .forEach { file ->
                        exec {
                            println("MACOSX: Signing ${file}")
                            workingDir = file("build/jpackage")
                            executable = "codesign"
                            args = listOf(
                                "--entitlements", "${projectDir}/src/jpackage/macos/java.entitlements",
                                "--options", "runtime",
                                "--timestamp",
                                "-vvv",
                                "-f",
                                "--sign", signer,
                                file.absolutePath
                            )
                        }
                    }
            }

            exec {
                println("MACOSX: Signing application")
                workingDir = file("build/jpackage")
                executable = "codesign"
                args = listOf(
                    "--entitlements", "${projectDir}/src/jpackage/macos/java.entitlements",
                    "--options", "runtime",
                    "--timestamp",
                    "-vvv",
                    "-f",
                    "--sign", signer,
                    "Mondrian.app")

            }
        }
    }
}