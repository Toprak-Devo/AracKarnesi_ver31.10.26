package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.Locale

class AppRepository(
    private val userDao: UserDao,
    private val vehicleDao: VehicleDao,
    private val recordDao: RecordDao,
    private val reminderDao: ReminderDao,
    private val fuelPriceService: FuelPriceService = FuelPriceService(),
    private val vehicleCatalogService: VehicleCatalogService = VehicleCatalogService()
) {
    private val seedMutex = Mutex()

    val selectedFuelCityFlow: StateFlow<FuelCityOption> = fuelPriceService.selectedCityFlow

    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
        if (user.cityId.isNotBlank()) {
            fuelPriceService.syncSelectedCityById(user.cityId)
        }
    }

    suspend fun updateUserGarageName(userId: String, garageName: String) {
        val cleaned = garageName.trim()
        val updatedRows = userDao.updateGarageName(userId, cleaned)
        if (updatedRows == 0) {
            userDao.insertUser(
                User(
                    id = userId,
                    name = "Toprak",
                    surname = "Baran",
                    phone = "+90 532 486 74 12",
                    email = if (userId == "default_user") "toprakmuco321@gmail.com" else userId,
                    garageName = cleaned,
                    cityId = selectedFuelCityFlow.value.id
                )
            )
        }
    }

    suspend fun updateUserCity(userId: String, city: FuelCityOption) {
        fuelPriceService.setSelectedCity(city)
        val updatedRows = userDao.updateUserCity(userId, city.id)
        if (updatedRows == 0) {
            userDao.insertUser(
                User(
                    id = userId,
                    name = "Toprak",
                    surname = "Baran",
                    phone = "+90 532 486 74 12",
                    email = if (userId == "default_user") "toprakmuco321@gmail.com" else userId,
                    garageName = "TOPRAK BARAN",
                    cityId = city.id
                )
            )
        }
    }

    fun syncFuelCityFromUser(cityId: String?) {
        fuelPriceService.syncSelectedCityById(cityId)
    }

    fun getVehiclesByUser(userId: String): Flow<List<Vehicle>> = vehicleDao.getVehiclesByUser(userId)
    fun getVehicleById(id: Int): Flow<Vehicle?> = vehicleDao.getVehicleById(id)
    suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insertVehicle(vehicle)
    suspend fun deleteVehicle(id: Int) = vehicleDao.deleteVehicle(id)

    suspend fun seedInitialProfileAndRecords(
        userId: String,
        forceProfileUpdate: Boolean,
        forceFuelTimelineUpgrade: Boolean = false
    ) {
        seedMutex.withLock {
            val existingUser = userDao.getUserByEmail(userId)
            val isLegacyDefault = existingUser != null &&
                (existingUser.email == "iiremtrkylmzzz@gmail.com" || existingUser.name == "İrem" || existingUser.name.isBlank())

            if (forceProfileUpdate || existingUser == null || isLegacyDefault) {
                val phoneToKeep = if (existingUser != null && existingUser.phone.isNotBlank() && !isLegacyDefault) {
                    existingUser.phone
                } else {
                    "+90 532 486 74 12"
                }
                val garageToKeep = existingUser?.garageName?.takeIf { it.isNotBlank() } ?: "TOPRAK BARAN"
                val cityToKeep = existingUser?.cityId?.takeIf { it.isNotBlank() } ?: fuelPriceService.selectedCityFlow.value.id
                userDao.insertUser(
                    User(
                        id = userId,
                        name = "Toprak",
                        surname = "Baran",
                        phone = phoneToKeep,
                        email = if (userId == "default_user") "toprakmuco321@gmail.com" else userId,
                        garageName = garageToKeep,
                        cityId = cityToKeep
                    )
                )
                fuelPriceService.syncSelectedCityById(cityToKeep)
            } else {
                if (existingUser.garageName.isBlank()) {
                    val fallbackGarage = "${existingUser.name} ${existingUser.surname}".trim().ifBlank { "TOPRAK BARAN" }
                    userDao.updateGarageName(userId, fallbackGarage)
                }
                if (existingUser.cityId.isNotBlank()) {
                    fuelPriceService.syncSelectedCityById(existingUser.cityId)
                }
            }

            val existingVehicles = vehicleDao.getVehiclesListByUser(userId)
            val shouldSeedVehicle = existingVehicles.none { it.plate == "34 TB 2022" } &&
                (forceProfileUpdate || existingVehicles.isEmpty())

            if (shouldSeedVehicle) {
                val now = System.currentTimeMillis()
                val dayMs = 24L * 60L * 60L * 1000L

                val vehicleId = vehicleDao.insertVehicle(
                    Vehicle(
                        userId = userId,
                        brand = "Fiat",
                        model = "Egea 1.6 MultiJet Lounge",
                        year = "2022",
                        plate = "34 TB 2022",
                        fuelType = "Dizel"
                    )
                ).toInt()

                // 4 Bakım Kaydı
                val sampleMaintenances = listOf(
                    MaintenanceRecord(
                        vehicleId = vehicleId,
                        type = "Periyodik Bakım, Hava Filtresi, Polen Filtresi",
                        description = "60.000 km periyodik bakım yapıldı. 5W-30 Selenia motor yağı, yağ, hava ve polen filtreleri orijinal parçalarla yenilendi.",
                        km = 60450,
                        cost = 5850.0,
                        date = now - (18L * dayMs)
                    ),
                    MaintenanceRecord(
                        vehicleId = vehicleId,
                        type = "Fren Balata & Disk, Antifriz & Sıvı Bakımı",
                        description = "Ön fren diskleri ve ön-arka Bosch fren balataları değiştirildi, fren hidrolik sıvısı ve antifriz yenilendi.",
                        km = 52300,
                        cost = 7400.0,
                        date = now - (115L * dayMs)
                    ),
                    MaintenanceRecord(
                        vehicleId = vehicleId,
                        type = "Mazot Pompası / Filtresi, V Kayışı",
                        description = "45.000 km kontrolünde yakıt (mazot) filtresi ve alternatör V kayışı yenilendi.",
                        km = 45100,
                        cost = 4600.0,
                        date = now - (230L * dayMs)
                    ),
                    MaintenanceRecord(
                        vehicleId = vehicleId,
                        type = "Periyodik Bakım, Rot Balans & Kışlık Kontrol",
                        description = "30.000 km yetkili servis periyodik bakımı ve 4 teker bilgisayarlı rot-balans ayarı tamamlandı.",
                        km = 30200,
                        cost = 4200.0,
                        date = now - (380L * dayMs)
                    )
                )
                sampleMaintenances.forEach { recordDao.insertMaintenanceRecord(it) }

                // 4 Gider Kaydı
                val sampleExpenses = listOf(
                    ExpenseRecord(
                        vehicleId = vehicleId,
                        category = "Kasko",
                        description = "Genişletilmiş Full Kasko Poliçesi",
                        amount = 14800.0,
                        date = now - (42L * dayMs)
                    ),
                    ExpenseRecord(
                        vehicleId = vehicleId,
                        category = "Sigorta",
                        description = "Zorunlu Trafik Sigortası Yenileme",
                        amount = 6450.0,
                        date = now - (42L * dayMs)
                    ),
                    ExpenseRecord(
                        vehicleId = vehicleId,
                        category = "Vergi (MTV)",
                        description = "Motorlu Taşıtlar Vergisi Dönem Ödemesi",
                        amount = 3350.0,
                        date = now - (65L * dayMs)
                    ),
                    ExpenseRecord(
                        vehicleId = vehicleId,
                        category = "Yıkama",
                        description = "Detaylı İç-Dış Temizlik & Boya Koruma Uygulaması",
                        amount = 2200.0,
                        date = now - (12L * dayMs)
                    )
                )
                sampleExpenses.forEach { recordDao.insertExpenseRecord(it) }

                // 6 Aylık Akaryakıt Kaydı (Aylık Grafik Analizi İçin)
                insertSixMonthSampleFuels(vehicleId, now)

                // 3 Hatırlatıcı Kaydı
                val sampleReminders = listOf(
                    Reminder(
                        vehicleId = vehicleId,
                        title = "TÜVTÜRK Araç Muayenesi",
                        dueDate = now + (5L * dayMs)
                    ),
                    Reminder(
                        vehicleId = vehicleId,
                        title = "Kış Lastiği Değişimi",
                        dueDate = now + (28L * dayMs)
                    ),
                    Reminder(
                        vehicleId = vehicleId,
                        title = "Periyodik Bakım (75.000 KM)",
                        dueDate = now + (120L * dayMs)
                    )
                )
                sampleReminders.forEach { reminderDao.insertReminder(it) }
            } else if (forceFuelTimelineUpgrade) {
                val targetVehicle = existingVehicles.firstOrNull { it.plate == "34 TB 2022" }
                if (targetVehicle != null) {
                    val existingFuels = recordDao.getFuelRecordsList(targetVehicle.id)
                    if (existingFuels.size <= 4) {
                        recordDao.deleteFuelRecordsByVehicle(targetVehicle.id)
                        insertSixMonthSampleFuels(targetVehicle.id, System.currentTimeMillis())
                    }
                }
            }
        }
    }

    private suspend fun insertSixMonthSampleFuels(vehicleId: Int, anchorMillis: Long) {
        val trLocale = Locale.forLanguageTag("tr-TR")
        fun monthTimestamp(monthsBack: Int): Long {
            return Calendar.getInstance(trLocale).apply {
                timeInMillis = anchorMillis
                val safeDay = get(Calendar.DAY_OF_MONTH).coerceAtMost(24)
                set(Calendar.DAY_OF_MONTH, safeDay)
                add(Calendar.MONTH, -monthsBack)
            }.timeInMillis
        }

        val sampleFuels = listOf(
            FuelRecord(
                vehicleId = vehicleId,
                liters = 46.0,
                price = 78.40,
                total = 46.0 * 78.40,
                km = 55400,
                date = monthTimestamp(5)
            ),
            FuelRecord(
                vehicleId = vehicleId,
                liters = 61.5,
                price = 81.20,
                total = 61.5 * 81.20,
                km = 56480,
                date = monthTimestamp(4)
            ),
            FuelRecord(
                vehicleId = vehicleId,
                liters = 73.0,
                price = 85.50,
                total = 73.0 * 85.50,
                km = 57720,
                date = monthTimestamp(3)
            ),
            FuelRecord(
                vehicleId = vehicleId,
                liters = 70.2,
                price = 89.40,
                total = 70.2 * 89.40,
                km = 58950,
                date = monthTimestamp(2)
            ),
            FuelRecord(
                vehicleId = vehicleId,
                liters = 64.4,
                price = 92.80,
                total = 64.4 * 92.80,
                km = 60080,
                date = monthTimestamp(1)
            ),
            FuelRecord(
                vehicleId = vehicleId,
                liters = 65.0,
                price = 95.90,
                total = 65.0 * 95.90,
                km = 61200,
                date = monthTimestamp(0)
            )
        )
        sampleFuels.forEach { recordDao.insertFuelRecord(it) }
    }
    
    fun getFuelRecords(vehicleId: Int): Flow<List<FuelRecord>> = recordDao.getFuelRecords(vehicleId)
    fun getFuelRecordsByUser(userId: String): Flow<List<FuelRecord>> = recordDao.getFuelRecordsByUser(userId)
    suspend fun fetchLiveFuelPrices(city: FuelCityOption): FuelPriceUiState = fuelPriceService.fetchLivePrices(city)
    suspend fun insertFuelRecord(record: FuelRecord) = recordDao.insertFuelRecord(record)
    suspend fun deleteFuelRecord(id: Int) = recordDao.deleteFuelRecord(id)
    
    fun getExpenseRecords(vehicleId: Int): Flow<List<ExpenseRecord>> = recordDao.getExpenseRecords(vehicleId)
    fun getExpenseRecordsByUser(userId: String): Flow<List<ExpenseRecord>> = recordDao.getExpenseRecordsByUser(userId)
    fun getAllExpenseRecords(): Flow<List<ExpenseRecord>> = recordDao.getAllExpenseRecords()
    suspend fun insertExpenseRecord(record: ExpenseRecord) = recordDao.insertExpenseRecord(record)
    suspend fun deleteExpenseRecord(id: Int) = recordDao.deleteExpenseRecord(id)
    
    fun getMaintenanceRecords(vehicleId: Int): Flow<List<MaintenanceRecord>> = recordDao.getMaintenanceRecords(vehicleId)
    fun getMaintenanceRecordsByUser(userId: String): Flow<List<MaintenanceRecord>> = recordDao.getMaintenanceRecordsByUser(userId)
    fun getAllMaintenanceRecords(): Flow<List<MaintenanceRecord>> = recordDao.getAllMaintenanceRecords()
    suspend fun insertMaintenanceRecord(record: MaintenanceRecord) = recordDao.insertMaintenanceRecord(record)
    suspend fun deleteMaintenanceRecord(id: Int) = recordDao.deleteMaintenanceRecord(id)
    
    fun getTotalFuelCostByUser(userId: String) = recordDao.getTotalFuelCostByUser(userId)
    fun getTotalExpenseCostByUser(userId: String) = recordDao.getTotalExpenseCostByUser(userId)

    fun getReminders(vehicleId: Int): Flow<List<Reminder>> = reminderDao.getReminders(vehicleId)
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    suspend fun deleteReminder(id: Int) = reminderDao.deleteReminder(id)

    fun getInitialVehicleCatalog(): VehicleCatalogData = vehicleCatalogService.getInitialCatalog()
    suspend fun fetchLiveVehicleCatalog(): VehicleCatalogData = vehicleCatalogService.fetchLiveCatalog()
    suspend fun enrichBrandModelsFromNhtsa(
        brandName: String,
        currentCatalog: VehicleCatalogData
    ): VehicleCatalogData = vehicleCatalogService.fetchModelsForBrandFromNhtsa(brandName, currentCatalog)

    fun getEnginesForVehicle(brand: String, model: String): List<VehicleEngineOption> =
        vehicleCatalogService.getEnginesForVehicle(brand, model)

    suspend fun fetchEnginesForVehicle(
        brand: String,
        model: String,
        year: String
    ): List<VehicleEngineOption> =
        vehicleCatalogService.fetchEnginesForVehicle(brand, model, year)
}

