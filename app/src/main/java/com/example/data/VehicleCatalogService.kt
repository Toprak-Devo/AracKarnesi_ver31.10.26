package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.Collator
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VehicleBrandItem(
    val brand: String,
    val models: List<String>,
    val isPopularInTR: Boolean = false
)

data class VehicleCatalogData(
    val brands: List<VehicleBrandItem>,
    val sourceLabel: String,
    val isRemoteSynced: Boolean
) {
    fun modelsForBrand(brandName: String): List<String> {
        if (brandName.isBlank()) return emptyList()
        val trLocale = Locale.forLanguageTag("tr-TR")
        val match = brands.firstOrNull {
            it.brand.equals(brandName.trim(), ignoreCase = true) ||
                normalizeBrandKey(it.brand, trLocale) == normalizeBrandKey(brandName, trLocale)
        }
        return match?.models.orEmpty()
    }
}

private fun normalizeBrandKey(raw: String, locale: Locale): String {
    val cleaned = raw.trim()
        .lowercase(locale)
        .replace("š", "s")
        .replace("ş", "s")
        .replace("ë", "e")
        .replace("é", "e")
        .replace("ç", "c")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ı", "i")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
    return when (cleaned) {
        "mercedes", "mercedes benz" -> "mercedes-benz"
        "vw", "volkswagen" -> "volkswagen"
        "skoda" -> "skoda"
        "citroen" -> "citroen"
        "ds", "ds automobiles" -> "ds automobiles"
        "kgm", "ssangyong", "ssang yong" -> "ssangyong"
        "alfa", "alfa romeo" -> "alfa romeo"
        "landrover", "land rover" -> "land rover"
        "astonmartin", "aston martin" -> "aston martin"
        else -> cleaned
    }
}

class VehicleCatalogService(context: Context? = null) {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val trCollator: Collator = Collator.getInstance(trLocale).apply {
        strength = Collator.PRIMARY
    }

    private val prefs: SharedPreferences? =
        context?.applicationContext?.getSharedPreferences("vehicle_catalog_cache_v1", Context.MODE_PRIVATE)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    companion object {
        private const val REMOTE_CAR_LIST_URL =
            "https://raw.githubusercontent.com/matthlavacka/car-list/master/car-list.json"
        private const val NHTSA_MODELS_BY_MAKE_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/GetModelsForMake/"
        private const val CACHE_KEY_JSON = "cached_catalog_json"

        val popularTurkishBrandsOrder: List<String> = listOf(
            "Fiat",
            "Renault",
            "Volkswagen",
            "Ford",
            "Toyota",
            "Hyundai",
            "Peugeot",
            "Opel",
            "Honda",
            "Citroën",
            "Dacia",
            "Škoda",
            "BMW",
            "Mercedes-Benz",
            "Audi",
            "Togg",
            "Chery",
            "Kia",
            "Nissan",
            "Seat",
            "Volvo",
            "Tesla",
            "BYD",
            "Cupra",
            "MG"
        )

        // Comprehensive base catalog covering Turkey & Europe market models
        private val turkishMarketCatalog: Map<String, List<String>> = linkedMapOf(
            "Fiat" to listOf(
                "Egea Sedan", "Egea Cross", "Egea Hatchback", "Egea Lounge", "Linea",
                "Fiorino", "Doblo", "500", "500X", "500L", "500e", "600", "Tipo",
                "Panda", "Punto", "Grande Punto", "Bravo", "Albea", "Palio", "Siena",
                "Tempra", "Uno", "Marea", "Ducato", "Scudo", "Fullback", "Topolino"
            ),
            "Renault" to listOf(
                "Clio", "Megane", "Megane Sedan", "Megane E-Tech", "Taliant", "Austral",
                "Symbol", "Fluence", "Captur", "Kadjar", "Koleos", "Talisman", "Rafale",
                "Duster", "Scenic", "Grand Scenic", "Kangoo", "Express", "Zoe", "Twingo",
                "Laguna", "Latitude", "Modus", "R9 Broadway", "R19 Europa", "Trafic", "Master"
            ),
            "Volkswagen" to listOf(
                "Polo", "Golf", "Passat", "Passat Variant", "Tiguan", "T-Roc", "Taigo",
                "T-Cross", "Caddy", "Jetta", "Arteon", "CC", "Bora", "Scirocco", "Beetle",
                "Touareg", "Touran", "ID.3", "ID.4", "ID.5", "ID.7", "Transporter",
                "Caravelle", "Amarok", "Crafter"
            ),
            "Ford" to listOf(
                "Focus", "Fiesta", "Puma", "Kuga", "Tourneo Courier", "Transit Courier",
                "Tourneo Connect", "Mondeo", "EcoSport", "C-Max", "B-Max", "S-Max",
                "Galaxy", "Fusion", "Ka", "Mustang", "Mustang Mach-E", "Ranger",
                "Tourneo Custom", "Transit"
            ),
            "Toyota" to listOf(
                "Corolla", "Corolla Hybrid", "Corolla Cross", "Corolla Hatchback", "Yaris",
                "Yaris Cross", "C-HR", "RAV4", "Auris", "Avensis", "Camry", "Verso",
                "Prius", "bZ4X", "Hilux", "Land Cruiser", "Proace City"
            ),
            "Hyundai" to listOf(
                "i10", "i20", "i30", "Bayon", "Tucson", "Elantra", "Accent Blue",
                "Accent Era", "Accent", "Kona", "Santa Fe", "Ioniq 5", "Ioniq 6",
                "Getz", "ix35", "ix20", "Matrix", "Sonata", "Staria", "H-100"
            ),
            "Peugeot" to listOf(
                "208", "308", "408", "508", "2008", "3008", "5008", "301", "Rifter",
                "Partner", "Bipper", "206", "207", "307", "407", "107", "108",
                "e-208", "e-2008", "e-308", "Boxer", "Expert"
            ),
            "Opel" to listOf(
                "Corsa", "Corsa-e", "Astra", "Mokka", "Crossland", "Grandland",
                "Frontera", "Combo", "Insignia", "Vectra", "Meriva", "Zafira",
                "Adam", "Antara", "Zafira Life", "Vivaro"
            ),
            "Honda" to listOf(
                "Civic", "City", "HR-V", "CR-V", "ZR-V", "Jazz", "Accord", "e:Ny1", "CR-Z"
            ),
            "Citroën" to listOf(
                "C3", "C3 Aircross", "C4", "C4 X", "e-C4", "e-C4 X", "C5 Aircross",
                "C-Elysée", "Berlingo", "Nemo", "Ami", "C1", "C5", "C4 Cactus",
                "C4 Picasso", "Saxo", "Xsara", "Jumpy", "Jumper"
            ),
            "Dacia" to listOf(
                "Duster", "Sandero", "Sandero Stepway", "Jogger", "Spring",
                "Lodgy", "Logan", "Logan MCV", "Dokker", "Solenza"
            ),
            "Škoda" to listOf(
                "Octavia", "Superb", "Fabia", "Scala", "Kamiq", "Karoq", "Kodiaq",
                "Rapid", "Roomster", "Yeti", "Citigo", "Enyaq"
            ),
            "BMW" to listOf(
                "1 Serisi", "116i", "118i", "2 Serisi", "216d Gran Coupe", "3 Serisi",
                "320i", "320d", "4 Serisi", "420i Gran Coupe", "5 Serisi", "520i",
                "520d", "7 Serisi", "X1", "X2", "X3", "X4", "X5", "X6", "X7",
                "i4", "i5", "i7", "iX", "iX1", "iX3", "Z4"
            ),
            "Mercedes-Benz" to listOf(
                "A-Serisi", "A 180", "A 200", "B-Serisi", "C-Serisi", "C 200", "CLA",
                "CLS", "E-Serisi", "E 180", "E 200", "S-Serisi", "GLA", "GLB", "GLC",
                "GLE", "GLS", "G-Serisi", "EQA", "EQB", "EQE", "EQS", "Vito", "Sprinter"
            ),
            "Audi" to listOf(
                "A1", "A3", "A3 Sedan", "A3 Sportback", "A4", "A5", "A6", "A7", "A8",
                "Q2", "Q3", "Q4 e-tron", "Q5", "Q7", "Q8", "Q8 e-tron", "e-tron GT", "TT"
            ),
            "Togg" to listOf(
                "T10X V1 RWD Standart Menzil", "T10X V2 RWD Uzun Menzil", "T10X", "T10F"
            ),
            "Chery" to listOf(
                "Tiggo 7 Pro", "Tiggo 7 Pro Max", "Tiggo 8 Pro", "Tiggo 8 Pro Max",
                "Omoda 5", "Omoda 5 Pro", "Tiggo 4 Pro"
            ),
            "Kia" to listOf(
                "Picanto", "Rio", "Stonic", "Ceed", "XCeed", "Cerato", "Sportage",
                "Sorento", "Niro", "EV3", "EV6", "EV9", "Soul", "Venga", "Bongo"
            ),
            "Nissan" to listOf(
                "Qashqai", "Juke", "X-Trail", "Micra", "Note", "Pulsar", "Primera",
                "Almera", "Leaf", "Navara"
            ),
            "Seat" to listOf(
                "Ibiza", "Leon", "Arona", "Ateca", "Tarraco", "Toledo", "Altea", "Cordoba"
            ),
            "Volvo" to listOf(
                "XC40", "XC60", "XC90", "EX30", "EX40", "EC40", "S60", "S90",
                "V40", "V60", "V90 Cross Country", "S40", "S80"
            ),
            "Tesla" to listOf(
                "Model Y", "Model 3", "Model S", "Model X"
            ),
            "BYD" to listOf(
                "Atto 3", "Seal U DM-i", "Seal U EV", "Seal", "Dolphin", "Han", "Tang"
            ),
            "Cupra" to listOf(
                "Formentor", "Leon", "Ateca", "Born", "Terramar", "Tavascan"
            ),
            "MG" to listOf(
                "ZS", "ZS EV", "HS", "HS PHEV", "MG4", "Marvel R", "Cyberster"
            ),
            "Suzuki" to listOf(
                "Swift", "Vitara", "S-Cross", "Jimny", "Ignis", "Baleno", "SX4", "Grand Vitara"
            ),
            "Jeep" to listOf(
                "Renegade", "Compass", "Avenger", "Cherokee", "Grand Cherokee", "Wrangler"
            ),
            "Alfa Romeo" to listOf(
                "Tonale", "Junior", "Giulia", "Stelvio", "Giulietta", "159", "MiTo"
            ),
            "Mazda" to listOf(
                "Mazda3", "Mazda6", "Mazda2", "CX-3", "CX-30", "CX-5", "CX-60", "MX-5"
            ),
            "Mitsubishi" to listOf(
                "Space Star", "ASX", "Eclipse Cross", "L200", "Colt", "Lancer", "Outlander", "Pajero"
            ),
            "Mini" to listOf(
                "Cooper", "Cooper Countryman", "Clubman", "Cooper Electric", "Paceman"
            ),
            "Land Rover" to listOf(
                "Range Rover", "Range Rover Sport", "Range Rover Velar", "Range Rover Evoque",
                "Defender", "Discovery", "Discovery Sport", "Freelander"
            ),
            "Porsche" to listOf(
                "Taycan", "Macan", "Cayenne", "Panamera", "911", "718 Cayman", "718 Boxster"
            ),
            "Subaru" to listOf(
                "Forester", "Crosstrek", "XV", "Outback", "Solterra", "Impreza", "Levorg"
            ),
            "DS Automobiles" to listOf(
                "DS 4", "DS 7", "DS 3", "DS 9"
            ),
            "SsangYong" to listOf(
                "Torres", "Torres EVX", "Korando", "Tivoli", "Musso Grand", "Rexton", "Actyon Sports", "Kyron"
            ),
            "Chevrolet" to listOf(
                "Cruze", "Aveo", "Captiva", "Trax", "Spark", "Kalos", "Lacetti", "Epica"
            )
        )
    }

    fun getInitialCatalog(): VehicleCatalogData {
        val cachedMap = loadCachedMap()
        val merged = mergeBrandMaps(turkishMarketCatalog, cachedMap)
        val totalModels = merged.sumOf { it.models.size }
        val isFromRemoteCache = cachedMap.isNotEmpty()
        return VehicleCatalogData(
            brands = merged,
            sourceLabel = if (isFromRemoteCache) {
                "Güncel Araç Veri Tabanı • ${merged.size} Marka / $totalModels Model"
            } else {
                "Araç Veri Tabanı • ${merged.size} Marka / $totalModels Model"
            },
            isRemoteSynced = isFromRemoteCache
        )
    }

    suspend fun fetchLiveCatalog(): VehicleCatalogData = withContext(Dispatchers.IO) {
        val remoteMap = fetchFromRemoteCarListJson()
        if (remoteMap.isNotEmpty()) {
            saveCachedMap(remoteMap)
            val merged = mergeBrandMaps(turkishMarketCatalog, remoteMap)
            val totalModels = merged.sumOf { it.models.size }
            return@withContext VehicleCatalogData(
                brands = merged,
                sourceLabel = "Canlı Veri Tabanı • ${merged.size} Marka / $totalModels Model",
                isRemoteSynced = true
            )
        }

        val cachedMap = loadCachedMap()
        val merged = mergeBrandMaps(turkishMarketCatalog, cachedMap)
        val totalModels = merged.sumOf { it.models.size }
        VehicleCatalogData(
            brands = merged,
            sourceLabel = "Araç Veri Tabanı • ${merged.size} Marka / $totalModels Model",
            isRemoteSynced = cachedMap.isNotEmpty()
        )
    }

    suspend fun fetchModelsForBrandFromNhtsa(
        brandName: String,
        currentCatalog: VehicleCatalogData
    ): VehicleCatalogData = withContext(Dispatchers.IO) {
        val trimmed = brandName.trim()
        if (trimmed.isEmpty()) return@withContext currentCatalog

        val nhtsaModels = mutableListOf<String>()
        try {
            val encodedMake = Uri.encode(trimmed)
            val request = Request.Builder()
                .url("$NHTSA_MODELS_BY_MAKE_URL$encodedMake?format=json")
                .header("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.startsWith("{")) {
                        val root = JSONObject(body)
                        val results = root.optJSONArray("Results")
                        if (results != null) {
                            for (i in 0 until results.length()) {
                                val item = results.optJSONObject(i) ?: continue
                                val modelName = item.optString("Model_Name", "").trim()
                                if (modelName.isNotEmpty() && modelName.length <= 36) {
                                    nhtsaModels.add(modelName)
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Keep existing models if NHTSA endpoint is unreachable
        }

        if (nhtsaModels.isEmpty()) return@withContext currentCatalog

        val updatedBrands = currentCatalog.brands.map { brandItem ->
            if (normalizeBrandKey(brandItem.brand, trLocale) == normalizeBrandKey(trimmed, trLocale)) {
                val combined = deduplicateAndSortModels(brandItem.models, nhtsaModels)
                brandItem.copy(models = combined)
            } else {
                brandItem
            }
        }
        val totalModels = updatedBrands.sumOf { it.models.size }
        currentCatalog.copy(
            brands = updatedBrands,
            sourceLabel = "Canlı Veri Tabanı • ${updatedBrands.size} Marka / $totalModels Model",
            isRemoteSynced = true
        )
    }

    private fun fetchFromRemoteCarListJson(): Map<String, List<String>> {
        try {
            val request = Request.Builder()
                .url(REMOTE_CAR_LIST_URL)
                .header("Accept", "application/json")
                .header("User-Agent", "AracKarnesi-Android/1.0")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyMap()
                val body = response.body?.string()?.trim().orEmpty()
                if (!body.startsWith("[")) return emptyMap()

                val jsonArray = JSONArray(body)
                val result = LinkedHashMap<String, List<String>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val brand = obj.optString("brand", "").trim()
                    val modelsArr = obj.optJSONArray("models") ?: continue
                    if (brand.isEmpty()) continue

                    val models = mutableListOf<String>()
                    for (j in 0 until modelsArr.length()) {
                        val m = modelsArr.optString(j, "").trim()
                        if (m.isNotEmpty()) {
                            models.add(m)
                        }
                    }
                    if (models.isNotEmpty()) {
                        result[brand] = models
                    }
                }
                return result
            }
        } catch (_: Exception) {
            return emptyMap()
        }
    }

    private fun mergeBrandMaps(
        primaryTrMap: Map<String, List<String>>,
        remoteMap: Map<String, List<String>>
    ): List<VehicleBrandItem> {
        val canonicalDisplayNames = LinkedHashMap<String, String>()
        val modelsByNormalizedBrand = LinkedHashMap<String, MutableList<String>>()

        primaryTrMap.forEach { (brand, models) ->
            val key = normalizeBrandKey(brand, trLocale)
            canonicalDisplayNames[key] = brand
            modelsByNormalizedBrand.getOrPut(key) { mutableListOf() }.addAll(models)
        }

        remoteMap.forEach { (brand, models) ->
            val key = normalizeBrandKey(brand, trLocale)
            if (!canonicalDisplayNames.containsKey(key)) {
                canonicalDisplayNames[key] = brand
            }
            modelsByNormalizedBrand.getOrPut(key) { mutableListOf() }.addAll(models)
        }

        val popularNormalizedSet = popularTurkishBrandsOrder
            .map { normalizeBrandKey(it, trLocale) }
            .toSet()

        val items = modelsByNormalizedBrand.map { (normKey, rawModels) ->
            val displayBrand = canonicalDisplayNames[normKey] ?: normKey
            val trCuratedForBrand = primaryTrMap.entries
                .firstOrNull { normalizeBrandKey(it.key, trLocale) == normKey }
                ?.value
                .orEmpty()
            val cleanModels = deduplicateAndSortModels(trCuratedForBrand, rawModels)
            VehicleBrandItem(
                brand = displayBrand,
                models = cleanModels,
                isPopularInTR = normKey in popularNormalizedSet
            )
        }

        return items.sortedWith { a, b ->
            trCollator.compare(a.brand, b.brand)
        }
    }

    private fun deduplicateAndSortModels(
        priorityModels: List<String>,
        additionalModels: List<String>
    ): List<String> {
        val seenKeys = HashSet<String>()
        val priorityKept = mutableListOf<String>()
        val extraKept = mutableListOf<String>()

        for (m in priorityModels) {
            val trimmed = m.trim()
            if (trimmed.isEmpty()) continue
            val norm = trimmed.lowercase(trLocale)
                .replace("é", "e")
                .replace("ë", "e")
            if (seenKeys.add(norm)) {
                priorityKept.add(trimmed)
            }
        }

        for (m in additionalModels) {
            val trimmed = m.trim()
            if (trimmed.isEmpty()) continue
            val norm = trimmed.lowercase(trLocale)
                .replace("é", "e")
                .replace("ë", "e")
            if (seenKeys.add(norm)) {
                extraKept.add(trimmed)
            }
        }

        extraKept.sortWith { x, y -> trCollator.compare(x, y) }
        return priorityKept + extraKept
    }

    private fun saveCachedMap(map: Map<String, List<String>>) {
        try {
            val arr = JSONArray()
            map.forEach { (brand, models) ->
                val obj = JSONObject()
                obj.put("brand", brand)
                val mArr = JSONArray()
                models.forEach { mArr.put(it) }
                obj.put("models", mArr)
                arr.put(obj)
            }
            prefs?.edit()?.putString(CACHE_KEY_JSON, arr.toString())?.apply()
        } catch (_: Exception) {
            // Ignore cache write errors
        }
    }

    private fun loadCachedMap(): Map<String, List<String>> {
        val raw = prefs?.getString(CACHE_KEY_JSON, null) ?: return emptyMap()
        return try {
            val arr = JSONArray(raw)
            val map = LinkedHashMap<String, List<String>>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val brand = obj.optString("brand", "").trim()
                val mArr = obj.optJSONArray("models") ?: continue
                if (brand.isEmpty()) continue
                val list = mutableListOf<String>()
                for (j in 0 until mArr.length()) {
                    val m = mArr.optString(j, "").trim()
                    if (m.isNotEmpty()) list.add(m)
                }
                if (list.isNotEmpty()) {
                    map[brand] = list
                }
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun getEnginesForVehicle(brand: String, model: String): List<VehicleEngineOption> {
        return VehicleEngineCatalog.getEnginesForVehicle(brand, model, prefs)
    }

    suspend fun fetchEnginesForVehicle(
        brand: String,
        model: String,
        year: String
    ): List<VehicleEngineOption> {
        return VehicleEngineCatalog.fetchLiveEnginesForVehicle(
            brand = brand,
            model = model,
            year = year,
            httpClient = httpClient,
            prefs = prefs
        )
    }
}

