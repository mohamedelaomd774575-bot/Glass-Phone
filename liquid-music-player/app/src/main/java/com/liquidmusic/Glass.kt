package com.liquidmusic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Immutable
class Pal(
    val dark: Boolean,
    val bg: Color,
    val fg: Color,
    val muted: Color,
    val glassTop: Color,
    val glassBottom: Color,
    val edgeHi: Color,
    val edgeLo: Color,
)

val DarkPal = Pal(
    dark = true,
    bg = Color(0xFF070A12),
    fg = Color(0xFFF1F4FB),
    muted = Color(0xFF9AA4BC),
    glassTop = Color.White.copy(alpha = 0.16f),
    glassBottom = Color.White.copy(alpha = 0.06f),
    edgeHi = Color.White.copy(alpha = 0.55f),
    edgeLo = Color.White.copy(alpha = 0.08f),
)

val LightPal = Pal(
    dark = false,
    bg = Color(0xFFE9EDF6),
    fg = Color(0xFF0E1320),
    muted = Color(0xFF566078),
    glassTop = Color.White.copy(alpha = 0.70f),
    glassBottom = Color.White.copy(alpha = 0.34f),
    edgeHi = Color.White.copy(alpha = 0.95f),
    edgeLo = Color.White.copy(alpha = 0.30f),
)

val LocalPal = staticCompositionLocalOf { DarkPal }

/** Liquid-glass surface: tinted translucent fill, specular sheen and a bright rim. */
@Composable
fun Modifier.glass(shape: Shape, strong: Boolean = false): Modifier {
    val p = LocalPal.current
    val tint = if (strong) p.bg.copy(alpha = 0.58f) else Color.Transparent
    return this
        .clip(shape)
        .background(tint)
        .background(Brush.verticalGradient(listOf(p.glassTop, p.glassBottom)))
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = if (p.dark) 0.12f else 0.35f),
                    Color.Transparent,
                    Color.Transparent,
                    Color.White.copy(alpha = if (p.dark) 0.05f else 0.15f),
                )
            )
        )
        .border(
            1.dp,
            Brush.linearGradient(listOf(p.edgeHi, p.edgeLo, p.edgeHi.copy(alpha = 0.35f))),
            shape,
        )
}

/** Soft colour blobs that follow the hue of the song that is playing. */
@Composable
fun LiquidBackground(hue: Float) {
    val p = LocalPal.current
    val a = if (p.dark) 0.42f else 0.55f
    val c1 by animateColorAsState(Color.hsv(hue, 0.75f, 1f).copy(alpha = a), tween(900), label = "c1")
    val c2 by animateColorAsState(Color.hsv((hue + 70f) % 360f, 0.7f, 1f).copy(alpha = a * 0.8f), tween(900), label = "c2")
    val c3 by animateColorAsState(Color.hsv((hue + 305f) % 360f, 0.7f, 0.95f).copy(alpha = a * 0.8f), tween(900), label = "c3")
    Box(
        Modifier
            .fillMaxSize()
            .background(p.bg)
            .drawBehind {
                fun blob(c: Color, cx: Float, cy: Float, r: Float) {
                    val center = Offset(cx * size.width, cy * size.height)
                    val radius = r * size.width
                    drawCircle(
                        brush = Brush.radialGradient(listOf(c, Color.Transparent), center = center, radius = radius),
                        radius = radius,
                        center = center,
                    )
                }
                blob(c1, 0.85f, 0.05f, 0.95f)
                blob(c2, 0.05f, 0.40f, 0.80f)
                blob(c3, 0.65f, 1.00f, 0.95f)
            }
    )
}

/** Album artwork from MediaStore, with a generated gradient if the device has no cover. */
@Composable
fun Art(song: Song?, hue: Float, modifier: Modifier = Modifier, corner: Dp = 16.dp) {
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(Color.hsv(hue, 0.55f, 0.95f), Color.hsv((hue + 45f) % 360f, 0.7f, 0.68f))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxSize(0.42f),
        )
        if (song != null) {
            AsyncImage(
                model = song.artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
fun T(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 15.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = LocalPal.current.fg,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
