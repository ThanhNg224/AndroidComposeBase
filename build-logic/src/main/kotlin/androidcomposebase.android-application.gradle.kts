import androidcomposebase.buildlogic.AndroidConfig
import androidcomposebase.buildlogic.configureKotlinJvm21
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
}

extensions.configure<ApplicationExtension> {
    compileSdk {
        version = release(AndroidConfig.COMPILE_SDK)
    }

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.COMPILE_SDK
    }

    compileOptions {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

configureKotlinJvm21()
