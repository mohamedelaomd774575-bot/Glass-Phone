package com.example.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.model.RecentCallItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhoneRepository(private val context: Context) {

  private var toneGenerator: ToneGenerator? = try {
    ToneGenerator(AudioManager.STREAM_MUSIC, 80)
  } catch (e: Exception) {
    try {
      ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
    } catch (_: Exception) {
      null
    }
  }

  private val vibrator: Vibrator? = try {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  } catch (_: Exception) {
    null
  }

  suspend fun loadContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
      return@withContext getSampleContacts()
    }

    val contactsList = mutableListOf<ContactItem>()
    val contentResolver: ContentResolver = context.contentResolver
    val projection = arrayOf(
      ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
      ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
      ContactsContract.CommonDataKinds.Phone.NUMBER,
      ContactsContract.CommonDataKinds.Phone.TYPE,
      ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
      ContactsContract.CommonDataKinds.Phone.STARRED
    )

    try {
      contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
      )?.use { cursor ->
        val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
        val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val starIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

        val seen = HashSet<String>()
        while (cursor.moveToNext()) {
          val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
          val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "Unknown" else "Unknown"
          val rawNumber = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
          val number = rawNumber.trim()
          val photoUri = if (photoIdx >= 0) cursor.getString(photoIdx) else null
          val isStarred = if (starIdx >= 0) cursor.getInt(starIdx) == 1 else false

          val typeCode = if (typeIdx >= 0) cursor.getInt(typeIdx) else ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
          val label = when (typeCode) {
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Main"
            else -> "Mobile"
          }

          val key = "$name-$number"
          if (number.isNotEmpty() && seen.add(key)) {
            contactsList.add(
              ContactItem(
                id = id.ifEmpty { number },
                name = name,
                phoneNumber = number,
                label = label,
                photoUri = photoUri,
                isStarred = isStarred
              )
            )
          }
        }
      }
    } catch (_: Exception) {
      // ignore and fallback
    }

    if (contactsList.isEmpty()) {
      getSampleContacts()
    } else {
      contactsList.sortedBy { it.name.lowercase(Locale.ROOT) }
    }
  }

  suspend fun loadRecentCalls(): List<RecentCallItem> = withContext(Dispatchers.IO) {
    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.READ_CALL_LOG
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
      return@withContext getSampleRecents()
    }

    val recents = mutableListOf<RecentCallItem>()
    val contentResolver: ContentResolver = context.contentResolver
    val projection = arrayOf(
      CallLog.Calls._ID,
      CallLog.Calls.CACHED_NAME,
      CallLog.Calls.NUMBER,
      CallLog.Calls.TYPE,
      CallLog.Calls.DATE,
      CallLog.Calls.DURATION
    )

    try {
      contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        projection,
        null,
        null,
        "${CallLog.Calls.DATE} DESC"
      )?.use { cursor ->
        val idIdx = cursor.getColumnIndex(CallLog.Calls._ID)
        val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
        val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
        val durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION)

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("M/d/yy", Locale.getDefault())
        val now = System.currentTimeMillis()

        while (cursor.moveToNext() && recents.size < 60) {
          val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
          val cachedName = if (nameIdx >= 0) cursor.getString(nameIdx) else null
          val number = if (numIdx >= 0) cursor.getString(numIdx) ?: "Unknown" else "Unknown"
          val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
          val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else now
          val duration = if (durIdx >= 0) cursor.getInt(durIdx) else 0

          val callType = when (type) {
            CallLog.Calls.MISSED_TYPE, CallLog.Calls.REJECTED_TYPE -> CallType.MISSED
            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
            else -> CallType.INCOMING
          }

          val contactName = if (!cachedName.isNullOrBlank()) cachedName else number

          val isToday = (now - date) < 24 * 60 * 60 * 1000 && Date(now).day == Date(date).day
          val formattedTime = if (isToday) {
            timeFormatter.format(Date(date))
          } else {
            dateFormatter.format(Date(date))
          }

          recents.add(
            RecentCallItem(
              id = id.ifEmpty { "$date-$number" },
              contactName = contactName,
              phoneNumber = number,
              callType = callType,
              timestampMillis = date,
              durationSeconds = duration,
              formattedTime = formattedTime
            )
          )
        }
      }
    } catch (_: Exception) {
      // fallback
    }

    if (recents.isEmpty()) {
      getSampleRecents()
    } else {
      recents
    }
  }

  fun makeCall(phoneNumber: String) {
    val clean = phoneNumber.replace("[^0-9+#*]".toRegex(), "")
    if (clean.isEmpty()) return

    val uri = Uri.parse("tel:$clean")
    val hasCallPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED

    val intent = if (hasCallPermission) {
      Intent(Intent.ACTION_CALL, uri)
    } else {
      Intent(Intent.ACTION_DIAL, uri)
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
      context.startActivity(intent)
    } catch (_: Exception) {
      val fallbackIntent = Intent(Intent.ACTION_DIAL, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      try {
        context.startActivity(fallbackIntent)
      } catch (_: Exception) {
        // failed to launch dialer
      }
    }
  }

  fun playTone(key: Char) {
    val tone = when (key) {
      '1' -> ToneGenerator.TONE_DTMF_1
      '2' -> ToneGenerator.TONE_DTMF_2
      '3' -> ToneGenerator.TONE_DTMF_3
      '4' -> ToneGenerator.TONE_DTMF_4
      '5' -> ToneGenerator.TONE_DTMF_5
      '6' -> ToneGenerator.TONE_DTMF_6
      '7' -> ToneGenerator.TONE_DTMF_7
      '8' -> ToneGenerator.TONE_DTMF_8
      '9' -> ToneGenerator.TONE_DTMF_9
      '0' -> ToneGenerator.TONE_DTMF_0
      '*' -> ToneGenerator.TONE_DTMF_S
      '#' -> ToneGenerator.TONE_DTMF_P
      else -> ToneGenerator.TONE_PROP_BEEP
    }

    try {
      toneGenerator?.startTone(tone, 120)
    } catch (_: Exception) {
      // ignore
    }
  }

  fun performHapticFeedback() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(22, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(22)
      }
    } catch (_: Exception) {
      // ignore
    }
  }

  private fun getSampleContacts(): List<ContactItem> {
    return listOf(
      ContactItem(id = "1", name = "Alexander Wright", phoneNumber = "(415) 555-0142", label = "Mobile", isStarred = true),
      ContactItem(id = "2", name = "Charlotte Miller", phoneNumber = "(650) 555-0198", label = "Work", isStarred = true),
      ContactItem(id = "3", name = "David Kim", phoneNumber = "(408) 555-0123", label = "Mobile", isStarred = false),
      ContactItem(id = "4", name = "Elena Rostova", phoneNumber = "(510) 555-0177", label = "Home", isStarred = true),
      ContactItem(id = "5", name = "Harper Vance", phoneNumber = "(212) 555-0199", label = "Mobile", isStarred = false),
      ContactItem(id = "6", name = "James Wilson", phoneNumber = "(312) 555-0184", label = "Mobile", isStarred = true),
      ContactItem(id = "7", name = "Liam Johnson", phoneNumber = "(206) 555-0165", label = "Work", isStarred = false),
      ContactItem(id = "8", name = "Maya Patel", phoneNumber = "(617) 555-0131", label = "Mobile", isStarred = true),
      ContactItem(id = "9", name = "Noah Taylor", phoneNumber = "(512) 555-0112", label = "Mobile", isStarred = false),
      ContactItem(id = "10", name = "Olivia Garcia", phoneNumber = "(305) 555-0155", label = "Mobile", isStarred = true),
      ContactItem(id = "11", name = "Lucas Thorne", phoneNumber = "(702) 555-0188", label = "Main", isStarred = false),
      ContactItem(id = "12", name = "Sophia Martinez", phoneNumber = "(619) 555-0149", label = "Mobile", isStarred = true),
      ContactItem(id = "13", name = "William Bennett", phoneNumber = "(404) 555-0163", label = "Home", isStarred = false),
      ContactItem(id = "14", name = "Zoe Sterling", phoneNumber = "(303) 555-0171", label = "Mobile", isStarred = false)
    ).sortedBy { it.name }
  }

  private fun getSampleRecents(): List<RecentCallItem> {
    return listOf(
      RecentCallItem(id = "r1", contactName = "Olivia Garcia", phoneNumber = "(305) 555-0155", callType = CallType.MISSED, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 14, formattedTime = "10:48 AM", count = 2),
      RecentCallItem(id = "r2", contactName = "Alexander Wright", phoneNumber = "(415) 555-0142", callType = CallType.INCOMING, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 55, formattedTime = "10:07 AM"),
      RecentCallItem(id = "r3", contactName = "Charlotte Miller", phoneNumber = "(650) 555-0198", callType = CallType.OUTGOING, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 180, formattedTime = "8:42 AM"),
      RecentCallItem(id = "r4", contactName = "(800) 555-0190", phoneNumber = "(800) 555-0190", callType = CallType.MISSED, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 24, formattedTime = "Yesterday"),
      RecentCallItem(id = "r5", contactName = "James Wilson", phoneNumber = "(312) 555-0184", callType = CallType.OUTGOING, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 26, formattedTime = "Yesterday"),
      RecentCallItem(id = "r6", contactName = "David Kim", phoneNumber = "(408) 555-0123", callType = CallType.INCOMING, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 50, formattedTime = "Tuesday"),
      RecentCallItem(id = "r7", contactName = "Elena Rostova", phoneNumber = "(510) 555-0177", callType = CallType.MISSED, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 72, formattedTime = "Monday"),
      RecentCallItem(id = "r8", contactName = "Maya Patel", phoneNumber = "(617) 555-0131", callType = CallType.OUTGOING, timestampMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 96, formattedTime = "Sunday")
    )
  }
}
