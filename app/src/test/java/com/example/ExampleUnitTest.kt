package com.example

import com.example.data.model.ChatContact
import com.example.data.remote.FirestoreConversation
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun filterConversations_byContactName_returnsMatching() {
    val conversations = listOf(
      FirestoreConversation(
        id = "1",
        title = "Ali Rezaei",
        lastMessage = "سلام، پروژه چطور پیش میره؟",
        lastSender = "Ali"
      ),
      FirestoreConversation(
        id = "2",
        title = "Sara Mohammadi",
        lastMessage = "فایل ارسال شد",
        lastSender = "Sara"
      )
    )

    val query = "Ali"
    val filtered = conversations.filter {
      it.title.contains(query, ignoreCase = true) ||
      it.lastSender.contains(query, ignoreCase = true) ||
      it.lastMessage.contains(query, ignoreCase = true)
    }

    assertEquals(1, filtered.size)
    assertEquals("Ali Rezaei", filtered[0].title)
  }

  @Test
  fun filterConversations_byMessageContent_returnsMatching() {
    val conversations = listOf(
      FirestoreConversation(
        id = "1",
        title = "Ali Rezaei",
        lastMessage = "سلام، جلسه ساعت ۵ برگزار میشه",
        lastSender = "Ali"
      ),
      FirestoreConversation(
        id = "2",
        title = "تیم توسعه",
        lastMessage = "کدها روی گیت‌پوش شد",
        lastSender = "Mohsen"
      )
    )

    val query = "جلسه"
    val filtered = conversations.filter {
      it.title.contains(query, ignoreCase = true) ||
      it.lastSender.contains(query, ignoreCase = true) ||
      it.lastMessage.contains(query, ignoreCase = true)
    }

    assertEquals(1, filtered.size)
    assertEquals("Ali Rezaei", filtered[0].title)
  }
}

