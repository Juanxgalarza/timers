package com.pixeltimers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sweetie 16 palette
object PixelColors {
    val Background = Color(0xFF1A1C2C)
    val Panel = Color(0xFF29366F)
    val PanelDark = Color(0xFF333C57)
    val Accent = Color(0xFFFFCD75)
    val Green = Color(0xFFA7F070)
    val Red = Color(0xFFB13E53)
    val Orange = Color(0xFFEF7D57)
    val Text = Color(0xFFF4F4F4)
    val Muted = Color(0xFF94B0C2)
}

/** Rectangle with stepped (pixelated) corners. */
private class PixelShape(private val step: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { step.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(s, 0f); lineTo(w - s, 0f); lineTo(w - s, s); lineTo(w, s)
            lineTo(w, h - s); lineTo(w - s, h - s); lineTo(w - s, h); lineTo(s, h)
            lineTo(s, h - s); lineTo(0f, h - s); lineTo(0f, s); lineTo(s, s)
            close()
        }
        return Outline.Generic(path)
    }
}

private val PixelCorner = PixelShape(4.dp)

fun Modifier.pixelBox(fill: Color, border: Color): Modifier =
    this.background(fill, PixelCorner).border(3.dp, border, PixelCorner)

@Composable
fun PixelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PixelColors.Text,
    size: TextUnit = 14.sp,
    bold: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            // Swap for a bundled pixel font (e.g. Silkscreen, Press Start 2P) in res/font if desired
            fontFamily = FontFamily.Monospace,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        ),
    )
}

@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    fill: Color = PixelColors.Panel,
    border: Color = PixelColors.Accent,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.pixelBox(fill, border).padding(14.dp), content = content)
}

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color = PixelColors.PanelDark,
    content: Color = PixelColors.Text,
    description: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Pressed state inverts the colors, like an old handheld button
    val bg = if (pressed) PixelColors.Accent else fill
    val fg = if (pressed) PixelColors.Background else content

    Box(
        modifier = modifier
            .pixelBox(bg, PixelColors.Accent)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { if (description != null) contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        PixelText(text, color = fg, bold = true)
    }
}

@Composable
fun PixelField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = PixelColors.Text, fontSize = 16.sp, fontFamily = FontFamily.Monospace),
        cursorBrush = SolidColor(PixelColors.Accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth().pixelBox(PixelColors.Background, PixelColors.Muted).padding(12.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) PixelText(placeholder, color = PixelColors.Muted, size = 16.sp)
                inner()
            }
        },
    )
}
