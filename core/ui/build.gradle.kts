plugins {
    id("androidcomposebase.android-library")
    id("androidcomposebase.quality")
    id("androidcomposebase.published-library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.thanhng224.androidcomposebase.core.ui"

    buildFeatures {
        compose = true
    }
}

publishedLibrary {
    artifactId.set("AndroidComposeBase-ui")
    displayName.set("AndroidComposeBase UI")
    description.set("Jetpack Compose Design System and UI components for AndroidComposeBase.")
}

kotlin {
    explicitApi()
}

dependencies {
    api(project(":core"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.coil.compose)
    implementation(libs.androidx.activity.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// The spec exempts this module from a line-coverage percentage gate: it is 89 lines of
// lifecycle/Compose glue with no meaningful unit-test surface, covered instead by Lint, API
// checks, compilation, and the temporary-repository consumer build. No `kover { ... verify }`
// rule is declared here.
