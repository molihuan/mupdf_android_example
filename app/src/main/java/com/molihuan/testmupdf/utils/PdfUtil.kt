package com.molihuan.testmupdf.utils

import android.util.Log
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.PDFDocument

/**
 * PDF 合并与拆分工具
 */
object PdfUtil {

    private const val TAG = "PdfUtil"

    private const val SAVE_OPTIONS = "garbage=4,compress,compress-fonts,compress-images,clean"

    /**
     * 合并多个 PDF 文件（按顺序）
     *
     * @param inputPaths 输入文件路径列表（按合并顺序）
     * @param outputPath 输出文件路径
     * @return 是否成功
     */
    fun mergePdfFiles(inputPaths: List<String>, outputPath: String): Boolean {
        if (inputPaths.isEmpty()) {
            Log.e(TAG, "输入文件列表为空")
            return false
        }

        var outputDoc: PDFDocument? = null

        try {
            outputDoc = PDFDocument()
            var currentPage = 0

            inputPaths.forEach { inputPath ->
                var inputDoc: Document? = null
                try {
                    inputDoc = Document.openDocument(inputPath)

                    if (!inputDoc.isPDF) {
                        Log.w(TAG, "跳过非 PDF 文件: $inputPath")
                        return@forEach
                    }

                    val pdfDoc = inputDoc as PDFDocument
                    val pageCount = pdfDoc.countPages()

                    // 按顺序嫁接每个页面
                    for (i in 0 until pageCount) {
                        outputDoc.graftPage(currentPage, pdfDoc, i)
                        currentPage++
                    }

                    Log.d(TAG, "已合并: $inputPath ($pageCount 页)")

                } catch (e: Exception) {
                    Log.e(TAG, "处理文件失败: $inputPath", e)
                } finally {
                    inputDoc?.destroy()
                }
            }
            // 子集化字体
            outputDoc.subsetFonts()
            // 保存
            outputDoc.save(outputPath, SAVE_OPTIONS)
            Log.d(TAG, "合并完成，共 $currentPage 页")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "合并失败", e)
            return false
        } finally {
            outputDoc?.destroy()
        }
    }

    /**
     * 按页面范围拆分 PDF
     *
     * @param inputPath 输入文件路径
     * @param outputPath 输出文件路径
     * @param startPage 起始页码（从 0 开始）
     * @param endPage 结束页码（包含，从 0 开始）
     * @return 是否成功
     */
    fun splitPdfByRange(
        inputPath: String,
        outputPath: String,
        startPage: Int,
        endPage: Int
    ): Boolean {
        if (startPage !in 0..endPage) {
            Log.e(TAG, "页码范围无效: $startPage - $endPage")
            return false
        }

        var inputDoc: Document? = null
        var outputDoc: PDFDocument? = null

        try {
            inputDoc = Document.openDocument(inputPath)

            if (!inputDoc.isPDF) {
                Log.e(TAG, "不是 PDF 文件")
                return false
            }

            val pdfDoc = inputDoc as PDFDocument
            val totalPages = pdfDoc.countPages()

            if (startPage >= totalPages || endPage >= totalPages) {
                Log.e(TAG, "页码超出范围 (总页数: $totalPages)")
                return false
            }

            outputDoc = PDFDocument()
            var targetPage = 0

            // 提取指定范围的页面
            for (i in startPage..endPage) {
                outputDoc.graftPage(targetPage, pdfDoc, i)
                targetPage++
            }

            // 子集化字体
            outputDoc.subsetFonts()

            outputDoc.save(outputPath, SAVE_OPTIONS)
            Log.d(TAG, "拆分完成，提取了 ${targetPage} 页")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "拆分失败", e)
            return false
        } finally {
            inputDoc?.destroy()
            outputDoc?.destroy()
        }
    }

    //pdf压缩
    fun compressPdf(inputPath: String, outputPath: String): Boolean {
        var inputDoc: Document? = null
        try {
            inputDoc = Document.openDocument(inputPath)
            if (!inputDoc.isPDF) {
                Log.e(TAG, "不是 PDF 文件")
                return false
            }
            val pdfDoc = inputDoc as PDFDocument
            // 子集化字体
            pdfDoc.subsetFonts()

            pdfDoc.save(outputPath, SAVE_OPTIONS)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            inputDoc?.destroy()
        }
    }

}