package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AppRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500) // Simulate network delay
            val user = repository.getUserByEmail(email)
            if (user != null && user.passwordHash == passwordHash) {
                _currentUser.value = user.id
                onSuccess()
            } else {
                onError("Hatalı e-posta veya şifre!")
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(500) // Simulate network delay
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onError("Bu e-posta zaten kayıtlı!")
            } else {
                repository.insertUser(User(email, passwordHash))
                _currentUser.value = email
                onSuccess()
            }
            _isLoading.value = false
        }
    }
    
    fun logout() {
        _currentUser.value = null
    }
}

class DashboardViewModel(private val repository: AppRepository, private val userId: String) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val user: StateFlow<User?> = repository.getUserById(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vehicles: StateFlow<List<Vehicle>> = repository.getVehiclesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaintenanceRecords: StateFlow<List<MaintenanceRecord>> = repository.getMaintenanceRecordsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allExpenseRecords: StateFlow<List<ExpenseRecord>> = repository.getExpenseRecordsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFuelRecords: StateFlow<List<FuelRecord>> = repository.getFuelRecordsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId

    val selectedFuelCity: StateFlow<FuelCityOption> = repository.selectedFuelCityFlow

    private val _fuelPriceState = MutableStateFlow<FuelPriceUiState>(FuelPriceUiState.Loading)
    val fuelPriceState: StateFlow<FuelPriceUiState> = _fuelPriceState

    private val _vehicleCatalog = MutableStateFlow(repository.getInitialVehicleCatalog())
    val vehicleCatalog: StateFlow<VehicleCatalogData> = _vehicleCatalog

    private val _isSyncingCatalog = MutableStateFlow(false)
    val isSyncingCatalog: StateFlow<Boolean> = _isSyncingCatalog

    private val _vehicleEngineOptions = MutableStateFlow<List<VehicleEngineOption>>(emptyList())
    val vehicleEngineOptions: StateFlow<List<VehicleEngineOption>> = _vehicleEngineOptions

    private val _isLoadingEngines = MutableStateFlow(false)
    val isLoadingEngines: StateFlow<Boolean> = _isLoadingEngines

    private var engineFetchJob: Job? = null

    private val enrichedBrands = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            user.filterNotNull().collect { u ->
                if (u.cityId.isNotBlank()) {
                    repository.syncFuelCityFromUser(u.cityId)
                }
            }
        }
        viewModelScope.launch {
            repository.selectedFuelCityFlow.collectLatest { city ->
                _fuelPriceState.value = FuelPriceUiState.Loading
                _fuelPriceState.value = repository.fetchLiveFuelPrices(city)
            }
        }
        refreshVehicleCatalog()
    }

    fun selectVehicle(vehicleId: Int) {
        _selectedVehicleId.value = vehicleId
    }

    fun selectFuelCity(city: FuelCityOption) {
        viewModelScope.launch {
            repository.updateUserCity(userId, city)
        }
    }

    fun refreshFuelPrices() {
        viewModelScope.launch {
            _fuelPriceState.value = FuelPriceUiState.Loading
            _fuelPriceState.value = repository.fetchLiveFuelPrices(selectedFuelCity.value)
        }
    }

    fun refreshVehicleCatalog() {
        viewModelScope.launch {
            _isSyncingCatalog.value = true
            _vehicleCatalog.value = repository.fetchLiveVehicleCatalog()
            _isSyncingCatalog.value = false
        }
    }

    fun onBrandSelected(brandName: String) {
        val key = brandName.trim().lowercase()
        if (key.isEmpty() || !enrichedBrands.add(key)) return
        viewModelScope.launch {
            _vehicleCatalog.value = repository.enrichBrandModelsFromNhtsa(brandName, _vehicleCatalog.value)
        }
    }

    fun loadEnginesForVehicle(brand: String, model: String, year: String) {
        engineFetchJob?.cancel()
        if (brand.isBlank() || model.isBlank()) {
            _vehicleEngineOptions.value = emptyList()
            _isLoadingEngines.value = false
            return
        }
        _vehicleEngineOptions.value = repository.getEnginesForVehicle(brand, model)
        engineFetchJob = viewModelScope.launch {
            _isLoadingEngines.value = true
            try {
                _vehicleEngineOptions.value = repository.fetchEnginesForVehicle(brand, model, year)
            } finally {
                _isLoadingEngines.value = false
            }
        }
    }
        
    fun addVehicle(brand: String, model: String, year: String, plate: String, fuelType: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400) // Smooth feedback
            val insertedId = repository.insertVehicle(
                Vehicle(userId = userId, brand = brand, model = model, year = year, plate = plate, fuelType = fuelType)
            ).toInt()
            if (insertedId > 0) {
                _selectedVehicleId.value = insertedId
            }
            _isLoading.value = false
            onSuccess()
        }
    }

    fun deleteVehicle(vehicleId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicleId)
            if (_selectedVehicleId.value == vehicleId) {
                _selectedVehicleId.value = null
            }
            onSuccess()
        }
    }

    fun addFuelRecord(
        vehicleId: Int,
        liters: Double,
        price: Double,
        km: Int,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(250)
            repository.insertFuelRecord(
                FuelRecord(
                    vehicleId = vehicleId,
                    liters = liters,
                    price = price,
                    total = liters * price,
                    km = km,
                    date = date
                )
            )
            _isLoading.value = false
            onSuccess()
        }
    }
}

class VehicleDetailViewModel(private val repository: AppRepository, private val vehicleId: Int) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val vehicle = repository.getVehicleById(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val maintenanceRecords = repository.getMaintenanceRecords(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val reminders = repository.getReminders(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelRecords = repository.getFuelRecords(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseRecords = repository.getExpenseRecords(vehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedFuelCity: StateFlow<FuelCityOption> = repository.selectedFuelCityFlow

    private val _fuelPriceState = MutableStateFlow<FuelPriceUiState>(FuelPriceUiState.Loading)
    val fuelPriceState: StateFlow<FuelPriceUiState> = _fuelPriceState

    init {
        viewModelScope.launch {
            repository.selectedFuelCityFlow.collectLatest { city ->
                _fuelPriceState.value = FuelPriceUiState.Loading
                _fuelPriceState.value = repository.fetchLiveFuelPrices(city)
            }
        }
    }

    fun selectFuelCity(city: FuelCityOption) {
        viewModelScope.launch {
            val ownerId = vehicle.value?.userId?.ifBlank { "default_user" } ?: "default_user"
            repository.updateUserCity(ownerId, city)
        }
    }

    fun refreshFuelPrices() {
        viewModelScope.launch {
            _fuelPriceState.value = FuelPriceUiState.Loading
            _fuelPriceState.value = repository.fetchLiveFuelPrices(selectedFuelCity.value)
        }
    }
        
    fun addMaintenance(type: String, description: String, km: Int, cost: Double, date: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            repository.insertMaintenanceRecord(
                MaintenanceRecord(vehicleId = vehicleId, type = type, description = description, km = km, cost = cost, date = date)
            )
            _isLoading.value = false
            onSuccess()
        }
    }

    fun deleteMaintenance(recordId: Int) {
        viewModelScope.launch {
            repository.deleteMaintenanceRecord(recordId)
        }
    }

    fun addFuelRecord(liters: Double, price: Double, km: Int, date: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            repository.insertFuelRecord(
                FuelRecord(vehicleId = vehicleId, liters = liters, price = price, total = liters * price, km = km, date = date)
            )
            _isLoading.value = false
            onSuccess()
        }
    }

    fun deleteFuelRecord(recordId: Int) {
        viewModelScope.launch {
            repository.deleteFuelRecord(recordId)
        }
    }

    fun addExpense(category: String, description: String, amount: Double, date: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            repository.insertExpenseRecord(
                ExpenseRecord(vehicleId = vehicleId, category = category, description = description, amount = amount, date = date)
            )
            _isLoading.value = false
            onSuccess()
        }
    }

    fun deleteExpense(recordId: Int) {
        viewModelScope.launch {
            repository.deleteExpenseRecord(recordId)
        }
    }
    
    fun addReminder(title: String, dueDate: Long, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(400)
            val id = repository.insertReminder(
                Reminder(vehicleId = vehicleId, title = title, dueDate = dueDate)
            )
            _isLoading.value = false
            onSuccess(id)
        }
    }

    fun deleteReminder(reminderId: Int) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
        }
    }
}

class ProfileViewModel(private val repository: AppRepository, private val userId: String) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val user: StateFlow<User?> = repository.getUserById(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vehicles: StateFlow<List<Vehicle>> = repository.getVehiclesByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenanceRecords: StateFlow<List<MaintenanceRecord>> = repository.getMaintenanceRecordsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseRecords: StateFlow<List<ExpenseRecord>> = repository.getExpenseRecordsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedFuelCity: StateFlow<FuelCityOption> = repository.selectedFuelCityFlow

    init {
        viewModelScope.launch {
            repository.seedInitialProfileAndRecords(userId = userId, forceProfileUpdate = false)
        }
        viewModelScope.launch {
            user.filterNotNull().collect { u ->
                if (u.cityId.isNotBlank()) {
                    repository.syncFuelCityFromUser(u.cityId)
                }
            }
        }
    }

    fun selectCity(city: FuelCityOption) {
        viewModelScope.launch {
            repository.updateUserCity(userId, city)
        }
    }

    fun updateGarageName(garageName: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(220)
            repository.updateUserGarageName(userId, garageName.trim())
            _isLoading.value = false
            onSuccess()
        }
    }

    fun updateProfile(
        garageName: String,
        name: String,
        surname: String,
        phone: String,
        email: String,
        city: FuelCityOption,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(300)
            repository.insertUser(
                User(
                    id = userId,
                    name = name.trim(),
                    surname = surname.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    garageName = garageName.trim(),
                    cityId = city.id
                )
            )
            _isLoading.value = false
            onSuccess()
        }
    }
}

class ViewModelFactory(private val repository: AppRepository, private val param: String = "", private val paramId: Int = 0) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) return AuthViewModel(repository) as T
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) return DashboardViewModel(repository, param) as T
        if (modelClass.isAssignableFrom(VehicleDetailViewModel::class.java)) return VehicleDetailViewModel(repository, paramId) as T
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) return ProfileViewModel(repository, param) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
