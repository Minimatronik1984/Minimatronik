package com.example.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainRepository(private val database: AppDatabase) {
    val allInvoices: Flow<List<Invoice>> = database.invoiceDao().getAllInvoicesFlow()
    val userSettings: Flow<UserSettings?> = database.userSettingsDao().getSettingsFlow()

    suspend fun getSettings(): UserSettings {
        return database.userSettingsDao().getSettings() ?: UserSettings().also {
            database.userSettingsDao().insertSettings(it)
        }
    }

    suspend fun insertInvoice(invoice: Invoice) {
        database.invoiceDao().insertInvoice(invoice)
    }

    suspend fun updateInvoice(invoice: Invoice) {
        database.invoiceDao().updateInvoice(invoice)
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        database.invoiceDao().deleteInvoice(invoice)
    }

    suspend fun deleteInvoiceById(id: Int) {
        database.invoiceDao().deleteInvoiceById(id)
    }

    suspend fun saveSettings(settings: UserSettings) {
        database.userSettingsDao().insertSettings(settings)
    }
}
