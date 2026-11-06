package com.example.data.util

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

data class DeviceContactInfo(
    val id: String,
    val displayName: String,
    val phoneNumber: String
)

object DeviceContactsHelper {

    fun readDeviceContacts(context: Context): List<DeviceContactInfo> {
        val contactsList = mutableListOf<DeviceContactInfo>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val seenNumbers = mutableSetOf<String>()

                while (it.moveToNext()) {
                    val rawId = if (idIndex != -1) it.getString(idIndex) ?: "" else ""
                    val name = if (nameIndex != -1) it.getString(nameIndex) ?: "" else ""
                    val rawNumber = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""

                    val normalizedNumber = normalizePhoneNumber(rawNumber)
                    if (name.isNotBlank() && normalizedNumber.isNotBlank() && !seenNumbers.contains(normalizedNumber)) {
                        seenNumbers.add(normalizedNumber)
                        contactsList.add(
                            DeviceContactInfo(
                                id = rawId.ifBlank { normalizedNumber },
                                displayName = name.trim(),
                                phoneNumber = rawNumber.trim()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceContactsHelper", "Error reading contacts: ${e.message}", e)
        }

        return contactsList
    }

    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace("[^0-9+]".toRegex(), "")
    }
}
