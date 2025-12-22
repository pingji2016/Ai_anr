# ExecuTorch 模型导出问题解决方案

## 问题描述
在 Windows 上执行 `python -m executorch.extension.llm.export.export_llm` 时出现 C++ 编译错误：
```
fatal error C1083: 无法打开包括文件: "algorithm": No such file or directory
```

## 解决方案

### 方案 1：禁用 TorchInductor 编译（推荐，最简单）

在执行命令前设置环境变量来禁用 JIT 编译：

**PowerShell:**
```powershell
$env:TORCH_COMPILE_DEBUG = "1"
$env:TORCHINDUCTOR_CACHE_DIR = ""
python -m executorch.extension.llm.export.export_llm base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True
```

**CMD:**
```cmd
set TORCH_COMPILE_DEBUG=1
set TORCHINDUCTOR_CACHE_DIR=
python -m executorch.extension.llm.export.export_llm base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True
```

**或者使用 Python 代码设置：**
```python
import os
os.environ['TORCH_COMPILE_DEBUG'] = '1'
os.environ['TORCHINDUCTOR_CACHE_DIR'] = ''
```

### 方案 2：安装 Visual Studio Build Tools

如果必须使用编译功能，需要安装 Visual Studio Build Tools：

1. 下载并安装 [Visual Studio Build Tools](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022)
2. 在安装时选择 "C++ 生成工具" 工作负载
3. 确保包含 Windows SDK 和 CMake 工具

### 方案 3：使用预编译的 PyTorch 版本

确保使用预编译的 PyTorch wheel 包，而不是从源码编译的版本：

```bash
pip uninstall torch torchvision
pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
```

### 方案 4：修改导出命令使用禁用编译的配置

在命令中添加参数来禁用编译：

```bash
python -m executorch.extension.llm.export.export_llm \
    base.checkpoint=stories110M.pt \
    base.params=params.json \
    model.dtype_override="fp16" \
    export.output_name=stories110m_h.pte \
    model.use_kv_cache=True \
    model.use_sdpa_with_kv_cache=False
```

### 方案 5：在 Python 脚本中设置

创建一个 Python 脚本来执行导出：

```python
import os
import sys

# 禁用 TorchInductor 编译
os.environ['TORCH_COMPILE_DEBUG'] = '1'
os.environ['TORCHINDUCTOR_CACHE_DIR'] = ''
os.environ['TORCH_LOGS'] = '+dynamo'

# 使用 Python 直接调用导出函数
from executorch.extension.llm.export.export_llm import main

if __name__ == '__main__':
    sys.argv = [
        'export_llm.py',
        'base.checkpoint=stories110M.pt',
        'base.params=params.json',
        'model.dtype_override=fp16',
        'export.output_name=stories110m_h.pte',
        'model.use_kv_cache=True'
    ]
    main()
```

## 推荐方案

**最简单有效的方法**：使用 PowerShell 脚本 `export_model.ps1`（推荐）或 Python 脚本 `export_model.py`。

### 使用 PowerShell 脚本（推荐）

```powershell
# 使用默认参数
.\export_model.ps1

# 或自定义参数
.\export_model.ps1 -ModelClass "stories110m" -Checkpoint "stories110M.pt" -Params "params.json" -OutputName "stories110m_h.pte"
```

### 使用 Python 脚本

```bash
python export_model.py base.model_class=stories110m base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True
```

### 直接使用 PowerShell 命令

```powershell
$env:TORCHDYNAMO_DISABLE="1"
$env:TORCHINDUCTOR_COMPILE_BACKEND="none"
$env:TORCHINDUCTOR_CACHE_DIR="$env:TEMP\torch_cache"
New-Item -ItemType Directory -Force -Path "$env:TEMP\torch_cache" | Out-Null
python -m executorch.extension.llm.export.export_llm base.model_class=stories110m base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True
```

## 验证

导出成功后，应该会生成 `stories110m_h.pte` 文件。

