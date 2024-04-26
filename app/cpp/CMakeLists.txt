cmake_minimum_required(VERSION 3.22.1)

project("testcpp")

set(OpenCV_STATIC ON)


set(OpenCV_DIR C:/Users/Admin/Desktop/OpenCV-android-sdk/sdk/native/jni)

find_package(OpenCV REQUIRED)

add_library(
        ${CMAKE_PROJECT_NAME} SHARED
        OpenCV_Func.cpp
)

find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
)