# TensorFlow Lite & ExecuTorch 综合演示应用 (AI_ANR)

本项是一个集成了多种 AI 技术的 Android 演示应用，涵盖了图像分类、大语言模型推理、传感器数据采集、多线程下载以及高性能 JNI 计算。

## 核心优化与特性

### 1. 高性能 JNI & SIMD 优化 (nativecalc 模块)
- **NEON 指令集加速**: 针对像素反转 (`flipHorizontalBitmap`) 和数组求和 (`sum`) 使用了 ARM NEON SIMD 指令集，每次并行处理 128 位数据，显著提升处理速度。
- **OpenMP 并行处理**: 在 C++ 层开启了 OpenMP 多线程支持，利用多核 CPU 并行处理图像行。
- **JNI 缓存 (JNI_OnLoad)**: 在库加载时缓存了类引用和方法 ID，减少了频繁调用 JNI 时的查找开销。
- **JNI_ABORT 优化**: 在不需要回写 Java 数组的情况下，使用 `JNI_ABORT` 释放数组，减少内存复制开销。

### 2. 灵活的构建风味 (Build Flavors)
- **CPU/GPU 风味拆分**: 针对 TensorFlow Lite 推理拆分了 `cpu` 和 `gpu` 两个构建风味。
  - `cpu` 版本：不包含 GPU Delegate 原生库，包体积更小。
  - `gpu` 版本：包含全套 GPU 加速支持，性能更强。
- **ABI 限制**: 默认仅针对 `arm64-v8a` 进行构建，大幅缩减 APK 体积。

### 3. 工程化与架构优化
- **Version Catalog (libs.versions.toml)**: 全项目依赖统一由版本目录管理，确保依赖版本的一致性与可维护性。
- **SDK & JVM 统一**: 全模块统一使用 `compileSdk 35`、`targetSdk 35` 和 `Java 17`，保持构建一致性。
- **模块化设计**: 拆分为 `app`、`db`、`gyro`、`net`、`cifar10`、`nativecalc` 等多个功能模块，低耦合高复用。

### 4. 传感器与数据采集 (gyro 模块)
- **批量写入优化**: 陀螺仪数据采集使用缓冲区机制，每 20 条数据或 1 秒进行一次批量 Room 写入，极大降低数据库事务开销。
- **协程生命周期管理**: 使用 `LaunchedEffect` 和 `DisposableEffect` 确保传感器监听与数据写入任务在组件销毁时正确释放。

### 5. 增强型网络下载 (net 模块)
- **多线程断点下载**: 支持并发下载，并自动探测服务器是否支持 `Range` 请求进行单/多线程切换。
- **稳定性增强**: 增加超时设置、User-Agent 模拟、进度节流更新以及写入失败时的自动重试机制。

---

## 项目模块说明

1. **app 模块**：主应用入口，包含 TFLite 图像分类演示、JNI 性能对比等。
2. **migrate 模块**：ExecuTorch LLM 演示应用，支持多种 Llama/Qwen 等模型推理。
3. **nativecalc 模块**：高性能 JNI 计算库，提供 SIMD 优化的图像处理与数学运算。
4. **db 模块**：基于 Room 的统一数据库访问层。
5. **gyro 模块**：提供传感器数据采集与展示的 Compose 组件。
6. **net 模块**：高性能、可扩展的多线程下载框架。
7. **cifar10 模块**：基于 CIFAR-10 数据集的图像分类示例。

---

## 快速开始

### 构建命令
- **构建 CPU Release APK**: `.\gradlew.bat :app:assembleCpuRelease`
- **构建 GPU Release APK**: `.\gradlew.bat :app:assembleGpuRelease`
- **构建 Native SO 库**: `.\gradlew.bat :nativecalc:externalNativeBuildRelease`

### 环境要求
- **Android Studio Koala+**
- **JDK 17**
- **Android NDK & CMake** (用于构建 nativecalc 模块)

---

## 功能对比与测试

在应用主界面的 Demo 列表中，您可以找到：
- **Image Flip**: 比较 Java Matrix 变换与 C++ NEON 优化版图片反转的耗时。
- **Native Calc Benchmark**: 比较 Java 与 C++ JNI 的数值计算性能。
- **Sensor Data**: 查看实时传感器曲线并记录数据到数据库。
- **Network Download**: 体验带进度的多线程下载功能。


