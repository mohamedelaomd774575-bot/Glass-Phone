package com.example.data.model

enum class CallType {
  INCOMING,
  OUTGOING,
  MISSED
}

data class ContactItem(
  val id: String,
  val name: String,
  val phoneNumber: String,
  val label: String = "Mobile",
  val photoUri: String? = null,
  val isStarred: Boolean = false,
  val initials: String = computeInitials(name),
  val gradientIndex: Int = (name.hashCode() and 0x7FFFFFFF) % 7
) {
  companion object {
    fun computeInitials(name: String): String {
      val trimmed = name.trim()
      if (trimmed.isEmpty()) return "?"
      val parts = trimmed.split("\\s+".toRegex())
      return if (parts.size >= 2) {
        "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
      } else {
        trimmed.take(2).uppercase()
      }
    }
  }
}

data class RecentCallItem(
  val id: String,
  val contactName: String,
  val phoneNumber: String,
  val callType: CallType,
  val timestampMillis: Long,
  val durationSeconds: Int = 0,
  val formattedTime: String,
  val count: Int = 1,
  val initials: String = ContactItem.computeInitials(contactName),
  val gradientIndex: Int = (contactName.hashCode() and 0x7FFFFFFF) % 7
)

data class DialerKey(
  val number: String,
  val letters: String
)

enum class TabDestination {
  FAVORITES,
  RECENTS,
  CONTACTS,
  KEYPAD,
  VOICEMAIL
}

enum class RecentsFilter {
  ALL,
  MISSED
}
