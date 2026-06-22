plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.1" apply false
}

android {
    namespace = "com.minhhuycoder.vidi"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.minhhuycoder.vidi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Nhập Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Hai thư viện Firebase duy nhất nhóm m cần
    implementation("com.google.firebase:firebase-auth-ktx")      // Khang dùng
    implementation("com.google.firebase:firebase-firestore-ktx") // Kiều, Huy, Hiếu dùng

    // THÊM THƯ VIỆN NÀY: Để load link ảnh từ mạng về (Thay thế cho Storage)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}