#include <jni.h>
#include <string>




extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_blodpool_MainActivity_getTest(JNIEnv *env, jobject thiz) {

    std::string ts = "tet from c++ fff";

    return env->NewStringUTF(ts.c_str());
}