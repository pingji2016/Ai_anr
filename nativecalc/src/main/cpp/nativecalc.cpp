#include <jni.h>
#include <android/bitmap.h>

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

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_nativecalc_NativeCalc_flipHorizontalBitmap(JNIEnv* env, jobject, jobject srcBitmap) {
    AndroidBitmapInfo info;
    if (AndroidBitmap_getInfo(env, srcBitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return nullptr;

    jclass bitmapCls = env->FindClass("android/graphics/Bitmap");
    jclass configCls = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888Field = env->GetStaticFieldID(configCls, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = env->GetStaticObjectField(configCls, argb8888Field);
    jmethodID createBitmap = env->GetStaticMethodID(bitmapCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject dstBitmap = env->CallStaticObjectMethod(bitmapCls, createBitmap, (jint)info.width, (jint)info.height, argb8888);

    void* srcPixels = nullptr;
    void* dstPixels = nullptr;
    if (AndroidBitmap_lockPixels(env, srcBitmap, &srcPixels) != ANDROID_BITMAP_RESULT_SUCCESS) return nullptr;
    if (AndroidBitmap_lockPixels(env, dstBitmap, &dstPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, srcBitmap);
        return nullptr;
    }

    uint32_t* src = (uint32_t*)srcPixels;
    uint32_t* dst = (uint32_t*)dstPixels;
    int w = (int)info.width;
    int h = (int)info.height;
    for (int y = 0; y < h; ++y) {
        uint32_t* srcRow = src + y * w;
        uint32_t* dstRow = dst + y * w;
        for (int x = 0; x < w; ++x) {
            dstRow[x] = srcRow[w - 1 - x];
        }
    }

    AndroidBitmap_unlockPixels(env, srcBitmap);
    AndroidBitmap_unlockPixels(env, dstBitmap);
    return dstBitmap;
}
