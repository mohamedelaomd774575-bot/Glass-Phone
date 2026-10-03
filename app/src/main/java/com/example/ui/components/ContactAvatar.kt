package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvatarGradients
import com.example.ui.theme.IosYellow

@Composable
fun ContactAvatar(
  initials: String,
  gradientIndex: Int,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  showStarBadge: Boolean = false
) {
  val colors = AvatarGradients[gradientIndex.coerceIn(0, AvatarGradients.lastIndex)]

  Box(modifier = modifier.size(size)) {
    Box(
      modifier = Modifier
        .size(size)
        .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.2f))
        .clip(CircleShape)
        .background(
          brush = Brush.linearGradient(
            colors = colors
          )
        )
        .border(
          width = 1.dp,
          brush = Brush.verticalGradient(
            listOf(
              Color.White.copy(alpha = 0.6f),
              Color.White.copy(alpha = 0.15f)
            )
          ),
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = initials,
        color = Color.White,
        fontSize = (size.value * 0.40f).sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
      )
    }

    if (showStarBadge) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .offset(x = 2.dp, y = 2.dp)
          .size((size.value * 0.40f).dp)
          .clip(CircleShape)
          .background(Color(0xFF1C1C1E))
          .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Star,
          contentDescription = "Starred",
          tint = IosYellow,
          modifier = Modifier.size((size.value * 0.26f).dp)
        )
      }
    }
  }
}
