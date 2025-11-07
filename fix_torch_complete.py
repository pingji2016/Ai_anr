import os
import torch
import sys

# 禁用所有可能的编译和优化
os.environ.update({
    "TORCH_COMPILE_DISABLE": "1",
    "TORCH_DYNAMO_DISABLE": "1",
    "TORCHINDUCTOR_FORCE_DISABLE_CPP_BUILDER": "1",
    "PYTORCH_JIT": "0",
})

# 在导入任何模块之前设置
torch._C._jit_set_profiling_mode(False)
torch._C._jit_set_profiling_executor(False)
torch._C._set_graph_executor_optimize(False)

def simple_export():
    """使用简单的导出方法"""
    try:
        # 直接导入并调用，避免复杂的参数传递
        from executorch.extension.llm.export import export_llm

        # 使用更简单的参数
        sys.argv = [
            "export_llm",
            "base.checkpoint=stories110M.pt",
            "base.params=params.json",
            "model.dtype_override=fp32",  # 改为fp32避免精度问题
            "export.output_name=stories110m_simple.pte",
            "model.use_kv_cache=False"  # 禁用KV缓存简化导出
        ]

        export_llm.main()

    except Exception as e:
        print(f"简单导出失败: {e}")
        # 尝试备用方法
        alternative_export()

def alternative_export():
    """备用导出方法"""
    print("尝试备用导出方法...")

    try:
        # 手动构建参数
        from executorch.extension.llm.export.export_llm import export_llm_main

        # 直接调用内部函数
        export_llm_main(
            checkpoint_path="stories110M.pt",
            params_path="params.json",
            output_path="stories110m_alt.pte",
            dtype_override="fp32",
            use_kv_cache=False
        )
    except Exception as e:
        print(f"备用方法也失败: {e}")
        # 最后尝试：手动加载和导出
        manual_export()

def manual_export():
    """手动加载模型并导出"""
    print("尝试手动导出...")

    try:
        import json
        import torch

        # 加载参数
        with open("params.json", "r") as f:
            params = json.load(f)

        # 加载模型
        print("加载模型...")
        model = torch.load("stories110M.pt", map_location="cpu")

        # 设置为评估模式
        model.eval()

        # 创建示例输入
        print("准备示例输入...")
        example_input = torch.randint(0, params["vocab_size"], (1, 128), dtype=torch.long)

        # 导出为TorchScript
        print("导出为TorchScript...")
        traced_model = torch.jit.trace(model, example_input)

        # 保存
        traced_model.save("stories110m_manual.pt")
        print("手动导出成功！保存为 stories110m_manual.pt")

    except Exception as e:
        print(f"手动导出失败: {e}")
        print("请考虑使用其他方法或检查模型文件")

if __name__ == "__main__":
    simple_export()