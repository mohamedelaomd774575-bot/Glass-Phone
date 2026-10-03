package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecentsFilter
import com.example.ui.glass.GlassSurface
import dev.chrisbanes.haze.HazeState

@Composable
fun LiquidSegmentedControl(
  selectedFilter: RecentsFilter,
  onFilterSelected: (RecentsFilter) -> Unit,
  hazeState: HazeState,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()

  GlassSurface(
    hazeState = hazeState,
    shape = CircleShape,
    tintAlpha = if (isDark) 0.18f else 0.32f,
    borderAlpha = 0.40f,
    specularAlpha = 0.45f,
    shadowElevation = 6.dp,
    modifier = modifier
      .width(180.dp)
      .height(38.dp)
      .testTag("recents_segmented_control")
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp)
    ) {
      val segmentWidth = maxWidth / 2

      val offsetFraction by animateFloatAsState(
        targetValue = if (selectedFilter == RecentsFilter.ALL) 0f else 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioLowBouncy,
          stiffness = Spring.StiffnessMediumLow
        ),
        label = "segment_indicator"
      )

      // Active pill indicator
      Box(
        modifier = Modifier
          .offset(x = segmentWidth * offsetFraction)
          .width(segmentWidth)
          .fillMaxHeight()
          .clip(CircleShape)
          .background(
            if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.70f)
          )
      )

      // Labels row
      Row(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              onFilterSelected(RecentsFilter.ALL)
            }
            .testTag("filter_all"),
          contentAlignment = Alignment.Center
        ) {
          val textColor by animateColorAsState(
            targetValue = if (selectedFilter == RecentsFilter.ALL) {
              if (isDark) Color.White else Color(0xFF1C1C1E)
            } else {
              if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF3C3C43).copy(alpha = 0.65f)
            },
            animationSpec = tween(150),
            label = "text_all"
          )
          Text(
            text = "All",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selectedFilter == RecentsFilter.ALL) FontWeight.SemiBold else FontWeight.Normal
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {
              onFilterSelected(RecentsFilter.MISSED)
            }
            .testTag("filter_missed"),
          contentAlignment = Alignment.Center
        ) {
          val textColor by animateColorAsState(
            targetValue = if (selectedFilter == RecentsFilter.MISSED) {
              if (isDark) Color.White else Color(0xFF1C1C1E)
            } else {
              if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF3C3C43).copy(alpha = 0.65f)
            },
            animationSpec = tween(150),
            label = "text_missed"
          )
          Text(
            text = "Missed",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selectedFilter == RecentsFilter.MISSED) FontWeight.SemiBold else FontWeight.Normal
          )
        }
      }
    }
  }
}
