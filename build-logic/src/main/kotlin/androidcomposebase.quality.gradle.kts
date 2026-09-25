import androidcomposebase.buildlogic.VerifySourceBoundaryTask

plugins {
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
}

ktlint {
    android = true
    outputToConsole = true
    filter {
        exclude("**/generated/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

// Coverage bounds are a per-module decision, not a shared convention: :core keeps its own filtered
// `verify` rule and :core:ui declares none, since it is lifecycle/UI glue with no
// meaningful unit-test surface. A shared `minBound` here would fail its `check` task.
//
// `verifyDeterministicCoreCoverage` is a stable, memorable alias for Kover's own `koverVerify`
// task, shared by every module using this convention so a module with no `verify {}` rule (like
// :core:ui) still exposes the same task name, vacuously passing.
val verifyDeterministicCoreCoverage =
    tasks.register("verifyDeterministicCoreCoverage") {
        group = "verification"
        description = "Alias for koverVerify: enforces this module's deterministic (non-UI) Kover coverage rule, if any."
        dependsOn("koverVerify")
    }
tasks.named("check") { dependsOn(verifyDeterministicCoreCoverage) }

// The framework-independence boundary only applies to :core's foundation package. It is
// project-relative (so a differently-shaped module could point it elsewhere) and only registered
// when that source root actually exists, so applying this convention to a module without it --
// such as :app -- does not try to verify a directory that was never meant to exist there.
val frameworkIndependentSourceRoot =
    layout.projectDirectory.dir("src/main/java/com/thanhng224/androidcomposebase/core/foundation")

if (frameworkIndependentSourceRoot.asFile.isDirectory) {
    val verifyFrameworkIndependentSources =
        tasks.register<VerifySourceBoundaryTask>("verifyFrameworkIndependentSources") {
            group = "verification"
            description = "Fails when core.foundation imports a framework/transport type."
            sourceRoot.set(frameworkIndependentSourceRoot)
            forbiddenImportPrefixes.set(
                listOf(
                    "android.",
                    "androidx.",
                    "retrofit2.",
                    "okhttp3.",
                    "dagger.",
                    "javax.inject.",
                    "com.google.android.material.",
                    "com.thanhng224.androidcomposebase.core.R",
                ),
            )
        }

    tasks.named("check") { dependsOn(verifyFrameworkIndependentSources) }
}
