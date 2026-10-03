package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TabDestination
import com.example.ui.components.LiquidBottomBar
import com.example.ui.components.PermissionExplanationDialog
import com.example.ui.glass.LiquidBackground
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.KeypadScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.VoicemailScreen
import com.example.ui.theme.PhoneTheme
import com.example.ui.viewmodel.PhoneViewModel
import dev.chrisbanes.haze.HazeState

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PhoneTheme {
        PhoneAppRoot()
      }
    }
  }
}

@Composable
fun PhoneAppRoot(
  viewModel: PhoneViewModel = viewModel()
) {
  val context = LocalContext.current
  val hazeState = remember { HazeState() }

  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val keypadNumber by viewModel.keypadNumber.collectAsStateWithLifecycle()
  val matchedContact by viewModel.matchedContact.collectAsStateWithLifecycle()
  val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val favorites by viewModel.favorites.collectAsStateWithLifecycle()
  val filteredRecents by viewModel.filteredRecents.collectAsStateWithLifecycle()
  val recentsFilter by viewModel.recentsFilter.collectAsStateWithLifecycle()

  var showPermissionPrompt by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    viewModel.loadData()
  }

  // Check if permissions need a friendly prompt on first start
  LaunchedEffect(Unit) {
    val hasContacts = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasContacts) {
      showPermissionPrompt = true
    }
  }

  // Back handling: if on a sub-tab, return to Keypad
  BackHandler(enabled = activeTab != TabDestination.KEYPAD) {
    viewModel.selectTab(TabDestination.KEYPAD)
  }

  LiquidBackground(
    hazeState = hazeState,
    modifier = Modifier.fillMaxSize()
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Screen container with smooth subtle crossfade
      Crossfade(
        targetState = activeTab,
        animationSpec = tween(180),
        label = "tab_crossfade",
        modifier = Modifier.fillMaxSize()
      ) { targetTab ->
        when (targetTab) {
          TabDestination.KEYPAD -> {
            KeypadScreen(
              keypadNumber = keypadNumber,
              matchedContact = matchedContact,
              onKeyPress = { viewModel.onKeypadPress(it) },
              onZeroLongPress = { viewModel.onKeypadZeroLongPress() },
              onDeletePress = { viewModel.onDeletePress() },
              onDeleteLongPress = { viewModel.onDeleteLongPress() },
              onCall = { viewModel.onCallKeypad() },
              hazeState = hazeState
            )
          }

          TabDestination.CONTACTS -> {
            ContactsScreen(
              contacts = filteredContacts,
              searchQuery = searchQuery,
              onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
              onContactClick = { viewModel.onCallContact(it) },
              onToggleStar = { viewModel.toggleStarred(it) },
              hazeState = hazeState
            )
          }

          TabDestination.RECENTS -> {
            RecentsScreen(
              recents = filteredRecents,
              selectedFilter = recentsFilter,
              onFilterSelected = { viewModel.onRecentsFilterChange(it) },
              onCallClick = { viewModel.onCallRecent(it) },
              hazeState = hazeState
            )
          }

          TabDestination.FAVORITES -> {
            FavoritesScreen(
              favorites = favorites,
              onContactClick = { viewModel.onCallContact(it) },
              onToggleStar = { viewModel.toggleStarred(it) },
              hazeState = hazeState
            )
          }

          TabDestination.VOICEMAIL -> {
            VoicemailScreen(
              onCallVoicemail = { viewModel.onCallVoicemail() },
              hazeState = hazeState
            )
          }
        }
      }

      // Detached floating pill bottom tab bar
      LiquidBottomBar(
        currentTab = activeTab,
        onTabSelected = { viewModel.selectTab(it) },
        hazeState = hazeState,
        modifier = Modifier.align(Alignment.BottomCenter)
      )

      // Friendly explanation dialog for permissions
      if (showPermissionPrompt) {
        PermissionExplanationDialog(
          onConfirm = {
            showPermissionPrompt = false
            permissionLauncher.launch(
              arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE
              )
            )
          },
          onDismiss = {
            showPermissionPrompt = false
          },
          hazeState = hazeState
        )
      }
    }
  }
}
