package com.example.tfdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.tfdemo.ui.theme.TfDemoTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {
    
    private lateinit var imageClassifier: ImageClassifier
    private var imageFile: File? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要存储权限来访问图片", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                
                // 保存图片到内部存储
                imageFile = File(filesDir, "temp_image.jpg")
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                
                Toast.makeText(this, "图片已加载", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading image: ${e.message}")
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化图像分类器
        imageClassifier = ImageClassifier(this)
        
        // 复制内置图片到内部存储
        copyBuiltInImage()
        
        setContent {
            TfDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageClassificationScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPickImage = { pickImage() },
                        onClassifyImage = { classifyImage() }
                    )
                }
            }
        }
    }
    
    private fun copyBuiltInImage() {
        try {
            val inputStream: InputStream = assets.open("img.png")
            imageFile = File(filesDir, "img.png")
            val outputStream = FileOutputStream(imageFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Log.d("MainActivity", "Built-in image copied successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error copying built-in image: ${e.message}")
        }
    }
    
    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            pickImageLauncher.launch("image/*")
        }
    }
    
    private fun classifyImage() {
        imageFile?.let { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                val result = imageClassifier.classifyBitmap(bitmap)
                if (result != null) {
                    val message = "分类结果: ${result.className}\n置信度: ${String.format("%.2f", result.confidence * 100)}%"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Classification result: $result")
                } else {
                    Toast.makeText(this, "分类失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        imageClassifier.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageClassificationScreen(
    modifier: Modifier = Modifier,
    onPickImage: () -> Unit,
    onClassifyImage: () -> Unit
) {
    val context = LocalContext.current
    var classificationResult by remember { mutableStateOf<ClassificationResult?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EfficientNet Lite0 图像分类",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "使用TensorFlow Lite进行图像分类",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // 显示内置图片
        val builtInImageFile = File(context.filesDir, "img.png")
        if (builtInImageFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(builtInImageFile.absolutePath)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "示例图片",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPickImage,
                modifier = Modifier.weight(1f)
            ) {
                Text("选择图片")
            }
            
            Button(
                onClick = {
                    onClassifyImage()
                    // 这里可以更新UI显示结果
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("开始分类")
            }
        }
        
        // 显示分类结果
        classificationResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "分类结果",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "类别: ${result.className}",
                        fontSize = 16.sp
                    )
                    
                    Text(
                        text = "置信度: ${String.format("%.2f", result.confidence * 100)}%",
                        fontSize = 16.sp
                    )
                    
                    Text(
                        text = "类别索引: ${result.classIndex}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 使用说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "使用说明",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "1. 点击'选择图片'按钮选择要分类的图片\n" +
                            "2. 点击'开始分类'按钮进行图像分类\n" +
                            "3. 应用将使用EfficientNet Lite0模型对图片进行分类\n" +
                            "4. 分类结果将显示图片的类别和置信度",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}