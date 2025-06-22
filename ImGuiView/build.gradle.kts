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


tasks.register("mergeJars") {
    tasks.named("assembleDebug")
    dependsOn("assembleDebug")
    doLast{
        println(layout.buildDirectory.dir("intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes\\com\\imgui\\ImGuiView.class").get())
        val dexPath = layout.buildDirectory.dir("intermediates\\javac\\debug\\compileDebugJavaWithJavac\\classes\\com\\imgui\\*.class").get()
        val androidpath = "${android.sdkDirectory}/platforms/${android.compileSdkVersion}/android.jar"
        val dxPath = "${android.sdkDirectory.path}/build-tools/${android.buildToolsVersion}\\d8.bat"

        exec {
            commandLine(
                dxPath,
                "--lib",
                androidpath,
                "--output",
                "build",
                "build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/com/imgui/ImGuiView.class",
            )
        }
    }











    //val outputDir = getPath()
    //println("Java编译输出目录: $outputDir")
    //destinationDirectory.set(file("build/libs"))
    //archiveFileName.set("merged.jar")
}



tasks.register("pythonToC") {
    dependsOn("mergeJars")
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