# ExecuTorch LLM Android 演示应用

此应用作为一个有价值的资源，可以激发您的创造力，并提供基础代码，您可以针对特定用例进行自定义和适配。

请深入了解并开始探索我们的演示应用！我们期待您的反馈，并很高兴看到您的创新想法。


## 核心概念
通过此演示应用，您将学习许多核心概念，例如：
* 如何准备 Llama 模型、构建 ExecuTorch 库以及在不同委托上进行模型推理
* 通过 JNI 层暴露 ExecuTorch 库
* 熟悉 ExecuTorch 当前的面向应用能力

目标是让您了解 ExecuTorch 提供的支持类型，并能够轻松地将其用于您的用例。

## 支持的模型
总体而言，此应用支持的模型如下（因委托而异）：
* Llama 3.2 量化 1B/3B
* Llama 3.2 1B/3B（BF16）
* Llama Guard 3 1B
* Llama 3.1 8B
* Llama 3 8B
* Llama 2 7B
* LLaVA-1.5 视觉模型（仅 XNNPACK）
* Qwen 3 0.6B、1.7B 和 4B
* Voxtral Mini 3B
* Gemma 3 4B

## 构建 APK
首先需要注意的是，默认情况下，应用依赖于 Maven Central 上的 [ExecuTorch 库](https://central.sonatype.com/artifact/org.pytorch/executorch-android)。它使用最新的 `org.pytorch:executorch-android` 包，该包包含所有默认内核库（portable、quantized、optimized）、LLM 自定义库和 XNNPACK 后端。

如果您想使用预构建的 ExecuTorch 库，则无需修改。

但是，您可以构建自己的 ExecuTorch Android 库（AAR 文件）。将文件复制到 `app/libs/executorch.aar`。在 `gradle.properties` 文件中，添加一行 `useLocalAar=true`，以便 gradle 使用本地 AAR 文件。

[此页面](https://github.com/pytorch/executorch/blob/main/extension/android/README.md) 包含构建 ExecuTorch Android 库的文档。

目前 ExecuTorch 支持 4 种委托。一旦您确定了选择的委托，请选择相应的 README 链接，以获取从环境设置到导出模型、构建 ExecuTorch 库和在设备上运行应用的完整端到端说明：

| 委托      | 资源 |
| ------------- | ------------- |
| XNNPACK（基于 CPU 的库）  | [链接](https://github.com/meta-pytorch/executorch-examples/blob/main/llm/android/LlamaDemo/docs/delegates/xnnpack_README.md) |
| QNN（高通 AI 加速器）  | [链接](https://github.com/meta-pytorch/executorch-examples/blob/main/llm/android/LlamaDemo/docs/delegates/qualcomm_README.md) |
| MediaTek（联发科 AI 加速器）  | [链接](https://github.com/meta-pytorch/executorch-examples/blob/main/llm/android/LlamaDemo/docs/delegates/mediatek_README.md)  |
| Vulkan | [链接](https://github.com/pytorch/executorch/blob/main/examples/vulkan/README.md) |


## 如何使用应用

本节将提供使用应用的主要步骤，以及 ExecuTorch API 的代码片段。

对于加载应用、开发和设备运行，我们推荐使用 Android Studio：
1. 打开 Android Studio，选择"打开现有 Android Studio 项目"以打开包含此 README.md 文件的目录。
2. 运行应用（^R）。这将构建并在手机上启动应用。

### 打开应用

以下是应用的 UI 功能。

选择设置小部件以开始选择模型、其参数和任何提示。
<p align="center">
<img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/opening_the_app_details.png" style="width:800px">
</p>



### 选择模型和参数

一旦您选择了模型、分词器和模型类型，您就可以点击"加载模型"让应用加载模型并返回到主聊天活动。
<p align="center">
      <img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/settings_menu.png" style="width:300px">
</p>



可选参数：
* Temperature（温度）：默认为 0，您也可以调整模型的温度。任何调整后模型将重新加载。
* System Prompt（系统提示）：更适用于高级用户，无需任何格式化，您可以输入系统提示。例如，"你是一个旅行助手"或"给我一个简短的回答"。
* User Prompt（用户提示）：更适用于高级用户，如果您想手动输入提示，可以通过修改 `{{user prompt}}` 来实现。您也可以修改特殊标记。更改后，返回主聊天活动发送。

#### ExecuTorch 应用 API

```java
// 返回主聊天活动后
mModule = new LlmModule(
            ModelUtils.getModelCategory(mCurrentSettingsFields.getModelType()),
            modelPath,
            tokenizerPath,
            temperature,
            dataPath);
int loadResult = mModule.load();
```

* `modelCategory`：指示是纯文本模型还是视觉模型
* `modePath`：.pte 文件的路径
* `tokenizerPath`：分词器文件的路径
* `temperature`：模型参数，用于调整模型输出的随机性
* `dataPath`：一个或多个 .ptd 文件的路径


### 用户提示
一旦模型成功加载，输入任何提示并点击发送（即生成）按钮将其发送到模型。
<p align="center">
<img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/load_complete_and_start_prompt.png" style="width:300px">
</p>

您也可以提供更多后续问题。
<p align="center">
<img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/chat.png" style="width:300px">
</p>

#### ExecuTorch 应用 API

```java
mModule.generate(prompt,sequence_length, MainActivity.this);
```
* `prompt`：用户格式化的提示
* `sequence_length`：响应提示生成的令牌数
* `MainActivity.this`：指示回调函数（OnResult()、OnStats()）存在于此类中。

[*LLaVA-1.5：仅适用于 XNNPACK 委托*]

对于 LLaVA-1.5 实现，在设置菜单中选择导出的 LLaVA .pte 和分词器文件并加载模型。之后，您可以从图库发送图像或拍摄实时图片以及文本提示到模型。

<p align="center">
<img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/llava_example.png" style="width:300px">
</p>


### 生成的输出
为了显示后续问题的完成情况，这是来自模型的完整详细响应。
<p align="center">
<img src="https://raw.githubusercontent.com/pytorch/executorch/refs/heads/main/docs/source/_static/img/chat_response.png" style="width:300px">
</p>

### 示例输出

#### Llama 3.2 1B


https://github.com/user-attachments/assets/b28530a1-bec4-45a4-8e46-ee4eed39b5bb


#### Llava - Llama 2 7b


https://github.com/user-attachments/assets/161929b9-2b71-411a-9193-0b9eae7170a1


#### Gemma 3 4B


https://github.com/user-attachments/assets/5a57af00-22f7-473e-abdb-5aa9bb708b57


#### Voxtral Mini 3B


https://github.com/user-attachments/assets/9ce361ce-9a59-4f32-b29a-2b24cc1cb2f7


#### ExecuTorch 应用 API

确保您在 `mModule.generate()` 中提供的回调类具有以下函数。对于此示例，它是 `MainActivity.this`。
```java
  @Override
  public void onResult(String result) {
    //...result 包含来自响应的令牌
    //.. onResult 将继续被调用，直到响应完成
  }

  @Override
  public void onStats(String stats) {
    //... 将是一个 json。有关字段定义，请参见 extension/llm/stats.h
  }

```

## 测试工具
您可以运行测试工具进行健全性检查。测试加载模型 pte 文件和 tokenizer.bin 文件，位于 `/data/local/tmp/llama` 下。

### 模型准备
您需要首先安装 [executorch python 包](https://docs.pytorch.org/executorch/stable/getting-started.html#installation)。
```sh
curl -C - -Ls "https://huggingface.co/karpathy/tinyllamas/resolve/main/stories110M.pt" --output stories110M.pt
curl -C - -Ls "https://raw.githubusercontent.com/karpathy/llama2.c/master/tokenizer.model" --output tokenizer.model
# 创建 params.json 文件
touch params.json
echo '{"dim": 768, "multiple_of": 32, "n_heads": 12, "n_layers": 12, "norm_eps": 1e-05, "vocab_size": 32000}' > params.json
python -m executorch.extension.llm.export.export_llm base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True
python -m pytorch_tokenizers.tools.llama2c.convert -t tokenizer.model -o tokenizer.bin
```
### 推送模型
```sh
adb shell mkdir -p /data/local/tmp/llama
adb push stories110m_h.pte /data/local/tmp/llama
adb push tokenizer.bin /data/local/tmp/llama
```

### 运行测试
```sh
./gradlew connectedAndroidTest
```

## 报告问题
如果您在遵循本教程时遇到任何错误或问题，请在 [Github](https://github.com/pytorch/executorch/issues/new) 上提交错误/问题，或加入我们的 discord [这里](https://lnkd.in/gWCM4ViK)。
