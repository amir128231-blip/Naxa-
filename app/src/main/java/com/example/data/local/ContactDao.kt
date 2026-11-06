package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllContacts(): Flow<List<ChatContact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ChatContact?

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun observeContactById(id: String): Flow<ChatContact?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateContact(contact: ChatContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ChatContact>)

    @Update
    suspend fun updateContact(contact: ChatContact)

    @Query("UPDATE contacts SET lastMessageText = :text, lastMessageTime = :time WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, text: String, time: Long)

    @Query("UPDATE contacts SET lastMessageText = :text, lastMessageTime = :time, lastMessageIsOutgoing = :isOutgoing, lastMessageStatus = :status WHERE id = :chatId")
    suspend fun updateLastMessageInfo(chatId: String, text: String, time: Long, isOutgoing: Boolean, status: String)

    @Query("UPDATE contacts SET lastMessageStatus = :status WHERE id = :chatId")
    suspend fun updateLastMessageStatus(chatId: String, status: String)

    @Query("UPDATE contacts SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :chatId")
    suspend fun updateOnlineStatus(chatId: String, isOnline: Boolean, lastSeen: String)

    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :chatId")
    suspend fun markChatAsRead(chatId: String)

    @Query("UPDATE contacts SET isPinned = NOT isPinned WHERE id = :chatId")
    suspend fun togglePin(chatId: String)

    @Query("UPDATE contacts SET isMuted = NOT isMuted WHERE id = :chatId")
    suspend fun toggleMute(chatId: String)

    @Query("DELETE FROM contacts WHERE id = :chatId")
    suspend fun deleteContact(chatId: String)
}
