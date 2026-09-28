package androidcomposebase.buildlogic

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

/**
 * Registers `apiDump`/`apiCheck` JavaExec tasks that run Metalava directly against this module's
 * public source API and wires `apiCheck` into `check`. There is no maintained Gradle plugin for
 * standalone Metalava use, so this drives the tool -- the one AndroidX itself uses -- imperatively
 * over this module's sources, release compile classpath, and Android boot classpath.
 *
 * [apiFileName] is committed at `api/<apiFileName>` and lets each published module keep its own
 * name (`core.api`, `ui.api`) while sharing this implementation.
 */
fun Project.registerApiTasks(
    metalavaClasspath: Configuration,
    apiFileName: String,
) {
    val androidComponents = extensions.getByType<LibraryAndroidComponentsExtension>()
    val androidBootClasspath = objects.fileCollection().from(androidComponents.sdkComponents.bootClasspath)

    val mainSourceDir = file("src/main/java")
    val sourceFiles = fileTree(mainSourceDir)
    val committedApiFile = layout.projectDirectory.file("api/$apiFileName").asFile
    val generatedApiFile = layout.buildDirectory.file("metalava/$apiFileName").get().asFile

    // The public library API is compiled for the published release variant. AGP's Variant API
    // exposes its resolved compile classpath lazily, including project artifact build dependencies.
    androidComponents.onVariants(androidComponents.selector().withBuildType("release")) { variant ->
        val compileClasspath = variant.compileClasspath
        val bootAndCompileClasspath =
            this@registerApiTasks.providers.provider {
                (androidBootClasspath.files + compileClasspath.files)
                    .distinct()
                    .joinToString(File.pathSeparator) { it.absolutePath }
            }

        fun JavaExec.configureMetalava(output: File) {
            classpath = metalavaClasspath
            mainClass.set("com.android.tools.metalava.Driver")
            outputs.upToDateWhen { false }
            inputs.files(sourceFiles).withPropertyName("apiSources")
            inputs.files(compileClasspath).withPropertyName("androidCompileClasspath")
            inputs.files(androidBootClasspath).withPropertyName("androidBootClasspath")
            dependsOn(compileClasspath.buildDependencies)

            val sourcePath = mainSourceDir.absolutePath
            val outPath = output.absolutePath
            doFirst { output.parentFile.mkdirs() }
            argumentProviders.add(
                CommandLineArgumentProvider {
                    listOf(
                        "main",
                        "--source-path",
                        sourcePath,
                        "--classpath",
                        bootAndCompileClasspath.get(),
                        "--api",
                        outPath,
                        "--format",
                        "4.0",
                    )
                },
            )
            doLast { rejectUnresolvedTypes(output, apiFileName) }
        }

        tasks.register<JavaExec>("apiDump") {
            group = "verification"
            description = "Regenerates api/$apiFileName from this module's public source API."
            configureMetalava(committedApiFile)
            doLast { committedApiFile.writeText(committedApiFile.readText().trimEnd() + "\n") }
        }

        val apiCheck =
            tasks.register<JavaExec>("apiCheck") {
                group = "verification"
                description = "Fails if api/$apiFileName is stale -- run apiDump and review the diff."
                configureMetalava(generatedApiFile)
                val committed = committedApiFile
                val generated = generatedApiFile
                doLast {
                    if (!committed.exists()) {
                        throw GradleException("api/$apiFileName is missing. Run apiDump and commit it.")
                    }
                    val committedText = committed.readText().trimEnd() + "\n"
                    val generatedText = generated.readText().trimEnd() + "\n"
                    if (committed.readText() != committedText || committedText != generatedText) {
                        throw GradleException(
                            "Public API differs from the committed api/$apiFileName. " +
                                "Run apiDump, review the diff, and commit it if intended.",
                        )
                    }
                }
            }

        tasks.named("check") { dependsOn(apiCheck) }
    }
}

private fun rejectUnresolvedTypes(apiFile: File, apiFileName: String) {
    if (!apiFile.exists()) {
        throw GradleException("Metalava did not produce api/$apiFileName at ${apiFile.absolutePath}.")
    }

    val unresolvedLines =
        apiFile.readLines()
            .mapIndexedNotNull { index, line ->
                if (UNRESOLVED_TYPE_PATTERN.containsMatchIn(line)) "${index + 1}: $line" else null
            }
    if (unresolvedLines.isNotEmpty()) {
        throw GradleException(
            "Metalava produced unresolved type signatures in api/$apiFileName:\n" +
                unresolvedLines.joinToString("\n"),
        )
    }
}

private val UNRESOLVED_TYPE_PATTERN = Regex("\\b(?:ErrorType|error\\.NonExistentClass)\\b")
