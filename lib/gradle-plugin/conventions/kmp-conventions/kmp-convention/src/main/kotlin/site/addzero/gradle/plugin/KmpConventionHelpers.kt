package site.addzero.gradle.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import site.addzero.gradle.BuildSettings

internal fun KotlinMultiplatformExtension.configureJvmToolchain(
    toolchainVersion: Int = 8,
    jvmTarget: JvmTarget = JvmTarget.JVM_1_8,
) {
    jvmToolchain(toolchainVersion)

    targets.withType<KotlinJvmTarget>().configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    this.jvmTarget.set(jvmTarget)
                }
            }
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun KotlinMultiplatformExtension.configureAndroidJvmTarget(libs: LibrariesForLibs) {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdk.get()))
        }
    }
}

@OptIn(ExperimentalWasmDsl::class)
internal fun KotlinMultiplatformExtension.configureWasmBrowser(
    project: Project,
    outputFileName: String = "composeApp.js",
    outputModuleName: String? = null,
    executable: Boolean = false,
) {
    wasmJs {
        if (outputModuleName != null) {
            this.outputModuleName.set(outputModuleName)
        }
        if (executable) {
            binaries.executable()
        }
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                this.outputFileName = outputFileName
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }
}

internal fun KotlinMultiplatformExtension.configureComposeCommonDependencies(
    compose: ComposePlugin.Dependencies,
    libs: LibrariesForLibs,
) {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
        }
    }
}

internal fun KotlinMultiplatformExtension.configureComposeDesktopDependencies(
    compose: ComposePlugin.Dependencies,
    libs: LibrariesForLibs,
) {
    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

internal fun KotlinMultiplatformExtension.configureComposeAndroidDependencies(
    compose: ComposePlugin.Dependencies,
    libs: LibrariesForLibs,
) {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
    }
}

internal fun KotlinMultiplatformExtension.enableContextParameters() {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

internal fun Project.configureComposeDesktopApplication() {
    extensions.configure<ComposeExtension> {
        desktop {
            application {
                mainClass = BuildSettings.Desktop.MAIN_CLASS
                nativeDistributions {
                    targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                    packageName = BuildSettings.PACKAGE_NAME
                    packageVersion = "1.0.0"
                }
            }
        }
    }
}

internal fun Project.configureAndroidApp(libs: LibrariesForLibs) {
    extensions.configure<ApplicationExtension> {
        configureAndroidBase(libs)
        defaultConfig {
            applicationId = BuildSettings.Android.ANDROID_APP_ID
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            val vname = libs.versions.android.biz.version.get()
            versionName = vname
            versionCode = vname.toDouble().toInt()
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                pickFirsts += "META-INF/INDEX.LIST"
            }
        }
        buildTypes {
            getByName(BuildSettings.Android.BUILD_TYPE) {
                isMinifyEnabled = false
            }
        }
    }
}

internal fun Project.configureAndroidLibrary(libs: LibrariesForLibs) {
    extensions.configure<LibraryExtension> {
        configureAndroidBase(libs)
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }
}

private fun CommonExtension<*, *, *, *, *, *>.configureAndroidBase(libs: LibrariesForLibs) {
    namespace = BuildSettings.PACKAGE_NAME
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        val toVersion = JavaVersion.toVersion(libs.versions.jdk.get())
        sourceCompatibility = toVersion
        targetCompatibility = toVersion
    }
}

internal fun DependencyHandlerScope.addKoinKspDependencies(libs: LibrariesForLibs) {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
}

internal fun KotlinMultiplatformExtension.configureKoinDependencies(
    project: Project,
    libs: LibrariesForLibs,
    includeCompose: Boolean,
) {
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.annotations)
            implementation(libs.koin.core)
            if (includeCompose) {
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)
            }
        }
    }
}

internal fun KotlinMultiplatformExtension.configureSerializationDependencies(
    libs: LibrariesForLibs,
    includeTool: Boolean = false,
) {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
            if (includeTool) {
                implementation(libs.tool.json)
            }
        }
    }
}
