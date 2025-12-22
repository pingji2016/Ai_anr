#!/usr/bin/env python
"""
ExecuTorch LLM 模型导出脚本 - Windows 兼容版本
解决 Windows 上 C++ 编译错误问题
"""

import os
import sys
import tempfile

# 禁用 TorchInductor 编译以避免 Windows 上的 C++ 编译错误
# 设置一个有效的临时目录作为缓存目录
cache_dir = os.path.join(tempfile.gettempdir(), 'torch_cache')
os.makedirs(cache_dir, exist_ok=True)

# 设置环境变量以禁用编译和设置有效的缓存目录
# 完全禁用 TorchInductor 编译
os.environ['TORCH_COMPILE_DEBUG'] = '1'
os.environ['TORCHINDUCTOR_CACHE_DIR'] = cache_dir
# 禁用 TorchDynamo 编译以避免 C++ 编译问题
os.environ['TORCHDYNAMO_DISABLE'] = '1'
# 设置 Dynamo 缓存目录（即使禁用也需要有效路径）
os.environ['TORCHDYNAMO_CACHE_DIR'] = os.path.join(cache_dir, 'dynamo')
os.makedirs(os.environ['TORCHDYNAMO_CACHE_DIR'], exist_ok=True)
# 禁用 TorchInductor 后端
os.environ['TORCHINDUCTOR_COMPILE_BACKEND'] = 'none'
# 使用 eager 模式
os.environ['TORCH_COMPILE_MODE'] = 'default'

# 在导入 PyTorch 之前设置环境变量
print(f"设置环境变量和缓存目录...")
print(f"缓存目录: {cache_dir}")
print(f"Dynamo 缓存目录: {os.environ['TORCHDYNAMO_CACHE_DIR']}")

# 现在可以安全地导入和执行导出
if __name__ == '__main__':
    # 检查参数
    if len(sys.argv) < 2:
        print("使用方法: python export_model.py [导出参数]")
        print("\n示例 (stories110m 模型):")
        print('python export_model.py base.model_class=stories110m base.checkpoint=stories110M.pt base.params=params.json model.dtype_override="fp16" export.output_name=stories110m_h.pte model.use_kv_cache=True')
        print("\n示例 (Llama 3 模型):")
        print('python export_model.py base.model_class=llama3 base.checkpoint=model.pt base.params=params.json model.dtype_override="fp16" export.output_name=model.pte model.use_kv_cache=True')
        sys.exit(1)
    
    # 检查是否指定了模型类型
    args_str = ' '.join(sys.argv[1:])
    if 'base.model_class' not in args_str and 'base.checkpoint=stories110M.pt' in args_str:
        print("警告: 检测到使用 stories110M.pt，建议添加 base.model_class=stories110m")
        print("如果导出失败，请尝试添加该参数\n")
    
    # 检查是否禁用了动态形状（这可以避免 C++ 编译）
    if 'model.enable_dynamic_shape=False' not in args_str and 'model.enable_dynamic_shape=true' not in args_str.lower():
        print("提示: 如果遇到 C++ 编译错误，尝试添加 model.enable_dynamic_shape=False 来禁用动态形状")
        print("这可以避免需要编译 C++ 代码\n")
    
    # 执行导出命令
    try:
        # 在导入 PyTorch 之前，设置更彻底的环境变量
        import os
        # 完全禁用 Dynamo 和 Inductor
        os.environ['TORCHDYNAMO_DISABLE'] = '1'
        os.environ['TORCH_COMPILE_DEBUG'] = '1'
        
        # 导入 PyTorch
        import torch
        print("PyTorch 版本:", torch.__version__)
        
        # 在导入后立即禁用编译功能
        try:
            # 禁用 Dynamo - 这是关键
            if hasattr(torch, '_dynamo'):
                torch._dynamo.config.disable = True
                torch._dynamo.config.suppress_errors = True
                # 禁用自动动态形状
                if hasattr(torch._dynamo.config, 'automatic_dynamic_shapes'):
                    torch._dynamo.config.automatic_dynamic_shapes = False
                # 禁用形状环境（这是导致 C++ 编译的根源）
                if hasattr(torch._dynamo.config, 'assume_static_by_default'):
                    torch._dynamo.config.assume_static_by_default = True
                print("已禁用 TorchDynamo 和动态形状")
        except Exception as e:
            print(f"警告: 无法完全禁用 TorchDynamo: {e}")
        
        try:
            # 禁用 Inductor 的 C++ 代码生成
            if hasattr(torch, '_inductor'):
                torch._inductor.config.disable_cpp_codegen = True
                # 禁用所有代码生成
                if hasattr(torch._inductor.config, 'cpp'):
                    torch._inductor.config.cpp.enabled = False
                # 禁用形状环境的后端编译
                if hasattr(torch._inductor, 'codecache'):
                    # 尝试禁用 CppCodeCache
                    try:
                        torch._inductor.codecache.CppCodeCache = None
                    except:
                        pass
                print("已禁用 TorchInductor C++ 代码生成")
        except Exception as e:
            print(f"警告: 无法完全禁用 TorchInductor: {e}")
        
        # 尝试禁用形状环境的 C++ 编译（这是问题的根源）
        try:
            if hasattr(torch._dynamo, 'guards'):
                # 尝试禁用 SHAPE_ENV guard 的 C++ 编译
                import torch._dynamo.guards as guards_module
                if hasattr(guards_module, 'SHAPE_ENV'):
                    # 替换 create 方法以避免 C++ 编译
                    original_create = guards_module.SHAPE_ENV.create
                    def dummy_create(builder, self):
                        # 返回一个不编译的 guard
                        return None
                    guards_module.SHAPE_ENV.create = dummy_create
                    print("已禁用形状环境的 C++ 编译")
        except Exception as e:
            print(f"警告: 无法禁用形状环境编译: {e}")
        
        # 尝试禁用 torch.compile 装饰器
        try:
            original_compile = getattr(torch, 'compile', None)
            if original_compile:
                def dummy_compile(*args, **kwargs):
                    # 如果第一个参数是可调用对象，直接返回它
                    if args and callable(args[0]):
                        return args[0]
                    return None
                torch.compile = dummy_compile
                print("已禁用 torch.compile")
        except Exception as e:
            print(f"警告: 无法禁用 torch.compile: {e}")
        
        from executorch.extension.llm.export.export_llm import main
        print("\n开始导出模型...")
        print("注意: 如果仍然遇到 C++ 编译错误，可能需要安装 Visual Studio Build Tools")
        print("或者需要修改 ExecuTorch 导出代码以完全禁用编译\n")
        main()
        print("\n导出完成！")
    except ImportError as e:
        print(f"错误：无法导入 ExecuTorch 模块: {e}")
        print("请确保已正确安装 executorch 包")
        sys.exit(1)
    except Exception as e:
        print(f"导出过程中出现错误: {e}")
        print("\n提示：")
        print("1. 确保已安装所有依赖: pip install executorch")
        print("2. 检查模型文件路径是否正确")
        print("3. 如果仍有问题，请查看 export_model_fix.md 获取更多解决方案")
        sys.exit(1)

