#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_kr_ac_kaist_cs496_opencvproject_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}