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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.ui.components.ContactAvatar
import com.example.ui.components.LiquidSearchBar
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosYellow
import dev.chrisbanes.haze.HazeState

@Composable
fun ContactsScreen(
  contacts: List<ContactItem>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
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
    // Header: Large 34sp Bold title with tight letter spacing
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Contacts",
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.8).sp,
        color = titleColor,
        modifier = Modifier.testTag("contacts_screen_title")
      )
    }

    // Glass Search Field
    LiquidSearchBar(
      query = searchQuery,
      onQueryChange = onSearchQueryChange,
      hazeState = hazeState,
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    if (contacts.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
      ) {
        GlassSurface(
          hazeState = hazeState,
          shape = RoundedCornerShape(26.dp),
          modifier = Modifier.padding(horizontal = 32.dp)
        ) {
          Text(
            text = if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No Contacts",
            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF3C3C43),
            fontSize = 16.sp,
            modifier = Modifier.padding(24.dp)
          )
        }
      }
    } else {
      // Group contacts by first letter or show in rounded glass card (26dp radius)
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("contacts_list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Group by initial letter
        val grouped = contacts.groupBy {
          it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
        }

        grouped.forEach { (sectionLetter, sectionContacts) ->
          item(key = "header_$sectionLetter") {
            Text(
              text = sectionLetter,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF3C3C43).copy(alpha = 0.65f),
              modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
          }

          item(key = "card_$sectionLetter") {
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
                sectionContacts.forEachIndexed { index, contact ->
                  ContactRowItem(
                    contact = contact,
                    onContactClick = { onContactClick(contact) },
                    onToggleStar = { onToggleStar(contact.id) }
                  )
                  if (index < sectionContacts.lastIndex) {
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
}

@Composable
private fun ContactRowItem(
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
      .testTag("contact_item_${contact.id}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ContactAvatar(
      initials = contact.initials,
      gradientIndex = contact.gradientIndex,
      size = 46.dp,
      showStarBadge = contact.isStarred
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

    // Quick Star toggle
    IconButton(
      onClick = onToggleStar,
      modifier = Modifier.size(36.dp)
    ) {
      Icon(
        imageVector = if (contact.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
        contentDescription = "Favorite",
        tint = if (contact.isStarred) IosYellow else subtitleColor,
        modifier = Modifier.size(20.dp)
      )
    }

    // Call icon button
    IconButton(
      onClick = onContactClick,
      modifier = Modifier.size(36.dp)
    ) {
      Icon(
        imageVector = Icons.Filled.Call,
        contentDescription = "Call",
        tint = IosGreen,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
