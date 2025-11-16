正在查看关键文件以了解项目结构和功能。


[4 tools called]


查看 README 和其他相关文件以获取更多信息：


[2 tools called]


## 项目概述

这是一个在 Android 设备上使用 ExecuTorch 对 CIFAR-10 模型进行微调（Fine-tuning）的演示应用。

### 核心功能

1. 模型训练/微调
    - 在设备上直接训练神经网络模型
    - 使用 SGD 优化器
    - 支持多轮训练（epochs）

2. 模型评估
    - 在测试集上评估模型性能
    - 计算准确率和损失值

3. 数据处理
    - 加载 CIFAR-10 数据集（32x32 彩色图像，10 个类别）
    - 图像预处理和数据增强（训练时：padding、随机裁剪、水平翻转、归一化；测试时：仅归一化）

### 技术栈

- 框架：ExecuTorch（PyTorch 的移动端执行框架）
- 语言：Kotlin
- 模型：CIFAR-10 分类模型（`.pte` 和 `.ptd` 格式）
- 数据集：CIFAR-10（飞机、汽车、鸟、猫、鹿、狗、青蛙、马、船、卡车）

### 主要组件

1. `MainActivity.kt`：主活动
    - 加载模型和数据
    - 实现训练和评估逻辑
    - UI 交互（两个按钮：Fine-tune 和 Evaluate）

2. `ImageTransformations.kt`：图像预处理工具类
    - 数据增强（训练时）
    - 归一化处理

3. 资源文件：
    - `cifar10_model.pte`：ExecuTorch 模型文件
    - `generic_cifar.ptd`：模型数据文件
    - CIFAR-10 数据集文件（`train_data.bin`、`test_data.bin`）

### 工作流程

1. 应用启动时：
    - 初始化 ExecuTorch
    - 从 assets 加载模型和数据文件
    - 加载 CIFAR-10 数据集

2. 用户点击 "Fine-tune" 按钮：
    - 开始训练循环（默认 5 个 epochs）
    - 每个 epoch 后评估验证集
    - 显示训练和验证准确率

3. 用户点击 "Evaluate" 按钮：
    - 在测试集上评估模型
    - 显示测试准确率和损失

### 训练参数

- 批次大小（batch size）：4
- 训练轮数（epochs）：5
- 学习率：0.001
- 动量（momentum）：0.9
- 训练样本数：1000
- 测试样本数：1000

这个项目展示了如何在 Android 设备上直接进行深度学习模型的训练和微调，无需依赖云端服务。

你想了解哪个部分？例如：
- 代码实现细节
- 如何修改训练参数
- 如何添加新功能
- 其他问题