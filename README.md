An application for android that can scan blood spills and estimate the volume of blood.

First download opencv for android https://opencv.org/releases/

import module in android studio
new -> import module -> module name = :opencv
directory C:\Users\jcull\Downloads\opencv-4.9.0-android-sdk\OpenCV-android-sdk\sdk
add     implementation(project(":opencv"))
into app/build.gradle.kts

add include(":opencv")
into settings.gradle.kts

//   Edit "settings.gradle" and add these lines:
//   include ':opencv'

make sure that this is true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

in src/main/build.gradle.kts

also add 
    implementation(project(":opencv"))




go into opencv
C:\Users\jcull\Downloads\opencv-4.9.0-android-sdk(1)\OpenCV-android-sdk\sdk\native\libs
create new folder jniLibs in
src/main
move the item from native/libs into there

go into 
app/cpp/cmakeLists.txt
change set(OpenCV_DIR C:/Users/jcull/Downloads/opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni)

make sure you are running java 11

