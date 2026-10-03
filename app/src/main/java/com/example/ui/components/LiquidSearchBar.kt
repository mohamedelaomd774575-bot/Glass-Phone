package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.GlassSurface
import dev.chrisbanes.haze.HazeState

@Composable
fun LiquidSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  hazeState: HazeState,
  modifier: Modifier = Modifier,
  placeholder: String = "Search contacts"
) {
  val isDark = isSystemInDarkTheme()
  val textColor = if (isDark) Color.White else Color(0xFF1C1C1E)
  val placeholderColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF3C3C43).copy(alpha = 0.50f)

  GlassSurface(
    hazeState = hazeState,
    shape = RoundedCornerShape(18.dp),
    tintAlpha = if (isDark) 0.18f else 0.32f,
    borderAlpha = 0.40f,
    specularAlpha = 0.50f,
    shadowElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .testTag("liquid_search_bar")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = "Search",
        tint = placeholderColor,
        modifier = Modifier.size(20.dp)
      )

      Box(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        if (query.isEmpty()) {
          Text(
            text = placeholder,
            color = placeholderColor,
            fontSize = 16.sp
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          textStyle = TextStyle(
            color = textColor,
            fontSize = 16.sp
          ),
          cursorBrush = SolidColor(if (isDark) Color.White else Color(0xFF0A84FF)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_text_input")
        )
      }

      if (query.isNotEmpty()) {
        Box(
          modifier = Modifier
            .size(22.dp)
            .clickable { onQueryChange("") }
            .testTag("search_clear_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Clear search",
            tint = placeholderColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
