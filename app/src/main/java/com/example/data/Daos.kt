package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET garageName = :garageName WHERE id = :id")
    suspend fun updateGarageName(id: String, garageName: String): Int

    @Query("UPDATE users SET cityId = :cityId WHERE id = :id")
    suspend fun updateUserCity(id: String, cityId: String): Int
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE userId = :userId")
    fun getVehiclesByUser(userId: String): Flow<List<Vehicle>>
    
    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    fun getVehicleById(id: Int): Flow<Vehicle?>

    @Query("SELECT * FROM vehicles WHERE userId = :userId")
    suspend fun getVehiclesListByUser(userId: String): List<Vehicle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: Int)
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getFuelRecords(vehicleId: Int): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    suspend fun getFuelRecordsList(vehicleId: Int): List<FuelRecord>

    @Query("SELECT * FROM fuel_records WHERE vehicleId IN (SELECT id FROM vehicles WHERE userId = :userId) ORDER BY date DESC")
    fun getFuelRecordsByUser(userId: String): Flow<List<FuelRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: FuelRecord)
    
    @Query("DELETE FROM fuel_records WHERE id = :id")
    suspend fun deleteFuelRecord(id: Int)

    @Query("DELETE FROM fuel_records WHERE vehicleId = :vehicleId")
    suspend fun deleteFuelRecordsByVehicle(vehicleId: Int)
    
    @Query("SELECT * FROM expense_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getExpenseRecords(vehicleId: Int): Flow<List<ExpenseRecord>>

    @Query("SELECT * FROM expense_records WHERE vehicleId IN (SELECT id FROM vehicles WHERE userId = :userId) ORDER BY date DESC")
    fun getExpenseRecordsByUser(userId: String): Flow<List<ExpenseRecord>>

    @Query("SELECT * FROM expense_records ORDER BY date DESC")
    fun getAllExpenseRecords(): Flow<List<ExpenseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseRecord(record: ExpenseRecord)
    
    @Query("DELETE FROM expense_records WHERE id = :id")
    suspend fun deleteExpenseRecord(id: Int)
    
    // For Dashboard Stats
    @Query("SELECT SUM(total) FROM fuel_records WHERE vehicleId IN (SELECT id FROM vehicles WHERE userId = :userId)")
    fun getTotalFuelCostByUser(userId: String): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE vehicleId IN (SELECT id FROM vehicles WHERE userId = :userId)")
    fun getTotalExpenseCostByUser(userId: String): Flow<Double?>
    
    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getMaintenanceRecords(vehicleId: Int): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE vehicleId IN (SELECT id FROM vehicles WHERE userId = :userId) ORDER BY date DESC")
    fun getMaintenanceRecordsByUser(userId: String): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records ORDER BY date DESC")
    fun getAllMaintenanceRecords(): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRecord(record: MaintenanceRecord)
    
    @Query("DELETE FROM maintenance_records WHERE id = :id")
    suspend fun deleteMaintenanceRecord(id: Int)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY dueDate ASC")
    fun getReminders(vehicleId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)
}
