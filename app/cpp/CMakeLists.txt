cmake_minimum_required(VERSION 3.22.1)

project("testcpp")

set(OpenCV_DIR C:/Users/Admin/Desktop/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

include_directories(${OpenCV_DIR}/jni/include)
add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)

add_library(
        ${CMAKE_PROJECT_NAME} SHARED
        testcpp.cpp
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        ${OpenCV_LIBS}
        android
        log
)