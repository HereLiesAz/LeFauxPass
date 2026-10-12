import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Shared Compose Multiplatform UI: Android library consumed by :app, and the
// Kotlin/Wasm PWA (`:shared:wasmJsBrowserDistribution`) deployed to GitHub Pages.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
}

kotlin {
    android {
        namespace = "com.hereliesaz.lefauxpass.shared"
        compileSdk = 37
        minSdk = 26
        androidResources { enable = true }
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("lefauxpass")
        browser {
            commonWebpackConfig { outputFileName = "lefauxpass.js" }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.cmp.runtime)
            implementation(libs.cmp.foundation)
            implementation(libs.cmp.ui)
            implementation(libs.cmp.material3)
            implementation(libs.cmp.material.icons.extended)
            implementation(libs.cmp.components.resources)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    packageOfResClass = "com.hereliesaz.lefauxpass.shared.resources"
}

// The PWA serves the same animation as the Android app; no duplicate copy in source control.
tasks.named<ProcessResources>("wasmJsProcessResources") {
    from(rootProject.file("app/src/main/res/raw/animation.mp4"))
}
