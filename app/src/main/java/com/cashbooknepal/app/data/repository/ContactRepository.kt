package com.cashbooknepal.app.data.repository

import com.cashbooknepal.app.data.local.dao.ContactDao
import com.cashbooknepal.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun insertContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun getContactById(id: Long): ContactEntity? {
        return contactDao.getContactById(id)
    }
}