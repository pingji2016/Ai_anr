#include <jni.h>
#include <android/bitmap.h>
#include <arm_neon.h>
#include <omp.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativecalc_NativeCalc_multiply(JNIEnv*, jobject, jint a, jint b) {
    return a * b;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_nativecalc_NativeCalc_sum(JNIEnv* env, jobject, jfloatArray arr) {
    jsize len = env->GetArrayLength(arr);
    jfloat* elems = env->GetFloatArrayElements(arr, nullptr);
    
    float32x4_t sum_vec = vdupq_n_f32(0.0f);
    jsize i = 0;
    for (; i <= len - 4; i += 4) {
        float32x4_t data = vld1q_f32(elems + i);
        sum_vec = vaddq_f32(sum_vec, data);
    }
    
    float s = vgetq_lane_f32(sum_vec, 0) + vgetq_lane_f32(sum_vec, 1) + 
              vgetq_lane_f32(sum_vec, 2) + vgetq_lane_f32(sum_vec, 3);
              
    for (; i < len; i++) s += elems[i];
    
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

    #pragma omp parallel for
    for (int y = 0; y < h; ++y) {
        uint32_t* srcRow = src + y * w;
        uint32_t* dstRow = dst + y * w;
        int x = 0;
        // NEON optimization: process 4 pixels at a time
        for (; x <= w - 4; x += 4) {
            uint32x4_t data = vld1q_u32(srcRow + (w - 4 - x));
            // Reverse 4 pixels: [p0, p1, p2, p3] -> [p3, p2, p1, p0]
            uint32x4_t rev = vrev64q_u32(data); // [p1, p0, p3, p2]
            rev = vcombine_u32(vget_high_u32(rev), vget_low_u32(rev)); // [p3, p2, p1, p0]
            vst1q_u32(dstRow + x, rev);
        }
        // Tail handling
        for (; x < w; ++x) {
            dstRow[x] = srcRow[w - 1 - x];
        }
    }

    AndroidBitmap_unlockPixels(env, srcBitmap);
    AndroidBitmap_unlockPixels(env, dstBitmap);
    return dstBitmap;
}
