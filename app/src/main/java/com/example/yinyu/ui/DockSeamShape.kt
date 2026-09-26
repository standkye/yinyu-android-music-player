package com.example.yinyu.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** 常态为圆角胶囊；仅在上滑时使封面上方的边缘短暂鼓起。 */
internal class DockSeamShape(private val lift: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val l = lift.coerceIn(0f, 1f)
        val top = with(density) { 6.dp.toPx() }
        val r = min((h - top) * .49f, w * .12f)
        val crest = top * (1f - l)
        val path = Path().apply {
            moveTo(0f, top + r)
            cubicTo(0f, top + r * .42f, r * .42f, top, r, top)
            lineTo(w * .40f, top)
            // The seam bends only while the dock is being pulled upward.
            cubicTo(w * .45f, top, w * .455f, crest, w * .50f, crest)
            cubicTo(w * .545f, crest, w * .55f, top, w * .60f, top)
            lineTo(w - r, top)
            cubicTo(w - r * .42f, top, w, top + r * .42f, w, top + r)
            lineTo(w, h - r)
            cubicTo(w, h - r * .42f, w - r * .42f, h, w - r, h)
            lineTo(r, h)
            cubicTo(r * .42f, h, 0f, h - r * .42f, 0f, h - r)
            close()
        }
        return Outline.Generic(path)
    }
}
