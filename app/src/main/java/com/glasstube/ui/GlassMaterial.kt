package com.glasstube.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GlassTeal = Color(0xFF63E2D1)
private val Ink = Color(0xFF102528)
private val MutedInk = Color(0xFF6C8586)

@Composable
fun AmbientBackdrop(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    Canvas(modifier.fillMaxSize()) {
        drawRect(if (dark) Color(0xFF071518) else Color(0xFFEAF3F1))
        val teal = if (dark) Color(0xFF0F827C) else Color(0xFF7CD8CB)
        val blue = if (dark) Color(0xFF244C69) else Color(0xFFB5D5E6)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(teal.copy(alpha = if (dark) 0.34f else 0.32f), Color.Transparent),
                center = Offset(size.width * 0.08f, size.height * 0.08f),
                radius = size.minDimension * 0.88f
            ),
            radius = size.maxDimension * 0.82f,
            center = Offset(size.width * 0.08f, size.height * 0.08f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blue.copy(alpha = if (dark) 0.24f else 0.42f), Color.Transparent),
                center = Offset(size.width * 0.96f, size.height * 0.46f),
                radius = size.minDimension * 0.70f
            ),
            radius = size.maxDimension * 0.68f,
            center = Offset(size.width * 0.96f, size.height * 0.46f)
        )
    }
}

/**
 * A layered translucent material with a specular edge and top reflection.
 * Apple’s private renderer is not available to Android apps; this recreates the
 * visual language with native Compose drawing and remains compatible with Android 8+.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val dark = isSystemInDarkTheme()
    val fill = if (dark) {
        listOf(
            Color(0xFF274148).copy(alpha = 0.72f),
            Color(0xFF10262C).copy(alpha = 0.78f),
            Color(0xFF183239).copy(alpha = 0.68f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.82f),
            Color(0xFFE6F4F1).copy(alpha = 0.74f),
            Color.White.copy(alpha = 0.66f)
        )
    }
    val edge = if (dark) {
        listOf(Color.White.copy(alpha = 0.56f), GlassTeal.copy(alpha = 0.20f), Color.White.copy(alpha = 0.08f))
    } else {
        listOf(Color.White.copy(alpha = 0.98f), Color(0xFF8ABBB5).copy(alpha = 0.34f), Color.White.copy(alpha = 0.52f))
    }

    Box(
        modifier = modifier
            .shadow(18.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(fill))
            .border(BorderStroke(1.dp, Brush.linearGradient(edge)), shape)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (dark) 0.12f else 0.48f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (dark) 0.10f else 0.025f)
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val interaction = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = CircleShape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSystemInDarkTheme()) Color.White else Ink,
            modifier = Modifier.align(Alignment.Center).size(20.dp)
        )
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (selected) GlassTeal.copy(alpha = if (dark) 0.22f else 0.30f)
                    else Color.Transparent
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (dark) Color.White else Ink,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search videos"
) {
    val dark = isSystemInDarkTheme()
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = GlassTeal, modifier = Modifier.size(21.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = if (dark) Color.White else Ink,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(GlassTeal),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                color = if (dark) Color.White.copy(alpha = 0.48f) else MutedInk,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
fun GlassNotice(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    GlassSurface(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                title,
                color = if (dark) Color.White else Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                body,
                color = if (dark) Color.White.copy(alpha = 0.68f) else MutedInk,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}
