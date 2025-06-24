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
                cppFlags += "-std=c++23"
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
            version = "3.28.0+"
        }
    }
    buildFeatures {
        viewBinding = true
        prefab = true
    }
}


tasks.named("preBuild") {
    //dependsOn(":ImGuiView:assembleDebug")
    dependsOn(":ImGuiView:mergeJars")
    dependsOn(":ImGuiView:pythonToC")


}

dependencies {

    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.lsplant.standalone)
    implementation(libs.dobby)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}