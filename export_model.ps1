# ExecuTorch LLM Model Export Script - PowerShell Version
# Fixes C++ compilation errors on Windows

param(
    [Parameter(Mandatory=$false)]
    [string]$ModelClass = "stories110m",
    [Parameter(Mandatory=$false)]
    [string]$Checkpoint = "stories110M.pt",
    [Parameter(Mandatory=$false)]
    [string]$Params = "params.json",
    [Parameter(Mandatory=$false)]
    [string]$OutputName = "stories110m_h.pte",
    [Parameter(Mandatory=$false)]
    [string]$DtypeOverride = "fp16",
    [Parameter(Mandatory=$false)]
    [switch]$UseKVCache = $true,
    [Parameter(Mandatory=$false)]
    [string]$PythonPath = ""
)

# Detect virtual environment
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$venvPath = Join-Path $scriptPath "et_env"
$venvPython = ""

if (Test-Path $venvPath) {
    $venvPythonPath = Join-Path $venvPath "Scripts\python.exe"
    if (Test-Path $venvPythonPath) {
        $venvPython = $venvPythonPath
        Write-Host "Found virtual environment: $venvPath" -ForegroundColor Green
    }
}

# Use specified Python path or virtual environment or default python
if ($PythonPath -ne "") {
    $pythonCmd = $PythonPath
} elseif ($venvPython -ne "") {
    $pythonCmd = $venvPython
} else {
    $pythonCmd = "python"
    Write-Host "Warning: Using system Python. If executorch is not installed, activate virtual environment or specify PythonPath" -ForegroundColor Yellow
}

Write-Host "Using Python: $pythonCmd" -ForegroundColor Cyan

# Set cache directories
$cacheDir = Join-Path $env:TEMP "torch_cache"
$dynamoCacheDir = Join-Path $cacheDir "dynamo"

# Create cache directories
New-Item -ItemType Directory -Force -Path $cacheDir | Out-Null
New-Item -ItemType Directory -Force -Path $dynamoCacheDir | Out-Null

# Set environment variables to disable compilation
Write-Host "Setting environment variables to disable C++ compilation..." -ForegroundColor Green
$env:TORCH_COMPILE_DEBUG = "1"
$env:TORCHINDUCTOR_CACHE_DIR = $cacheDir
$env:TORCHDYNAMO_CACHE_DIR = $dynamoCacheDir
$env:TORCHDYNAMO_DISABLE = "1"
$env:TORCHINDUCTOR_COMPILE_BACKEND = "none"
$env:TORCH_COMPILE_MODE = "default"

Write-Host "Cache directory: $cacheDir" -ForegroundColor Cyan
Write-Host "Dynamo cache directory: $dynamoCacheDir" -ForegroundColor Cyan

# Build command arguments (avoid using $args as it conflicts with PowerShell built-in)
# Add disable_dynamic_shape to avoid C++ compilation issues
$exportArgs = @(
    "base.model_class=$ModelClass",
    "base.checkpoint=$Checkpoint",
    "base.params=$Params",
    "model.dtype_override=$DtypeOverride",
    "model.enable_dynamic_shape=False",
    "export.output_name=$OutputName",
    "model.use_kv_cache=$UseKVCache"
)

Write-Host ""
Write-Host "Starting model export..." -ForegroundColor Green
Write-Host "Command: $pythonCmd -m executorch.extension.llm.export.export_llm $($exportArgs -join ' ')" -ForegroundColor Yellow
Write-Host ""

# Execute export command
try {
    & $pythonCmd -m executorch.extension.llm.export.export_llm $exportArgs
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Export completed successfully!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Export failed with exit code: $LASTEXITCODE" -ForegroundColor Red
        exit $LASTEXITCODE
    }
} catch {
    Write-Host ""
    Write-Host "Error during export: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Tips:" -ForegroundColor Yellow
    Write-Host "1. This script automatically sets model.enable_dynamic_shape=False to avoid C++ compilation"
    Write-Host "2. If you still get C++ errors, install Visual Studio Build Tools:"
    Write-Host "   https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022"
    Write-Host "3. Activate virtual environment: .\et_env\Scripts\Activate.ps1"
    Write-Host "4. Check if model file paths are correct"
    exit 1
}
