package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // email or user id
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val email: String = "",
    val passwordHash: String = "", // Not secure but it's a mock
    val garageName: String = "",
    val cityId: String = "ist_avr"
)

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val brand: String,
    val model: String,
    val year: String,
    val plate: String,
    val fuelType: String
)

@Entity(tableName = "fuel_records")
data class FuelRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val liters: Double,
    val price: Double,
    val total: Double, // calculated
    val km: Int,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "expense_records")
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val category: String,
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val title: String,
    val dueDate: Long
)

@Entity(tableName = "maintenance_records")
data class MaintenanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val type: String,
    val description: String,
    val km: Int,
    val cost: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)
