package com.molihuan.testmupdf.ui.screen.pdf.operation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.molihuan.testmupdf.utils.PdfUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfOperationScreen() {
    val context = LocalContext.current
    var targetPdfPaths by remember { mutableStateOf<List<String>>(emptyList()) }

    val scope = rememberCoroutineScope()

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) {
            return@rememberLauncherForActivityResult
        }

        try {
            val tempFiles = mutableListOf<String>()
            uris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile =
                    File(context.cacheDir, "temp_${index}_${System.currentTimeMillis()}.pdf")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                tempFiles.add(tempFile.absolutePath)
            }
            targetPdfPaths = tempFiles
            Toast.makeText(context, "已选择 ${tempFiles.size} 个文件", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(onClick = { pickFileLauncher.launch("application/pdf") }) {
                Text(text = "选择PDF文件(可多选)")
            }
            Button(onClick = {
                if (targetPdfPaths.isEmpty()) {
                    Toast.makeText(context, "请选择PDF文件", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                for (pdfPath in targetPdfPaths) {
                    val outputPath = "${pdfPath}.split.pdf"
                    scope.launch(Dispatchers.IO) {
                        val isSuccess = PdfUtil.splitPdfByRange(pdfPath, outputPath, 0, 1)
                        withContext(Dispatchers.Main) {
                            if (isSuccess) {
                                Toast.makeText(context, "拆分成功:$outputPath", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d("PdfOperationScreen", "拆分成功:$outputPath")
                            } else {
                                Toast.makeText(context, "拆分失败:$outputPath", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d("PdfOperationScreen", "拆分失败:$outputPath")
                            }
                        }
                    }
                }
            }) {
                Text(text = "pdf拆分")
            }

            Button(onClick = {
                if (targetPdfPaths.isEmpty()) {
                    Toast.makeText(context, "请选择PDF文件", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val outputPath = "${targetPdfPaths[0]}.merge.pdf"
                scope.launch(Dispatchers.IO) {
                    val isSuccess = PdfUtil.mergePdfFiles(targetPdfPaths, outputPath)
                    withContext(Dispatchers.Main) {
                        if (isSuccess) {
                            Toast.makeText(context, "合并成功:$outputPath", Toast.LENGTH_SHORT)
                                .show()
                            Log.d("PdfOperationScreen", "合并成功:$outputPath")
                        } else {
                            Toast.makeText(context, "合并失败:$outputPath", Toast.LENGTH_SHORT)
                                .show()
                            Log.d("PdfOperationScreen", "合并失败:$outputPath")
                        }
                    }
                }
            }) {
                Text(text = "pdf合并")
            }

            Button(onClick = {
                if (targetPdfPaths.isEmpty()) {
                    Toast.makeText(context, "请选择PDF文件", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                for (pdfPath in targetPdfPaths) {
                    val outputPath = "${pdfPath}.compress.pdf"
                    scope.launch(Dispatchers.IO) {
                        val isSuccess = PdfUtil.compressPdf(pdfPath, outputPath)
                        withContext(Dispatchers.Main) {
                            if (isSuccess) {
                                Toast.makeText(context, "压缩成功:$outputPath", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d("PdfOperationScreen", "压缩成功:$outputPath")
                            } else {
                                Toast.makeText(context, "压缩失败:$outputPath", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d("PdfOperationScreen", "压缩失败:$outputPath")
                            }
                        }
                    }
                }

            }) {
                Text(text = "pdf压缩")
            }
        }
    }
}

@Preview
@Composable
private fun PdfOperationScreenPreview() {
    PdfOperationScreen()
}