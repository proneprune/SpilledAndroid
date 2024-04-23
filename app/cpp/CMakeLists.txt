cmake_minimum_required(VERSION 3.22.1)

project("testcpp")

set(OpenCV_STATIC ON)
<<<<<<< HEAD
set(OpenCV_DIR C:/Users/jcull/Downloads/opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni)
=======
set(OpenCV_DIR C:/tools/opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni)
>>>>>>> 984c6bbf32e96fb9442d17501897c51e3afd7946
find_package(OpenCV REQUIRED)

add_library(
        ${CMAKE_PROJECT_NAME} SHARED
        testcpp.cpp
        Test.cpp
)

find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
)