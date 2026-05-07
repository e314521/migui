import com.android.build.gradle.internal.scope.ProjectInfo.Companion.getBaseName

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.imguiview"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}
tasks.register("bundleLibRuntimeToDirDebug1") {
    doLast {
        val debugDexDir = layout.buildDirectory.dir("transformed")
        //val outputDir = outputs.files.singleFile

        println("Bundle目录: ${debugDexDir}")

    }
}



tasks.register("convertToDex") {
    dependsOn("compileReleaseJavaWithJavac")  // 确保 class 文件已编译

    doLast {
        val sdkPath = android.sdkDirectory
        val buildToolsVersion = android.buildToolsVersion
        val compileSdk = android.compileSdkVersion

        // 构建完整的 d8 路径
        val d8Path = sdkPath.resolve("build-tools/$buildToolsVersion/d8.bat")
        val androidJar = sdkPath.resolve("platforms/$compileSdk/android.jar")


        // 输入文件
        val inputClass = layout.buildDirectory
            .file("intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/imgui/ImGuiView.class")
            .get().asFile

        // 输出目录
        val outputDir = layout.buildDirectory.dir("d8-output").get().asFile

        // 检查文件是否存在
        if (!d8Path.exists()) {
            throw GradleException("d8.bat not found at: ${d8Path.absolutePath}")
        }

        if (!androidJar.exists()) {
            throw GradleException("android.jar not found at: ${androidJar.absolutePath}")
        }

        if (!inputClass.exists()) {
            throw GradleException("Input class not found: ${inputClass.absolutePath}")
        }

        logger.lifecycle("Using d8: ${d8Path.absolutePath}")
        logger.lifecycle("Android JAR: ${androidJar.absolutePath}")
        logger.lifecycle("Input: ${inputClass.absolutePath}")

        // 执行 d8 命令
        try {
            providers.exec {
                commandLine(
                    d8Path.absolutePath,
                    "--lib",
                    androidJar.absolutePath,
                    "--output",
                    "build",
                    inputClass.absolutePath
                )

                // 设置工作目录
                workingDir = project.projectDir

                // 输出日志
                //standardOutput = System.out
                //errorOutput = System.err
            }.result.get()

            logger.lifecycle("✅ D8 conversion completed successfully")
            logger.lifecycle("📁 Output directory: ${outputDir.absolutePath}")

        } catch (e: Exception) {
            throw GradleException("Failed to execute d8: ${e.message}", e)
        }
    }
}

tasks.register("pythonToC") {
    dependsOn("convertToDex")
    doLast {
        val dexFile = file("build/classes.dex")
        val outputFile = file("../app/src/main/cpp/src/dex_data.h")


        dexFile.inputStream().use { input ->
            outputFile.printWriter().use { output ->
                output.println("unsigned char dex_data[] = {")
                var bytesRead = 0
                val buffer = ByteArray(1024)

                while (true) {
                    val count = input.read(buffer)
                    if (count <= 0) break

                    buffer.take(count).forEach { byte ->
                        output.print("0x${byte.toUByte().toString(16).padStart(2, '0')}, ")
                        if (++bytesRead % 16 == 0) output.println()
                    }
                }

                output.println("\n};")
                output.println("const int dex_data_size = $bytesRead;")
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}