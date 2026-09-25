package androidcomposebase.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** Shared Android/Kotlin toolchain constants for every convention plugin. */
object AndroidConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 24
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_21
}

/** Pins every Kotlin compile task's JVM target to [AndroidConfig.JAVA_VERSION]. */
fun Project.configureKotlinJvm21() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
