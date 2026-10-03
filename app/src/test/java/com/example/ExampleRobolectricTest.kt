package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ContactItem
import com.example.data.model.TabDestination
import com.example.data.repository.PhoneRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Phone", appName)
  }

  @Test
  fun `verify contact initials calculation`() {
    assertEquals("AW", ContactItem.computeInitials("Alexander Wright"))
    assertEquals("DO", ContactItem.computeInitials("David"))
    assertEquals("?", ContactItem.computeInitials(""))
  }

  @Test
  fun `repository loads sample contacts on missing permissions`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = PhoneRepository(context)
    val contacts = repository.loadContacts()
    assertTrue(contacts.isNotEmpty())
    assertNotNull(contacts.firstOrNull())
  }
}
