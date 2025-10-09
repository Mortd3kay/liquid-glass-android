import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_23)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

//            After Effects Animations
            implementation(libs.compose.icons.extended)

            implementation(libs.compottie)
        }

        desktopMain.dependencies {
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.animation)
        }

        jsMain.dependencies {
            implementation(libs.html.core.js)
        }
    }

    js(IR) {
        browser {
            binaries.executable()
        }
    }
}

android {
    namespace = "org.delyo.glass"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
//        proguardFiles.add(files("consumer-rules.pro").files.first())

        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
}

compose.desktop {
    application {
        mainClass = "org.delyo.glass.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.delyo.glass"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}