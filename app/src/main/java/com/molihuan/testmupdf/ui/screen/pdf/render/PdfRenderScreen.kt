package com.molihuan.testmupdf.ui.screen.pdf.render

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.artifex.mupdf.fitz.ColorSpace
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer

@Composable
fun PdfRenderScreen() {
    val context = LocalContext.current
    var pdfPath by remember { mutableStateOf<String?>(null) }

    var document by remember { mutableStateOf<Document?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }


    DisposableEffect(pdfPath) {
        // 加载 PDF文档
        try {
            pdfPath?.let {
                val doc = Document.openDocument(it)
                document = doc
                pageCount = doc.countPages()
                currentPage = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            document?.destroy()
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                pdfPath = tempFile.absolutePath
            } catch (e: Exception) {
                Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            Button(onClick = { pickFileLauncher.launch("application/pdf") }) {
                Text("选择 PDF 文件")
            }
            Spacer(modifier = Modifier.height(16.dp))
            document?.let {
                PdfView(document = it, currentPage = currentPage, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        currentPage = (currentPage - 1).coerceAtLeast(0)
                    },
                    enabled = currentPage > 0
                ) {
                    Text("上一页")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        currentPage = (currentPage + 1).coerceAtMost(pageCount - 1)
                    },
                    enabled = currentPage < pageCount - 1
                ) {
                    Text("下一页")
                }
            }
        }
    }

}

@Composable
private fun PdfView(
    document: Document,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val bitmapState = produceState<Bitmap?>(null, document, currentPage) {
        value = withContext(Dispatchers.Default) {
            try {
                val page = document.loadPage(currentPage)

                val zoom = density.density * 2

                val pixmap = page.toPixmap(
                    Matrix().scale(zoom, zoom),
                    ColorSpace.DeviceRGB,
                    true
                )

                val bitmap = createBitmap(pixmap.width, pixmap.height)


                bitmap.copyPixelsFromBuffer(
                    IntBuffer.wrap(pixmap.pixels)
                )

                pixmap.destroy()
                page.destroy()

                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    val bitmap = bitmapState.value


    Box(
        modifier = modifier
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(5.dp)
                    .zoomable(rememberZoomState())
            )
        } ?: run {
            CircularProgressIndicator()
        }
    }

}

@Preview
@Composable
private fun PdfRenderScreenPreview() {
    PdfRenderScreen()
}