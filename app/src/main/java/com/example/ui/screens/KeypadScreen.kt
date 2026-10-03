package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactItem
import com.example.data.model.DialerKey
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import dev.chrisbanes.haze.HazeState

private val KeypadRows = listOf(
  listOf(DialerKey("1", ""), DialerKey("2", "A B C"), DialerKey("3", "D E F")),
  listOf(DialerKey("4", "G H I"), DialerKey("5", "J K L"), DialerKey("6", "M N O")),
  listOf(DialerKey("7", "P Q R S"), DialerKey("8", "T U V"), DialerKey("9", "W X Y Z")),
  listOf(DialerKey("*", ""), DialerKey("0", "+"), DialerKey("#", ""))
)

@Composable
fun KeypadScreen(
  keypadNumber: String,
  matchedContact: ContactItem?,
  onKeyPress: (Char) -> Unit,
  onZeroLongPress: () -> Unit,
  onDeletePress: () -> Unit,
  onDeleteLongPress: () -> Unit,
  onCall: () -> Unit,
  hazeState: HazeState,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val numberColor = if (isDark) Color.White else Color(0xFF1C1C1E)

  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top display: typed number and matching contact name
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp, bottom = 12.dp)
        .height(110.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = if (keypadNumber.isEmpty()) "" else keypadNumber,
        fontSize = if (keypadNumber.length > 11) 28.sp else 38.sp,
        fontWeight = FontWeight.Light,
        color = numberColor,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("keypad_display_number")
      )

      Spacer(modifier = Modifier.height(4.dp))

      if (matchedContact != null) {
        Text(
          text = matchedContact.name,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = IosBlue,
          textAlign = TextAlign.Center,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.testTag("matched_contact_name")
        )
      } else if (keypadNumber.isNotEmpty()) {
        Text(
          text = "Add Number",
          fontSize = 15.sp,
          fontWeight = FontWeight.Normal,
          color = IosBlue,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("add_number_action")
        )
      } else {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }

    // 3x4 Glass Keypad Grid
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      KeypadRows.forEach { row ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          row.forEach { keyItem ->
            KeypadButton(
              key = keyItem,
              hazeState = hazeState,
              onPress = { onKeyPress(keyItem.number[0]) },
              onLongPress = {
                if (keyItem.number == "0") {
                  onZeroLongPress()
                } else {
                  onKeyPress(keyItem.number[0])
                }
              }
            )
          }
        }
      }
    }

    // Bottom Action Row: Call Button centered, Delete Button on right
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, bottom = 96.dp)
        .height(80.dp),
      contentAlignment = Alignment.Center
    ) {
      // Call Button (Green #2FD158)
      CallActionButton(
        onClick = onCall,
        modifier = Modifier.align(Alignment.Center)
      )

      // Delete Button (Thin stroke, hidden when number is empty, long press clears)
      androidx.compose.animation.AnimatedVisibility(
        visible = keypadNumber.isNotEmpty(),
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(140)),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 24.dp)
      ) {
        DeleteActionButton(
          onClick = onDeletePress,
          onLongClick = onDeleteLongPress
        )
      }
    }
  }
}

/**
 * Circular glass keypad button:
 * - 78dp circle
 * - Big light-weight number (34sp)
 * - Small letter labels (ABC, DEF...)
 * - Press animation (scale 0.92 + brighter glass)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeypadButton(
  key: DialerKey,
  hazeState: HazeState,
  onPress: () -> Unit,
  onLongPress: () -> Unit
) {
  val isDark = isSystemInDarkTheme()
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1.0f,
    animationSpec = tween(120),
    label = "keypad_button_scale"
  )

  val tintAlpha = if (isPressed) {
    if (isDark) 0.42f else 0.58f
  } else {
    if (isDark) 0.22f else 0.36f
  }

  val specularAlpha = if (isPressed) 0.85f else 0.55f

  val primaryTextColor = if (isDark) Color.White else Color(0xFF1C1C1E)
  val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF3C3C43).copy(alpha = 0.75f)

  Box(
    modifier = Modifier
      .size(78.dp)
      .scale(scale)
  ) {
    GlassSurface(
      hazeState = hazeState,
      shape = CircleShape,
      tintAlpha = tintAlpha,
      borderAlpha = if (isPressed) 0.70f else 0.45f,
      specularAlpha = specularAlpha,
      shadowElevation = if (isPressed) 4.dp else 8.dp,
      modifier = Modifier
        .fillMaxSize()
        .combinedClickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onPress,
          onLongClick = onLongPress
        )
        .testTag("keypad_btn_${key.number}")
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = key.number,
          fontSize = if (key.number == "*" || key.number == "#") 38.sp else 34.sp,
          fontWeight = FontWeight.Light,
          color = primaryTextColor,
          lineHeight = 36.sp
        )
        if (key.letters.isNotEmpty()) {
          Text(
            text = key.letters,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = secondaryTextColor,
            letterSpacing = 1.6.sp
          )
        }
      }
    }
  }
}

/**
 * Green (#2FD158) circular call button with a white phone icon.
 */
@Composable
private fun CallActionButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.90f else 1.0f,
    animationSpec = tween(120),
    label = "call_button_scale"
  )

  Box(
    modifier = modifier
      .size(76.dp)
      .scale(scale)
      .shadow(12.dp, CircleShape, ambientColor = IosGreen.copy(alpha = 0.4f), spotColor = IosGreen.copy(alpha = 0.6f))
      .clip(CircleShape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF34E865),
            IosGreen,
            Color(0xFF24A844)
          )
        )
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .testTag("call_action_button"),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.Filled.Call,
      contentDescription = "Call",
      tint = Color.White,
      modifier = Modifier.size(34.dp)
    )
  }
}

/**
 * Delete button: No background, thin-stroke backspace icon,
 * click to delete last character, long-press to clear all.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeleteActionButton(
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val iconColor = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF3C3C43).copy(alpha = 0.85f)
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.85f else 1.0f,
    animationSpec = tween(120),
    label = "delete_button_scale"
  )

  Box(
    modifier = modifier
      .size(54.dp)
      .scale(scale)
      .combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("delete_action_button"),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.Backspace,
      contentDescription = "Delete",
      tint = iconColor,
      modifier = Modifier.size(28.dp)
    )
  }
}
