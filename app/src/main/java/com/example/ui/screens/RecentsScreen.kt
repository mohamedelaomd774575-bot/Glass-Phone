package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.data.model.RecentCallItem
import com.example.data.model.RecentsFilter
import com.example.ui.components.ContactAvatar
import com.example.ui.components.LiquidSegmentedControl
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosRed
import dev.chrisbanes.haze.HazeState

@Composable
fun RecentsScreen(
  recents: List<RecentCallItem>,
  selectedFilter: RecentsFilter,
  onFilterSelected: (RecentsFilter) -> Unit,
  onCallClick: (RecentCallItem) -> Unit,
  hazeState: HazeState,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val titleColor = if (isDark) Color.White else Color(0xFF1C1C1E)

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
  ) {
    // Top Bar with Segmented Control centered
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      LiquidSegmentedControl(
        selectedFilter = selectedFilter,
        onFilterSelected = onFilterSelected,
        hazeState = hazeState
      )
    }

    // Screen Title (34sp Bold, tight letter spacing)
    Text(
      text = "Recents",
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.8).sp,
      color = titleColor,
      modifier = Modifier
        .padding(start = 20.dp, top = 8.dp, bottom = 12.dp)
        .testTag("recents_screen_title")
    )

    if (recents.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
      ) {
        GlassSurface(
          hazeState = hazeState,
          shape = RoundedCornerShape(26.dp),
          modifier = Modifier.padding(horizontal = 32.dp)
        ) {
          Text(
            text = if (selectedFilter == RecentsFilter.MISSED) "No Missed Calls" else "No Recent Calls",
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF3C3C43),
            fontSize = 16.sp,
            modifier = Modifier.padding(24.dp)
          )
        }
      }
    } else {
      // Rounded glass cards (26dp radius) containing recents
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("recents_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp)
      ) {
        item {
          GlassSurface(
            hazeState = hazeState,
            shape = RoundedCornerShape(26.dp),
            tintAlpha = if (isDark) 0.18f else 0.32f,
            borderAlpha = 0.45f,
            specularAlpha = 0.50f,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              recents.forEachIndexed { index, recent ->
                RecentCallRow(
                  recent = recent,
                  onCallClick = { onCallClick(recent) }
                )
                if (index < recents.lastIndex) {
                  HorizontalDivider(
                    thickness = 0.6.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                    modifier = Modifier.padding(start = 68.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecentCallRow(
  recent: RecentCallItem,
  onCallClick: () -> Unit
) {
  val isDark = isSystemInDarkTheme()
  val isMissed = recent.callType == CallType.MISSED
  val nameColor = if (isMissed) IosRed else if (isDark) Color.White else Color(0xFF1C1C1E)
  val subtitleColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF3C3C43).copy(alpha = 0.65f)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onCallClick)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("recent_item_${recent.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ContactAvatar(
      initials = recent.initials,
      gradientIndex = recent.gradientIndex,
      size = 46.dp
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = recent.contactName,
          fontSize = 17.sp,
          fontWeight = FontWeight.SemiBold,
          color = nameColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (recent.count > 1) {
          Text(
            text = " (${recent.count})",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = nameColor
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
      ) {
        val (icon, tint) = when (recent.callType) {
          CallType.MISSED -> Icons.Filled.CallMissed to IosRed
          CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to subtitleColor
          CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to subtitleColor
        }
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
          text = if (recent.contactName != recent.phoneNumber) recent.phoneNumber else "Mobile",
          fontSize = 13.sp,
          color = subtitleColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Call date/time & Info button
    Text(
      text = recent.formattedTime,
      fontSize = 14.sp,
      color = subtitleColor,
      modifier = Modifier.padding(end = 4.dp)
    )

    IconButton(
      onClick = onCallClick,
      modifier = Modifier.size(36.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = "Call details",
        tint = IosBlue,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
