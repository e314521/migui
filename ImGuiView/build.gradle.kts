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
tasks.register("extractDex") {
    dependsOn("assembleRelease")
    val aarFile = layout.buildDirectory.file("outputs/aar/${project.name}-release.aar").get().asFile
    val outputDex = File(rootDir, "ImGuiView.dex")
    val dexOutputDir = File(layout.buildDirectory.asFile.get(), "tmp_dex_output")
    dexOutputDir.deleteRecursively()
    dexOutputDir.mkdirs()
    inputs.file(aarFile)
    doLast {
        if (!aarFile.exists()) {
            throw GradleException("未找到 AAR 文件，请检查路径: ${aarFile.absolutePath}")
        }

        // 1. 创建临时目录并解压 AAR
        val tempDir = File(layout.buildDirectory.asFile.get(), "tmp_aar_extract")
        tempDir.deleteRecursively()
        tempDir.mkdirs()

        project.copy {
            from(project.zipTree(aarFile))
            into(tempDir)
            include("classes.jar")
        }

        val jarFile = File(tempDir, "classes.jar")
        if (!jarFile.exists()) {
            throw GradleException("AAR 中未能解压出 classes.jar")
        }

        // 2. 动态寻找电脑中配置的 Android SDK d8 编译器
        val sdkDir = project.extensions.getByType(com.android.build.gradle.BaseExtension::class.java).sdkDirectory
        val buildToolsVersion = project.extensions.getByType(com.android.build.gradle.BaseExtension::class.java).buildToolsVersion

        // 兼容 Windows / Linux / Mac 的路径写法
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val d8Name = if (isWindows) "d8.bat" else "d8"
        val d8Executable = File(sdkDir, "build-tools/$buildToolsVersion/$d8Name")

        if (!d8Executable.exists()) {
            throw GradleException("未找到 d8 编译工具: ${d8Executable.absolutePath}")
        }

        // 3. 执行命令行，将 jar 编译为最终的 dex
        println("正在通过 d8 编译核心 DEX 文件...")
        println(d8Executable.absolutePath)
        println(outputDex.absolutePath)
        println(jarFile.absolutePath)
        providers.exec {
            commandLine(
                d8Executable.absolutePath,
                "--output",dexOutputDir.absolutePath,
                jarFile.absolutePath
            )

        }.result.get()
        // 5. 将生成的 classes.dex 复制并重命名到项目根目录下
        val generatedDex = File(dexOutputDir, "classes.dex")
        if (generatedDex.exists()) {
            project.copy {
                from(generatedDex)
                into(layout.buildDirectory)
                rename { "ImGuiView.dex" }
            }
            println("==========================================================")
            println("  DEX 转换成功！已保存在项目根目录下: ImGuiView.dex")
            println("==========================================================")
        } else {
            throw GradleException("d8 编译失败，未生成 classes.dex")
        }
        // 4. 清理临时文件夹
        tempDir.deleteRecursively()
        dexOutputDir.deleteRecursively()
        println("==========================================================")
        println("  DEX 转换成功！已保存在项目根目录下: ImGuiView.dex")
        println("==========================================================")
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
        val classDir =layout.buildDirectory.file("intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/imgui/")
        logger.lifecycle("classDir: ${classDir}")
        val classFiles: FileTree = (fileTree(classDir).include("*.class") as FileTree)
        val files: List<File> = classFiles.files.toList()
        val filePathsArray: Array<String> = files.map { it.absolutePath }.toTypedArray()
//
//        var files = ""
//        classFiles.forEach { file: File ->
//            files = files + file.absolutePath + " "
//        }



        // 输入文件
        val inputClass = layout.buildDirectory
            .file("intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/imgui/ImGuiView.class")
            .get().asFile

        val inputClass1 = layout.buildDirectory
            .file("intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/imgui/ImGuiValue.class")
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
        logger.lifecycle("Input: ${files}")

        // 执行 d8 命令
        try {
            providers.exec {
                commandLine(
                    d8Path.absolutePath,
                    "--lib",
                    androidJar.absolutePath,
                    "--output",
                    "build",
                    *filePathsArray
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
    dependsOn("extractDex")
    doLast {
        //val dexFile = file("build/classes.dex")
        val dexFile = layout.buildDirectory
            .file("ImGuiView.dex")
            .get().asFile
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