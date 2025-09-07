端侧识别APP
tf_efficientnet_lite0.pth 可以官网下载
TensorFlow Hub 上搜就行

# TensorFlow Lite 集成说明

## 当前状态

当前应用使用模拟分类器进行演示，避免了TensorFlow Lite依赖问题。

## 要使用真实的EfficientNet Lite0模型

### 1. 添加TensorFlow Lite依赖

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")
}
```

### 2. 模型转换

将PyTorch模型（.pth）转换为TensorFlow Lite格式（.tflite）：

```bash
# 使用torch2tflite工具
pip install torch2tflite
torch2tflite --model tf_efficientnet_lite0.pth --output efficientnet_lite0.tflite
```

### 3. 更新ImageClassifier.kt

恢复TensorFlow Lite Interpreter的使用：

```kotlin
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options

class ImageClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    
    private fun loadModel() {
        try {
            val modelFile = loadModelFile("efficientnet_lite0.tflite")
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(modelFile, options)
            Log.d("ImageClassifier", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("ImageClassifier", "Error loading model: ${e.message}")
        }
    }
    
    // ... 其他方法
}
```

### 4. 文件放置

将转换后的 `.tflite` 文件放置在：
```
app/src/main/assets/efficientnet_lite0.tflite
```

## 模拟分类器说明

当前使用的模拟分类器会随机返回以下类别之一：
- cat, dog, bird, car, person, tree, flower, house

置信度在60%-95%之间随机生成，用于演示应用功能。

## 故障排除

如果遇到TensorFlow Lite导入问题：

1. 确保Gradle同步完成
2. 检查网络连接（下载依赖需要）
3. 清理项目：Build -> Clean Project
4. 重新构建：Build -> Rebuild Project

## 性能考虑

- EfficientNet Lite0模型大小约5.3MB
- 在移动设备上推理时间约50-100ms
- 内存占用约20-30MB


PyTorch (.pth) → ONNX → TensorFlow → TensorFlow Lite (.tflite)