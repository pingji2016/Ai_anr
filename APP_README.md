# TensorFlow Lite 图像分类应用

这是一个使用TensorFlow Lite进行图像分类的Android应用，基于EfficientNet Lite0模型架构。

## 功能特性

- 📱 支持从相册选择图片进行分类
- 🖼️ 内置示例图片用于测试
- 🤖 使用TensorFlow Lite进行高效的端侧推理
- 📊 显示分类结果和置信度
- 🎨 现代化的Material Design 3界面

## 技术架构

### 核心组件

1. **ImageClassifier.kt** - 图像分类核心类
   - 模型加载和管理
   - 图像预处理
   - 推理执行
   - 结果解析

2. **MainActivity.kt** - 主界面和用户交互
   - 图片选择
   - 分类触发
   - 结果显示

### 技术栈

- **框架**: Android Jetpack Compose
- **机器学习**: TensorFlow Lite
- **图像处理**: Android Bitmap API
- **UI**: Material Design 3

## 模型说明

### EfficientNet Lite0

EfficientNet Lite0是一个轻量级的图像分类模型，专为移动设备优化：

- **输入尺寸**: 224x224x3
- **输出类别**: 1000个ImageNet类别
- **模型大小**: 约5.3MB
- **推理速度**: 在移动设备上可实现实时推理

### 模型转换

原始模型文件为`.pth`格式（PyTorch），需要转换为TensorFlow Lite格式：

```bash
# 使用torch2tflite工具转换
torch2tflite --model efficientnet_lite0.pth --output efficientnet_lite0.tflite
```

## 安装和使用

### 环境要求

- Android Studio Arctic Fox或更高版本
- Android SDK API 24+
- Kotlin 1.8+

### 构建步骤

1. 克隆项目
```bash
git clone <repository-url>
cd Ai_anr
```

2. 打开Android Studio并导入项目

3. 同步Gradle依赖

4. 构建并运行应用

### 使用说明

1. **启动应用** - 应用会自动加载内置的示例图片
2. **选择图片** - 点击"选择图片"按钮从相册选择图片
3. **开始分类** - 点击"开始分类"按钮进行图像分类
4. **查看结果** - 分类结果会显示在界面上，包括类别名称和置信度

## 项目结构

```
app/
├── src/main/
│   ├── assets/                 # 模型文件和资源
│   │   ├── img.png            # 示例图片
│   │   └── tf_efficientnet_lite0.pth  # 模型文件
│   ├── java/com/example/tfdemo/
│   │   ├── MainActivity.kt     # 主Activity
│   │   ├── ImageClassifier.kt # 图像分类器
│   │   └── ui/theme/          # UI主题
│   ├── res/                   # 资源文件
│   └── AndroidManifest.xml    # 应用清单
├── build.gradle.kts           # 构建配置
└── proguard-rules.pro         # 混淆规则
```

## 依赖库

```kotlin
// TensorFlow Lite
implementation("org.tensorflow:tensorflow-lite:2.14.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")

// 图像处理
implementation("androidx.exifinterface:exifinterface:1.3.6")
```

## 权限配置

应用需要以下权限：

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## 性能优化

### 模型优化

- 使用量化技术减少模型大小
- 优化输入预处理流程
- 实现模型缓存机制

### 内存管理

- 及时释放Bitmap资源
- 使用对象池减少GC压力
- 合理设置线程池大小

## 故障排除

### 常见问题

1. **模型加载失败**
   - 检查模型文件是否存在于assets目录
   - 确认模型格式为TensorFlow Lite

2. **分类结果不准确**
   - 确保输入图片尺寸正确（224x224）
   - 检查图像预处理流程

3. **应用崩溃**
   - 查看Logcat输出
   - 检查内存使用情况

## 扩展功能

### 可能的改进

- [ ] 支持更多模型格式
- [ ] 添加批量处理功能
- [ ] 实现模型热更新
- [ ] 添加结果可视化
- [ ] 支持视频分类

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

---

**注意**: 当前实现包含模拟分类器用于演示目的。要使用真实的EfficientNet Lite0模型，需要将PyTorch模型转换为TensorFlow Lite格式。
