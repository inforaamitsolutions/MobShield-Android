import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val embeddedModules =
    listOf(
        ":mobshield-core",
        ":mobshield-detect-root",
        ":mobshield-detect-hooks",
        ":mobshield-detect-debugger",
        ":mobshield-detect-environment",
        ":mobshield-detect-integrity",
    )

android {
    namespace = "io.mobshield"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// compileOnly: bundle submodules via mergeReleaseFatAar — do NOT publish them as transitives
// (avoids "Failed to resolve: mobshield-android:mobshield-core:unspecified" in consumer apps).
dependencies {
    compileOnly(project(":mobshield-core"))
    compileOnly(project(":mobshield-detect-root"))
    compileOnly(project(":mobshield-detect-hooks"))
    compileOnly(project(":mobshield-detect-debugger"))
    compileOnly(project(":mobshield-detect-environment"))
    compileOnly(project(":mobshield-detect-integrity"))
    api(libs.coroutines.android)
}

fun unzipZipEntry(
    zip: ZipFile,
    entry: ZipEntry,
    dest: java.io.File,
) {
    if (entry.isDirectory) {
        dest.mkdirs()
        return
    }
    dest.parentFile?.mkdirs()
    zip.getInputStream(entry).use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    }
}

fun mergeClassJars(
    jars: List<java.io.File>,
    outputJar: java.io.File,
) {
    outputJar.parentFile.mkdirs()
    val seen = mutableSetOf<String>()
    ZipOutputStream(outputJar.outputStream()).use { zos ->
        jars.forEach { jar ->
            ZipFile(jar).use { zip ->
                for (entry in zip.entries()) {
                    if (entry.isDirectory) continue
                    if (!seen.add(entry.name)) continue
                    zos.putNextEntry(ZipEntry(entry.name))
                    zip.getInputStream(entry).use { input -> input.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
}

fun repackAar(
    baseDir: java.io.File,
    outputAar: java.io.File,
) {
    outputAar.parentFile.mkdirs()
    ZipOutputStream(outputAar.outputStream()).use { zos ->
        baseDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val relative = baseDir.toPath().relativize(file.toPath()).toString().replace('\\', '/')
                zos.putNextEntry(ZipEntry(relative))
                file.inputStream().use { input -> input.copyTo(zos) }
                zos.closeEntry()
            }
    }
}

fun resolveReleaseAar(modulePath: String): java.io.File {
    val aarDir = project(modulePath).layout.buildDirectory.dir("outputs/aar").get().asFile
    return aarDir.listFiles()
        ?.firstOrNull { it.extension == "aar" && it.name.contains("release", ignoreCase = true) }
        ?: error("No release AAR in $aarDir for $modulePath (run assembleRelease first)")
}

gradle.projectsEvaluated {
    val assembleTasks =
        (listOf(":mobshield") + embeddedModules).map { project(it).tasks.named("assembleRelease") }

    tasks.register("mergeReleaseFatAar") {
        group = "mobshield"
        description = "Bundles core and all detect modules into mobshield-release.aar for JitPack consumers"

        dependsOn(assembleTasks)

        doLast {
            val hostAar = resolveReleaseAar(":mobshield")

            val workDir = temporaryDir.resolve("fat-aar-work")
            workDir.deleteRecursively()
            workDir.mkdirs()

            ZipFile(hostAar).use { zip ->
                for (entry in zip.entries()) {
                    unzipZipEntry(zip, entry, workDir.resolve(entry.name))
                }
            }

            val classJars = mutableListOf<java.io.File>()
            workDir.resolve("classes.jar").takeIf { it.exists() }?.let { classJars.add(it) }

            embeddedModules.forEach { modulePath ->
                val moduleAar = resolveReleaseAar(modulePath)
                val moduleWork = temporaryDir.resolve("module-${moduleAar.nameWithoutExtension}")
                moduleWork.deleteRecursively()
                moduleWork.mkdirs()
                ZipFile(moduleAar).use { zip ->
                    for (entry in zip.entries()) {
                        val dest = moduleWork.resolve(entry.name)
                        unzipZipEntry(zip, entry, dest)
                        if (entry.name.startsWith("jni/") && !entry.isDirectory) {
                            val jniDest = workDir.resolve(entry.name)
                            jniDest.parentFile?.mkdirs()
                            dest.copyTo(jniDest, overwrite = true)
                        }
                    }
                }
                moduleWork.resolve("classes.jar").takeIf { it.exists() }?.let { classJars.add(it) }
            }

            val mergedClasses = temporaryDir.resolve("classes-merged.jar")
            mergeClassJars(classJars.filter { it.length() > 0L }, mergedClasses)
            mergedClasses.copyTo(workDir.resolve("classes.jar"), overwrite = true)
            repackAar(workDir, hostAar)
        }
    }

    tasks.matching { it.name == "bundleReleaseAar" || it.name == "assembleRelease" }.configureEach {
        finalizedBy("mergeReleaseFatAar")
    }

    tasks.matching {
        it.name == "generateMetadataFileForMavenPublication" ||
            it.name.contains("publishMavenPublication") ||
            it.name == "publishToMavenLocal"
    }.configureEach {
        mustRunAfter("mergeReleaseFatAar")
        dependsOn("mergeReleaseFatAar")
    }

    // Safety net: strip stale project-module entries from Gradle metadata if they appear.
    tasks.matching { it.name == "generateMetadataFileForMavenPublication" }.configureEach {
        doLast {
            val moduleFile =
                layout.buildDirectory
                    .file("publications/maven/module.json")
                    .get()
                    .asFile
            if (!moduleFile.exists()) return@doLast
            val json = moduleFile.readText()
            if (!json.contains("mobshield-android")) return@doLast
            val cleaned =
                json.replace(
                    Regex("""\s*\{[^{}]*"group": "mobshield-android"[^{}]*\},?"""),
                    "",
                )
            moduleFile.writeText(cleaned)
        }
    }
}
