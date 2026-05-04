package com.molihuan.testmupdf.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.molihuan.testmupdf.ui.screen.app.LocalNavBackStack
import com.molihuan.testmupdf.ui.screen.app.NavRoute

@Composable
fun MainScreen() {
    val navBackStack = LocalNavBackStack.current

    var mupdfVersion by remember { mutableStateOf("未知") }

    LaunchedEffect(Unit) {
        runCatching {
            mupdfVersion = com.artifex.mupdf.fitz.Context.getVersion().version
        }.onFailure {
            mupdfVersion = "错误"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "MuPdf版本:$mupdfVersion")
            Button(onClick = {
                navBackStack?.add(NavRoute.PdfRender)
            }) {
                Text(text = "pdf渲染")
            }
            Button(onClick = {
                navBackStack?.add(NavRoute.PdfOperation)
            }) {
                Text(text = "pdf操作")
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
}