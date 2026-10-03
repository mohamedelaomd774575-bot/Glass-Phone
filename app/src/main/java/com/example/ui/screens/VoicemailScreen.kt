package com.example.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.GlassSurface
import com.example.ui.theme.IosBlue
import dev.chrisbanes.haze.HazeState

@Composable
fun VoicemailScreen(
  onCallVoicemail: () -> Unit,
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
      text = "Voicemail",
      fontSize = 34.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = (-0.8).sp,
      color = titleColor,
      modifier = Modifier
        .padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
        .testTag("voicemail_screen_title")
    )

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
        borderAlpha = 0.50f,
        specularAlpha = 0.55f,
        shadowElevation = 10.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voicemail_glass_card")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 28.dp, vertical = 38.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Voicemail,
            contentDescription = "Voicemail",
            tint = IosBlue,
            modifier = Modifier.size(64.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "No Voicemail",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("no_voicemail_label")
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Your carrier voicemail inbox has no pending voice messages.",
            fontSize = 15.sp,
            color = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF3C3C43).copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
          )

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = onCallVoicemail,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = IosBlue,
              contentColor = Color.White
            ),
            modifier = Modifier
              .height(46.dp)
              .testTag("call_voicemail_button")
          ) {
            Text(
              text = "Call Voicemail",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
