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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.ContactAvatar
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosYellow
import dev.chrisbanes.haze.HazeState

@Composable
fun FavoritesScreen(
  favorites: List<ContactItem>,
  onContactClick: (ContactItem) -> Unit,
  onToggleStar: (String) -> Unit,
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
    Text(
      text = "Favorites",
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.8).sp,
      color = titleColor,
      modifier = Modifier
        .padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
        .testTag("favorites_screen_title")
    )

    if (favorites.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp)
          .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
      ) {
        GlassSurface(
          hazeState = hazeState,
          shape = RoundedCornerShape(26.dp),
          tintAlpha = if (isDark) 0.20f else 0.35f,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Outlined.StarBorder,
              contentDescription = null,
              tint = IosYellow,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "No Favorites Yet",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = titleColor,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Add frequently called contacts to your favorites for instant one-tap dialing.",
              fontSize = 14.sp,
              color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF3C3C43).copy(alpha = 0.70f),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("favorites_list"),
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
              favorites.forEachIndexed { index, contact ->
                FavoriteRowItem(
                  contact = contact,
                  onContactClick = { onContactClick(contact) },
                  onToggleStar = { onToggleStar(contact.id) }
                )
                if (index < favorites.lastIndex) {
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
private fun FavoriteRowItem(
  contact: ContactItem,
  onContactClick: () -> Unit,
  onToggleStar: () -> Unit
) {
  val isDark = isSystemInDarkTheme()
  val nameColor = if (isDark) Color.White else Color(0xFF1C1C1E)
  val subtitleColor = if (isDark) Color.White.copy(alpha = 0.60f) else Color(0xFF3C3C43).copy(alpha = 0.65f)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onContactClick)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("favorite_item_${contact.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ContactAvatar(
      initials = contact.initials,
      gradientIndex = contact.gradientIndex,
      size = 48.dp,
      showStarBadge = true
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 14.dp)
    ) {
      Text(
        text = contact.name,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = nameColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = "${contact.label} • ${contact.phoneNumber}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = subtitleColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Call Button
    IconButton(
      onClick = onContactClick,
      modifier = Modifier.size(40.dp)
    ) {
      Icon(
        imageVector = Icons.Filled.Call,
        contentDescription = "Call",
        tint = IosGreen,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
