#!/bin/bash
# Arranges the flat uploaded files into a normal Android project and writes build files.
set -e
B=app/src/main/java/com/demopay/app
R=app/src/main/res
mkdir -p $B $R/drawable $R/values $R/xml $R/mipmap-anydpi-v26
mv -f ./*.kt $B/

cat > settings.gradle.kts <<'EOF'
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "DemoPay"
include(":app")
EOF

cat > build.gradle.kts <<'EOF'
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
EOF

cat > gradle.properties <<'EOF'
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
EOF

cat > app/build.gradle.kts <<'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.demopay.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.demopay.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
EOF

cat > app/src/main/AndroidManifest.xml <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <!-- Offline-only app: make sure no library can add internet access -->
    <uses-permission android:name="android.permission.INTERNET" tools:node="remove" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="DemoPay"
        android:theme="@style/Theme.DemoPay">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|keyboardHidden|smallestScreenSize|screenLayout|uiMode">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
EOF

cat > $R/values/themes.xml <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.DemoPay" parent="@android:style/Theme.Material.Light.NoActionBar" />
</resources>
EOF

cat > $R/xml/file_paths.xml <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="receipts" path="receipts/" />
</paths>
EOF

cat > $R/drawable/ic_notify.xml <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z" />
</vector>
EOF

cat > $R/drawable/ic_launcher_background.xml <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="0" android:startY="0"
                android:endX="108" android:endY="108"
                android:type="linear">
                <item android:color="#36C25F" android:offset="0.0" />
                <item android:color="#0B5D27" android:offset="1.0" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
EOF

cat > $R/drawable/ic_launcher_foreground.xml <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path
        android:fillColor="#33FFFFFF"
        android:pathData="M54,54 m-32,0 a32,32 0 1,0 64,0 a32,32 0 1,0 -64,0" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,54 m-26,0 a26,26 0 1,0 52,0 a26,26 0 1,0 -52,0" />
    <path
        android:strokeColor="#1B8A3C"
        android:strokeWidth="7"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M42,55 L51,64 L67,45" />
</vector>
EOF

for n in ic_launcher ic_launcher_round; do
cat > $R/mipmap-anydpi-v26/$n.xml <<'EOF'
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
EOF
done
