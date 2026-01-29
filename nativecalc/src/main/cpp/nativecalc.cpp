#include <jni.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativecalc_NativeCalc_multiply(JNIEnv*, jobject, jint a, jint b) {
    return a * b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_nativecalc_NativeCalc_sum(JNIEnv* env, jobject, jfloatArray arr) {
    jsize len = env->GetArrayLength(arr);
    jfloat* elems = env->GetFloatArrayElements(arr, nullptr);
    jfloat s = 0;
    for (jsize i = 0; i < len; i++) s += elems[i];
    // Not modifying array: use JNI_ABORT to avoid copy-back
    env->ReleaseFloatArrayElements(arr, elems, JNI_ABORT);
    return s;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_nativecalc_NativeCalc_callJavaLog(JNIEnv* env, jobject, jobject logger, jstring msg) {
    jclass loggerCls = env->GetObjectClass(logger);
    jmethodID logMethod = env->GetMethodID(loggerCls, "log", "(Ljava/lang/String;)V");
    if (logMethod != nullptr) {
        env->CallVoidMethod(logger, logMethod, msg);
    }
}
