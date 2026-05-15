plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.imgui"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.imgui"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
        prefab = true
    }
}


tasks.named("preBuild") {
    //dependsOn(":ImGuiView:assembleDebug")
    dependsOn(":ImGuiView:convertToDex")
    dependsOn(":ImGuiView:pythonToC")



}
tasks.register("push") {
    dependsOn("assembleDebug")
    doLast {
        val sdkPath = android.sdkDirectory
        val adbPath = sdkPath.resolve("platform-tools/adb")
        val buildDir = layout.buildDirectory.get().asFile
        val soPath = buildDir.resolve("intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/arm64-v8a/libimgui.so")
        println(buildDir.absolutePath)
        providers.exec {
            commandLine(
                adbPath.absolutePath,
                "push",
                soPath,
                "/data/local/tmp/com.pinkcore.heros"
            )

            // 设置工作目录
            workingDir = project.projectDir

            // 输出日志
            //standardOutput = System.out
            //errorOutput = System.err
        }.result.get()
    }

}

dependencies {

    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.lsplant.standalone)
    implementation(libs.dobby)
    implementation(libs.androidx.activity)
    implementation(project(":ImGuiView"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}