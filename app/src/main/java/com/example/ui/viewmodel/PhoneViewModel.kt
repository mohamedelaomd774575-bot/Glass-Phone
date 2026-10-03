package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.model.RecentCallItem
import com.example.data.model.RecentsFilter
import com.example.data.model.TabDestination
import com.example.data.repository.PhoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = PhoneRepository(application.applicationContext)

  private val _activeTab = MutableStateFlow(TabDestination.KEYPAD)
  val activeTab: StateFlow<TabDestination> = _activeTab.asStateFlow()

  private val _keypadNumber = MutableStateFlow("")
  val keypadNumber: StateFlow<String> = _keypadNumber.asStateFlow()

  private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
  val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _recents = MutableStateFlow<List<RecentCallItem>>(emptyList())
  val recents: StateFlow<List<RecentCallItem>> = _recents.asStateFlow()

  private val _recentsFilter = MutableStateFlow(RecentsFilter.ALL)
  val recentsFilter: StateFlow<RecentsFilter> = _recentsFilter.asStateFlow()

  private val _showPermissionSheet = MutableStateFlow(false)
  val showPermissionSheet: StateFlow<Boolean> = _showPermissionSheet.asStateFlow()

  val favorites: StateFlow<List<ContactItem>> = _contacts.combine(_keypadNumber) { list, _ ->
    list.filter { it.isStarred }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredContacts: StateFlow<List<ContactItem>> = combine(_contacts, _searchQuery) { list, query ->
    if (query.isBlank()) {
      list
    } else {
      val q = query.trim().lowercase(Locale.ROOT)
      list.filter {
        it.name.lowercase(Locale.ROOT).contains(q) ||
          it.phoneNumber.replace("[^0-9]".toRegex(), "").contains(q)
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredRecents: StateFlow<List<RecentCallItem>> = combine(_recents, _recentsFilter) { list, filter ->
    when (filter) {
      RecentsFilter.ALL -> list
      RecentsFilter.MISSED -> list.filter { it.callType == CallType.MISSED }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Contact matched against current keypad input (shows in blue under the number!)
  val matchedContact: StateFlow<ContactItem?> = combine(_contacts, _keypadNumber) { list, input ->
    val clean = input.replace("[^0-9+]".toRegex(), "")
    if (clean.length < 2) {
      null
    } else {
      list.firstOrNull {
        val contactDigits = it.phoneNumber.replace("[^0-9+]".toRegex(), "")
        contactDigits.contains(clean) || clean.contains(contactDigits)
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  init {
    loadData()
  }

  fun loadData() {
    viewModelScope.launch {
      _contacts.value = repository.loadContacts()
      _recents.value = repository.loadRecentCalls()
    }
  }

  fun selectTab(tab: TabDestination) {
    _activeTab.value = tab
  }

  fun onKeypadPress(key: Char) {
    _keypadNumber.value = _keypadNumber.value + key
    repository.playTone(key)
    repository.performHapticFeedback()
  }

  fun onKeypadZeroLongPress() {
    _keypadNumber.value = _keypadNumber.value + "+"
    repository.playTone('0')
    repository.performHapticFeedback()
  }

  fun onDeletePress() {
    val current = _keypadNumber.value
    if (current.isNotEmpty()) {
      _keypadNumber.value = current.dropLast(1)
      repository.performHapticFeedback()
    }
  }

  fun onDeleteLongPress() {
    if (_keypadNumber.value.isNotEmpty()) {
      _keypadNumber.value = ""
      repository.performHapticFeedback()
    }
  }

  fun onCallKeypad() {
    val number = _keypadNumber.value.ifEmpty {
      matchedContact.value?.phoneNumber ?: ""
    }
    if (number.isNotBlank()) {
      repository.makeCall(number)
    }
  }

  fun onCallContact(contact: ContactItem) {
    repository.makeCall(contact.phoneNumber)
  }

  fun onCallRecent(recent: RecentCallItem) {
    repository.makeCall(recent.phoneNumber)
  }

  fun onCallVoicemail() {
    repository.makeCall("*86")
  }

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  fun onRecentsFilterChange(filter: RecentsFilter) {
    _recentsFilter.value = filter
    repository.performHapticFeedback()
  }

  fun toggleStarred(contactId: String) {
    _contacts.value = _contacts.value.map {
      if (it.id == contactId) it.copy(isStarred = !it.isStarred) else it
    }
    repository.performHapticFeedback()
  }

  fun requestPermissionSheet() {
    _showPermissionSheet.value = true
  }

  fun dismissPermissionSheet() {
    _showPermissionSheet.value = false
  }
}
