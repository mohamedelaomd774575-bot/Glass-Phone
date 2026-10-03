package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import dev.chrisbanes.haze.HazeState

@Composable
fun PermissionExplanationDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  hazeState: HazeState
) {
  val isDark = isSystemInDarkTheme()
  val titleColor = if (isDark) Color.White else Color(0xFF1C1C1E)

  Dialog(onDismissRequest = onDismiss) {
    GlassSurface(
      hazeState = hazeState,
      shape = RoundedCornerShape(28.dp),
      tintAlpha = if (isDark) 0.35f else 0.50f,
      borderAlpha = 0.60f,
      specularAlpha = 0.65f,
      shadowElevation = 18.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("permission_dialog")
    ) {
      Column(
        modifier = Modifier.padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .background(IosBlue.copy(alpha = 0.20f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Contacts,
            contentDescription = null,
            tint = IosBlue,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Sync Device Contacts",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = titleColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Allow Phone to access your real device contacts and recent call log so you can make calls and search your address book seamlessly. If skipped, demo contacts are used.",
          fontSize = 14.sp,
          color = if (isDark) Color.White.copy(alpha = 0.70f) else Color(0xFF3C3C43).copy(alpha = 0.75f),
          textAlign = TextAlign.Center,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("permission_dismiss_button")
          ) {
            Text(
              text = "Use Demo Data",
              color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF3C3C43)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onConfirm,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = IosBlue,
              contentColor = Color.White
            ),
            modifier = Modifier.testTag("permission_confirm_button")
          ) {
            Text(
              text = "Allow Access",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
