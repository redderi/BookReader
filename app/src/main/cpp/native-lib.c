#include <jni.h>
#include <stdlib.h>
#include <string.h>

JNIEXPORT jfloatArray JNICALL
Java_com_redderi_bookreader_pages_NativeColorGenerator_generateRandomColor(JNIEnv *env, jobject thiz, jstring username) {
    // Получаем строку из Java
    const char *user = (*env)->GetStringUTFChars(env, username, 0);

    unsigned char firstChar = user[0];
    size_t length = strlen(user);

    int r = (firstChar * 22 + length * 4) % 256;
    int g = (firstChar * 17 + length * 5) % 256;
    int b = (firstChar * 67 + length * 6) % 256;

    // Освобождаем память строки
    (*env)->ReleaseStringUTFChars(env, username, user);

    jfloatArray result = (*env)->NewFloatArray(env, 3);
    if (result == NULL) {
        return NULL;
    }

    jfloat colors[3] = {r / 255.0f, g / 255.0f, b / 255.0f};

    (*env)->SetFloatArrayRegion(env, result, 0, 3, colors);

    return result;
}
