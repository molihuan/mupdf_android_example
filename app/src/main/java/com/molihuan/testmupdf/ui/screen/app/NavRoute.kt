package com.molihuan.testmupdf.ui.screen.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute(
    val routeName: String? = null
) : NavKey {
    @Serializable
    object Main : NavRoute(routeName = "Main")
    @Serializable
    object PdfRender : NavRoute(routeName = "PdfRender")
    @Serializable
    object PdfOperation : NavRoute(routeName = "PdfOperation")
}