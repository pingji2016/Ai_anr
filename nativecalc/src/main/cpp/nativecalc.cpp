#include <jni.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativecalc_NativeCalc_00024Companion_multiply(JNIEnv*, jobject, jint a, jint b) {
    return a * b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_nativecalc_NativeCalc_00024Companion_sum(JNIEnv* env, jobject, jfloatArray arr) {
    jsize len = env->GetArrayLength(arr);
    jfloat* elems = env->GetFloatArrayElements(arr, nullptr);
    jfloat s = 0;
    for (jsize i = 0; i < len; i++) s += elems[i];
    env->ReleaseFloatArrayElements(arr, elems, 0);
    return s;
}
