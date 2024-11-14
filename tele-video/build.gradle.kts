plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.tele.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        dataBinding = true
    }
    packaging {
        jniLibs {
            pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
            pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
            pickFirsts.add("lib/x86/libc++_shared.so")
            pickFirsts.add("lib/x86_64/libc++_shared.so")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.amazon.chime.sdk)
    implementation(libs.amazon.chime.sdk.media)
    implementation (libs.blur)
    implementation (libs.gson)
    implementation (libs.hb.recorder)
    implementation (libs.ffmpeg)
    implementation (libs.renderscript)
}


publishing {
    publications {
        create("bar", MavenPublication::class) {
            groupId = "tele"
            artifactId = "com.tele.android"
            version = "1.0.3"
            artifact("$buildDir/outputs/aar/tele-video-release.aar")
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.named("publishBarPublicationToMavenLocal") {
    dependsOn(tasks.named("bundleReleaseAar"))
    mustRunAfter(tasks.named("bundleReleaseAar"))
}
