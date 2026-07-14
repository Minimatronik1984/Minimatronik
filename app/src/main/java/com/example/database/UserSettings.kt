package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "Moja Preduzetnička Radnja",
    val pib: String = "109876543",
    val activityCode: String = "6201 - Računarsko programiranje",
    val monthlyTaxRsd: Double = 32000.0,
    val annualLimitRsd: Double = 6000000.0
)
