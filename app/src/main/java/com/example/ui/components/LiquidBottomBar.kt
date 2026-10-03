package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TabDestination
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import dev.chrisbanes.haze.HazeState

private data class TabItemData(
  val tab: TabDestination,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

private val tabs = listOf(
  TabItemData(TabDestination.FAVORITES, "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder),
  TabItemData(TabDestination.RECENTS, "Recents", Icons.Filled.History, Icons.Outlined.History),
  TabItemData(TabDestination.CONTACTS, "Contacts", Icons.Filled.Person, Icons.Outlined.Person),
  TabItemData(TabDestination.KEYPAD, "Keypad", Icons.Filled.Dialpad, Icons.Outlined.Dialpad),
  TabItemData(TabDestination.VOICEMAIL, "Voicemail", Icons.Filled.Voicemail, Icons.Outlined.Voicemail)
)

/**
 * Floating pill-shaped bottom tab bar, detached from screen edges (16dp margin),
 * fully rounded (32dp), translucent liquid glass with Haze blur, specular highlight,
 * and lighter capsule behind the active tab.
 */
@Composable
fun LiquidBottomBar(
  currentTab: TabDestination,
  onTabSelected: (TabDestination) -> Unit,
  hazeState: HazeState,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    GlassSurface(
      hazeState = hazeState,
      shape = RoundedCornerShape(36.dp),
      tintAlpha = if (isDark) 0.24f else 0.38f,
      borderAlpha = 0.55f,
      specularAlpha = 0.60f,
      shadowElevation = 14.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(68.dp)
        .testTag("liquid_bottom_bar")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        tabs.forEach { item ->
          val isSelected = item.tab == currentTab
          val tintColor by animateColorAsState(
            targetValue = if (isSelected) IosBlue else if (isDark) Color.White.copy(alpha = 0.62f) else Color(0xFF3C3C43).copy(alpha = 0.70f),
            animationSpec = tween(180),
            label = "tab_tint"
          )

          val capsuleBackground = if (isSelected) {
            if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.45f)
          } else {
            Color.Transparent
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .clip(CircleShape)
              .background(capsuleBackground)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                onTabSelected(item.tab)
              }
              .testTag("tab_${item.label.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
              )
              Text(
                text = item.label,
                color = tintColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}
