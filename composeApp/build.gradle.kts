import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ktlint)
}

ktlint {
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

val packageVersion = "1.0.2"

// Task to generate BuildConfig file
val generateBuildConfig =
    tasks.register("generateBuildConfig") {
        val version = packageVersion
        val outputDir = layout.buildDirectory.dir("generated/buildConfig/kotlin/com/jp319/zerochan")

        inputs.property("version", version)
        outputs.dir(outputDir)

        doLast {
            val buildConfigFile = outputDir.get().file("BuildConfig.kt").asFile
            buildConfigFile.parentFile.mkdirs()
            buildConfigFile.writeText(
                """
                package com.jp319.zerochan

                object BuildConfig {
                    const val VERSION = "$version"
                }
                """.trimIndent(),
            )
        }
    }

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.composeIcons.simpleIcons)
            implementation(libs.composeIcons.tablerIcons)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlinx.serialization)
            implementation(libs.material.color.utilities)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.compose.components.animatedImage)
                implementation(libs.slf4j.simple)
            }
            kotlin.srcDir(generateBuildConfig.map { it.outputs.files.asPath })
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.jp319.zerochan.MainKt"

        buildTypes.release.proguard {
            isEnabled = true

            obfuscate = false

            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe, TargetFormat.Rpm)

            jvmArgs("--enable-native-access=ALL-UNNAMED")
            jvmArgs("--sun-misc-unsafe-memory-access=allow")

            packageName = "Zerochan Downloader"
            this.packageVersion = packageVersion
            vendor = "John Fritz P. Antipuesto"
            description = "A beautiful desktop client for Zerochan."
            copyright = "© 2026 John Fritz P. Antipuesto"

            windows {
                shortcut = true
                menuGroup = "Zerochan Downloader"
                iconFile.set(project.file("src/jvmMain/resources/app_icon.ico"))
            }

            linux {
                shortcut = true
                iconFile.set(project.file("src/commonMain/composeResources/drawable/logo.png"))
                debMaintainer = "John Fritz P. Antipuesto <johnfritzantipuesto2020@gmail.com>"
                menuGroup = "Zerochan Downloader"
            }

            macOS {
                bundleID = "com.jp319.zerochan"
                iconFile.set(project.file("src/jvmMain/resources/app_icon.icns"))
            }
        }
    }
}
