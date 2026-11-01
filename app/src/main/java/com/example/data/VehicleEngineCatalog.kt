package com.example.data

import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

data class VehicleEngineOption(
    val engineName: String,
    val fuelType: String,
    val specSummary: String = ""
)

/**
 * Provides vehicle-specific selectable Engine Options (Motor Seçenekleri) per Brand & Model,
 * combining a curated Turkish/European market vehicle engine database with live remote
 * vehicle engine specification fetching (CarQuery & EPA FuelEconomy APIs) and local caching.
 */
object VehicleEngineCatalog {

    private val trLocale: Locale = Locale.forLanguageTag("tr-TR")
    private const val ENGINE_CACHE_PREFIX = "vehicle_engines_v1_"
    private const val CARQUERY_TRIMS_URL = "https://www.carqueryapi.com/api/0.3/?cmd=getTrims"
    private const val EPA_OPTIONS_URL = "https://www.fueleconomy.gov/ws/rest/vehicle/menu/options"

    private fun opt(name: String, fuel: String, spec: String = ""): VehicleEngineOption =
        VehicleEngineOption(
            engineName = name,
            fuelType = fuel,
            specSummary = spec.ifBlank { "$fuel Motor Seçeneği" }
        )

    private fun norm(raw: String): String = raw.trim()
        .lowercase(trLocale)
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

    /**
     * Returns immediate, vehicle-specific engine options for the given [brand] and [model],
     * including cached remote options if previously fetched.
     */
    fun getEnginesForVehicle(
        brand: String,
        model: String,
        prefs: SharedPreferences? = null
    ): List<VehicleEngineOption> {
        if (brand.isBlank() || model.isBlank()) return emptyList()
        val curated = resolveCuratedEngines(brand, model)
        val cached = loadCachedEngines(brand, model, prefs)
        return mergeUniqueEngines(curated, cached)
    }

    /**
     * Fetches live engine options for the selected [brand], [model], and [year] from remote
     * vehicle specification endpoints, merges with curated market engines, and caches locally.
     */
    suspend fun fetchLiveEnginesForVehicle(
        brand: String,
        model: String,
        year: String,
        httpClient: OkHttpClient,
        prefs: SharedPreferences?
    ): List<VehicleEngineOption> = withContext(Dispatchers.IO) {
        val base = resolveCuratedEngines(brand, model)
        if (brand.isBlank() || model.isBlank()) return@withContext base

        val remote = mutableListOf<VehicleEngineOption>()
        remote.addAll(fetchFromCarQuery(brand, model, year, httpClient))
        if (remote.isEmpty()) {
            remote.addAll(fetchFromEpaMenu(brand, model, year, httpClient))
        }

        if (remote.isNotEmpty()) {
            saveCachedEngines(brand, model, remote, prefs)
            return@withContext mergeUniqueEngines(base, remote)
        }

        val cached = loadCachedEngines(brand, model, prefs)
        mergeUniqueEngines(base, cached)
    }

    private fun resolveCuratedEngines(brand: String, model: String): List<VehicleEngineOption> {
        val b = norm(brand)
        val m = norm(model)

        // 1. Dedicated Electric Vehicle Models Detection Across Brands
        if (isPureElectricModel(b, m)) {
            return electricEnginesFor(b, m)
        }

        // 2. Exact Brand + Model Family Engine Mapping
        when {
            b.contains("fiat") -> return when {
                m.contains("egea") || m.contains("tipo") -> listOf(
                    opt("1.6 MultiJet 130 HP", "Dizel", "1598 cc Turbo Dizel • 320 Nm"),
                    opt("1.3 MultiJet 95 HP", "Dizel", "1248 cc Turbo Dizel • 200 Nm"),
                    opt("1.4 Fire 95 HP", "Benzin", "1368 cc Atmosferik Benzin"),
                    opt("1.5 T4 Hibrit 130 HP", "Hibrit", "1469 cc 48V e-Hybrid • Otomatik"),
                    opt("1.0 FireFly Turbo 100 HP", "Benzin", "999 cc Turbo Benzin"),
                    opt("1.6 E-Torq 110 HP", "Benzin", "1598 cc Benzin • Otomatik")
                )
                m.contains("linea") || m.contains("punto") || m.contains("fiorino") || m.contains("doblo") -> listOf(
                    opt("1.3 MultiJet 95 HP", "Dizel", "1248 cc Turbo Dizel"),
                    opt("1.6 MultiJet 105 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("1.6 MultiJet 120 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("1.4 Fire 77 HP", "Benzin", "1368 cc Benzin"),
                    opt("1.4 Fire Eko 77 HP", "LPG", "1368 cc Fabrikasyon LPG")
                )
                m.contains("500") || m.contains("600") || m.contains("panda") -> listOf(
                    opt("1.0 Hybrid 70 HP", "Hibrit", "999 cc Mild Hybrid"),
                    opt("1.2 Hybrid 100 HP", "Hibrit", "1199 cc 48V Hybrid"),
                    opt("1.3 FireFly Turbo 150 HP", "Benzin", "1332 cc Turbo Benzin"),
                    opt("1.6 MultiJet 130 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("1.2 Fire 69 HP", "Benzin", "1242 cc Atmosferik Benzin")
                )
                m.contains("ducato") || m.contains("scudo") || m.contains("fullback") -> listOf(
                    opt("2.2 MultiJet3 140 HP", "Dizel", "2184 cc Ticari Turbo Dizel"),
                    opt("2.2 MultiJet3 180 HP", "Dizel", "2184 cc Ticari Turbo Dizel"),
                    opt("2.3 MultiJet 130 HP", "Dizel", "2287 cc Turbo Dizel"),
                    opt("2.0 MultiJet 145 HP", "Dizel", "1997 cc Turbo Dizel")
                )
                else -> listOf(
                    opt("1.4 Fire 95 HP", "Benzin", "Benzinli Motor"),
                    opt("1.3 MultiJet 95 HP", "Dizel", "Turbo Dizel Motor"),
                    opt("1.6 MultiJet 120 HP", "Dizel", "Turbo Dizel Motor"),
                    opt("1.6 16V 103 HP", "LPG", "Benzin / LPG Uyumlu Motor")
                )
            }

            b.contains("renault") -> return when {
                m.contains("clio") -> listOf(
                    opt("1.0 TCe 90 HP", "Benzin", "999 cc Turbo Benzin • X-Tronic/Manuel"),
                    opt("1.0 SCe 65 HP", "Benzin", "999 cc Atmosferik Benzin"),
                    opt("1.0 TCe ECO-G 100 HP", "LPG", "999 cc Turbo Fabrikasyon LPG"),
                    opt("1.6 E-Tech Full Hybrid 145 HP", "Hibrit", "Tam Hibrit Otomatik"),
                    opt("1.5 Blue dCi 100 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("1.5 dCi 90 HP", "Dizel", "1461 cc Turbo Dizel")
                )
                m.contains("megane") || m.contains("fluence") || m.contains("talisman") -> listOf(
                    opt("1.3 TCe 140 HP", "Benzin", "1332 cc Turbo Benzin • EDC"),
                    opt("1.5 Blue dCi 115 HP", "Dizel", "1461 cc Turbo Dizel • EDC"),
                    opt("1.5 dCi 110 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("1.6 dCi 130 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("1.6 16V 115 HP", "Benzin", "1598 cc Atmosferik Benzin"),
                    opt("1.2 TCe 130 HP", "Benzin", "1197 cc Turbo Benzin")
                )
                m.contains("austral") || m.contains("rafale") -> listOf(
                    opt("1.3 Mild Hybrid 160 HP", "Hibrit", "1332 cc 12V Mild Hybrid Otomatik"),
                    opt("1.2 E-Tech Full Hybrid 200 HP", "Hibrit", "1199 cc Tam Hibrit Multi-Mode")
                )
                m.contains("taliant") || m.contains("symbol") -> listOf(
                    opt("1.0 Turbo X-Tronic 90 HP", "Benzin", "999 cc Turbo Benzin"),
                    opt("1.0 SCe 65 HP", "Benzin", "999 cc Benzin"),
                    opt("1.0 Turbo ECO-G 100 HP", "LPG", "999 cc Turbo LPG"),
                    opt("1.5 dCi 90 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("0.9 TCe 90 HP", "Benzin", "898 cc Turbo Benzin")
                )
                m.contains("captur") || m.contains("kadjar") || m.contains("koleos") || m.contains("duster") -> listOf(
                    opt("1.3 Mild Hybrid 140 HP", "Hibrit", "1332 cc Mild Hybrid EDC"),
                    opt("1.3 TCe 140 HP", "Benzin", "1332 cc Turbo Benzin"),
                    opt("1.5 Blue dCi 115 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("1.6 E-Tech Hybrid 145 HP", "Hibrit", "Tam Hibrit Otomatik"),
                    opt("1.0 ECO-G 100 HP", "LPG", "Turbo Benzin / LPG")
                )
                m.contains("kangoo") || m.contains("express") || m.contains("trafic") || m.contains("master") -> listOf(
                    opt("1.5 Blue dCi 95 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("1.5 Blue dCi 115 HP", "Dizel", "1461 cc Turbo Dizel"),
                    opt("1.3 TCe 130 HP", "Benzin", "1332 cc Turbo Benzin"),
                    opt("2.0 Blue dCi 150 HP", "Dizel", "1997 cc Ticari Dizel"),
                    opt("2.3 dCi 165 HP", "Dizel", "2299 cc Ticari Dizel")
                )
                else -> listOf(
                    opt("1.3 TCe 140 HP", "Benzin", "Turbo Benzin"),
                    opt("1.5 Blue dCi 115 HP", "Dizel", "Turbo Dizel"),
                    opt("1.0 TCe 90 HP", "Benzin", "Turbo Benzin"),
                    opt("1.6 16V 110 HP", "LPG", "Atmosferik Benzin / LPG")
                )
            }

            b.contains("volkswagen") || b == "vw" -> return when {
                m.contains("polo") || m.contains("taigo") || m.contains("t cross") -> listOf(
                    opt("1.0 TSI 95 HP", "Benzin", "999 cc Turbo Benzin • DSG/Manuel"),
                    opt("1.0 TSI 110 HP", "Benzin", "999 cc Turbo Benzin • DSG"),
                    opt("1.0 MPI 80 HP", "Benzin", "999 cc Atmosferik Benzin"),
                    opt("1.5 TSI 150 HP", "Benzin", "1498 cc ACT Turbo Benzin"),
                    opt("1.6 TDI 95 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("1.4 TDI 90 HP", "Dizel", "1422 cc Turbo Dizel")
                )
                m.contains("golf") || m.contains("t roc") || m.contains("scirocco") || m.contains("bora") || m.contains("jetta") -> listOf(
                    opt("1.5 eTSI 150 HP", "Hibrit", "1498 cc 48V Mild Hybrid DSG"),
                    opt("1.0 eTSI 110 HP", "Hibrit", "999 cc 48V Mild Hybrid DSG"),
                    opt("1.5 TSI 150 HP", "Benzin", "1498 cc Turbo Benzin"),
                    opt("1.4 TSI 125 HP", "Benzin", "1395 cc Turbo Benzin"),
                    opt("1.6 TDI 115 HP", "Dizel", "1598 cc Turbo Dizel"),
                    opt("2.0 TDI 150 HP", "Dizel", "1968 cc Turbo Dizel")
                )
                m.contains("passat") || m.contains("tiguan") || m.contains("arteon") || m.contains("cc") || m.contains("touran") -> listOf(
                    opt("1.5 TSI 150 HP", "Benzin", "1498 cc ACT Turbo Benzin • DSG"),
                    opt("1.5 eTSI 150 HP", "Hibrit", "1498 cc 48V Mild Hybrid • DSG"),
                    opt("1.6 TDI 120 HP", "Dizel", "1598 cc Turbo Dizel • DSG"),
                    opt("2.0 TDI 150 HP", "Dizel", "1968 cc Turbo Dizel • DSG"),
                    opt("2.0 TDI 200 HP 4Motion", "Dizel", "1968 cc Bi-Turbo Dizel AWD"),
                    opt("1.4 TSI 150 HP", "Benzin", "1395 cc ACT Turbo Benzin")
                )
                m.contains("caddy") || m.contains("transporter") || m.contains("caravelle") || m.contains("amarok") || m.contains("crafter") || m.contains("touareg") -> listOf(
                    opt("2.0 TDI 122 HP", "Dizel", "1968 cc Turbo Dizel"),
                    opt("2.0 TDI 150 HP", "Dizel", "1968 cc Turbo Dizel"),
                    opt("2.0 BiTDI 205 HP", "Dizel", "1968 cc Çift Turbo Dizel"),
                    opt("3.0 V6 TDI 240 HP", "Dizel", "2967 cc V6 Turbo Dizel"),
                    opt("3.0 V6 TDI 286 HP", "Dizel", "2967 cc V6 Turbo Dizel 4Motion")
                )
                else -> listOf(
                    opt("1.5 TSI 150 HP", "Benzin", "Turbo Benzin"),
                    opt("1.0 TSI 110 HP", "Benzin", "Turbo Benzin"),
                    opt("1.6 TDI 120 HP", "Dizel", "Turbo Dizel"),
                    opt("2.0 TDI 150 HP", "Dizel", "Turbo Dizel")
                )
            }

            b.contains("ford") -> return when {
                m.contains("focus") || m.contains("fiesta") || m.contains("puma") || m.contains("ecosport") -> listOf(
                    opt("1.0 EcoBoost Hybrid 125 HP", "Hibrit", "999 cc mHEV Turbo • Otomatik"),
                    opt("1.0 EcoBoost Hybrid 155 HP", "Hibrit", "999 cc mHEV Turbo"),
                    opt("1.0 EcoBoost 125 HP", "Benzin", "999 cc Turbo Benzin"),
                    opt("1.5 EcoBlue 115 HP", "Dizel", "1499 cc Turbo Dizel • 8 İleri Otomatik"),
                    opt("1.5 TDCi 120 HP", "Dizel", "1499 cc Turbo Dizel"),
                    opt("1.5 Ti-VCT 123 HP", "Benzin", "1498 cc Atmosferik Benzin")
                )
                m.contains("kuga") || m.contains("mondeo") || m.contains("c max") || m.contains("s max") -> listOf(
                    opt("1.5 EcoBoost 186 HP", "Benzin", "1497 cc Turbo Benzin"),
                    opt("1.5 EcoBoost 150 HP", "Benzin", "1497 cc Turbo Benzin"),
                    opt("1.5 EcoBlue 120 HP", "Dizel", "1499 cc Turbo Dizel"),
                    opt("2.5 Duratec Hibrit 180 HP", "Hibrit", "2488 cc Tam Hibrit FHEV"),
                    opt("2.0 EcoBlue 190 HP", "Dizel", "1995 cc Turbo Dizel AWD")
                )
                else -> listOf(
                    opt("1.5 EcoBlue 100 HP", "Dizel", "1499 cc Turbo Dizel"),
                    opt("1.0 EcoBoost 125 HP", "Benzin", "999 cc Turbo Benzin"),
                    opt("2.0 EcoBlue 130 HP", "Dizel", "1995 cc Ticari Turbo Dizel"),
                    opt("2.0 EcoBlue 170 HP", "Dizel", "1995 cc Turbo Dizel"),
                    opt("2.0 EcoBlue Bi-Turbo 205 HP", "Dizel", "1996 cc Çift Turbo Dizel")
                )
            }

            b.contains("toyota") -> return when {
                m.contains("corolla") || m.contains("auris") || m.contains("avensis") || m.contains("verso") -> listOf(
                    opt("1.8 Hybrid 140 HP", "Hibrit", "1798 cc e-CVT Tam Hibrit"),
                    opt("1.5 Dynamic Force 125 HP", "Benzin", "1490 cc Multidrive S Benzin"),
                    opt("1.6 Valvematic 132 HP", "Benzin", "1598 cc Atmosferik Benzin"),
                    opt("1.4 D-4D 90 HP", "Dizel", "1364 cc Turbo Dizel"),
                    opt("1.6 Valvematic LPG 132 HP", "LPG", "1598 cc Benzin / LPG")
                )
                m.contains("yaris") -> listOf(
                    opt("1.5 Hybrid 116 HP", "Hibrit", "1490 cc e-CVT Tam Hibrit"),
                    opt("1.5 Hybrid 130 HP", "Hibrit", "1490 cc 5. Nesil Hibrit"),
                    opt("1.5 Dynamic Force 125 HP", "Benzin", "1490 cc Benzin"),
                    opt("1.0 VVT-i 72 HP", "Benzin", "998 cc Benzin")
                )
                m.contains("c hr") || m.contains("rav4") || m.contains("camry") || m.contains("prius") -> listOf(
                    opt("1.8 Hybrid 140 HP", "Hibrit", "1798 cc e-CVT Hibrit"),
                    opt("2.0 Hybrid 197 HP", "Hibrit", "1987 cc e-CVT Hibrit"),
                    opt("2.5 Hybrid 218 HP", "Hibrit", "2487 cc e-CVT Tam Hibrit"),
                    opt("1.2 Turbo 116 HP", "Benzin", "1197 cc Turbo Benzin")
                )
                else -> listOf(
                    opt("2.4 D-4D 150 HP", "Dizel", "2393 cc Turbo Dizel"),
                    opt("2.8 D-4D 204 HP", "Dizel", "2755 cc 4x4 Turbo Dizel"),
                    opt("1.5 D-4D 130 HP", "Dizel", "1499 cc Turbo Dizel"),
                    opt("1.8 Hybrid 140 HP", "Hibrit", "e-CVT Tam Hibrit")
                )
            }

            b.contains("hyundai") -> return when {
                m.contains("i10") -> listOf(
                    opt("1.0 MPi 67 HP", "Benzin", "998 cc Atmosferik Benzin"),
                    opt("1.2 MPi 84 HP", "Benzin", "1197 cc Atmosferik Benzin")
                )
                m.contains("i20") || m.contains("bayon") || m.contains("getz") -> listOf(
                    opt("1.4 MPi 100 HP", "Benzin", "1368 cc Otomatik/Manuel Benzin"),
                    opt("1.0 T-GDI 100 HP", "Benzin", "998 cc Turbo DCT Benzin"),
                    opt("1.0 T-GDI 48V MHEV 100 HP", "Hibrit", "998 cc Mild Hybrid DCT"),
                    opt("1.2 MPi 84 HP", "Benzin", "1197 cc Benzin"),
                    opt("1.4 CRDi 90 HP", "Dizel", "1396 cc Turbo Dizel")
                )
                m.contains("tucson") || m.contains("kona") || m.contains("santa fe") || m.contains("ix35") -> listOf(
                    opt("1.6 T-GDI 160 HP", "Benzin", "1598 cc Turbo DCT Benzin"),
                    opt("1.6 T-GDI 180 HP", "Benzin", "1598 cc Turbo DCT 4x2/4x4"),
                    opt("1.6 CRDi 136 HP", "Dizel", "1598 cc 48V Turbo Dizel DCT"),
                    opt("1.6 T-GDI Hybrid 215 HP", "Hibrit", "1598 cc Tam Hibrit Otomatik"),
                    opt("1.0 T-GDI 120 HP", "Benzin", "998 cc Turbo Benzin")
                )
                else -> listOf(
                    opt("1.6 MPi 123 HP", "Benzin", "1591 cc Atmosferik Benzin"),
                    opt("1.6 CRDi 136 HP", "Dizel", "1582 cc Turbo Dizel DCT"),
                    opt("1.5 CRDi VGT 110 HP", "Dizel", "1493 cc Turbo Dizel"),
                    opt("2.2 CRDi 177 HP", "Dizel", "2199 cc Turbo Dizel")
                )
            }

            b.contains("peugeot") || b.contains("citroen") || b.contains("opel") || b.contains("ds") -> return when {
                m.contains("rifter") || m.contains("partner") || m.contains("berlingo") || m.contains("combo") ||
                    m.contains("boxer") || m.contains("expert") || m.contains("vivaro") || m.contains("jumper") || m.contains("jumpy") -> listOf(
                    opt("1.5 BlueHDi / Dizel 130 HP", "Dizel", "1499 cc EAT8 / Manuel Turbo Dizel"),
                    opt("1.5 BlueHDi / Dizel 100 HP", "Dizel", "1499 cc Turbo Dizel"),
                    opt("2.0 BlueHDi 145 HP", "Dizel", "1997 cc Ticari Turbo Dizel"),
                    opt("2.2 BlueHDi 165 HP", "Dizel", "2179 cc Ticari Turbo Dizel")
                )
                else -> listOf(
                    opt("1.2 PureTech / Turbo 130 HP", "Benzin", "1199 cc EAT8 Turbo Benzin"),
                    opt("1.2 Hybrid 136 HP e-DCS6", "Hibrit", "1199 cc 48V Yeni Nesil Hibrit"),
                    opt("1.5 BlueHDi / Dizel 130 HP", "Dizel", "1499 cc EAT8 Turbo Dizel"),
                    opt("1.2 PureTech / Benzin 100 HP", "Benzin", "1199 cc Turbo Benzin"),
                    opt("1.6 PureTech / PHEV 180 HP", "Hibrit", "1598 cc Şarj Edilebilir Hibrit"),
                    opt("1.6 HDi / CDTI 115 HP", "Dizel", "1560 cc Turbo Dizel")
                )
            }

            b.contains("honda") -> return when {
                m.contains("civic") -> listOf(
                    opt("1.5 VTEC Turbo 182 HP", "Benzin", "1498 cc Turbo CVT Benzin"),
                    opt("1.5 VTEC Turbo Eco 129 HP", "LPG", "1498 cc Fabrikasyon LPG • CVT"),
                    opt("1.6 i-VTEC Eco 125 HP", "LPG", "1597 cc Fabrikasyon LPG"),
                    opt("1.6 i-VTEC 125 HP", "Benzin", "1597 cc Atmosferik Benzin"),
                    opt("1.6 i-DTEC 120 HP", "Dizel", "1597 cc Turbo Dizel"),
                    opt("2.0 e:HEV Hybrid 184 HP", "Hibrit", "1993 cc Tam Hibrit")
                )
                m.contains("city") || m.contains("jazz") -> listOf(
                    opt("1.5 i-VTEC 121 HP", "Benzin", "1498 cc CVT Benzin"),
                    opt("1.5 e:HEV Hybrid 122 HP", "Hibrit", "1498 cc Tam Hibrit"),
                    opt("1.3 i-VTEC 102 HP", "Benzin", "1318 cc Benzin")
                )
                else -> listOf(
                    opt("1.5 e:HEV Hybrid 131 HP", "Hibrit", "1498 cc Tam Hibrit Otomatik"),
                    opt("2.0 e:HEV Hybrid 184 HP", "Hibrit", "1993 cc Tam Hibrit AWD"),
                    opt("1.5 VTEC Turbo 193 HP", "Benzin", "1498 cc Turbo Benzin")
                )
            }

            b.contains("dacia") -> return listOf(
                opt("1.0 ECO-G 100 HP", "LPG", "999 cc Fabrikasyon Turbo LPG"),
                opt("1.3 TCe 130 HP", "Benzin", "1332 cc Turbo Benzin"),
                opt("1.3 TCe 150 HP EDC / 4x4", "Benzin", "1332 cc Turbo Benzin"),
                opt("1.5 Blue dCi 115 HP", "Dizel", "1461 cc Turbo Dizel 4x2/4x4"),
                opt("1.0 TCe 90 HP", "Benzin", "999 cc Turbo Benzin • X-Tronic"),
                opt("1.6 Hybrid 140 HP", "Hibrit", "1598 cc Tam Hibrit Otomatik")
            )

            b.contains("skoda") || b.contains("seat") || b.contains("cupra") -> return listOf(
                opt("1.5 TSI e-TEC / mHEV 150 HP", "Hibrit", "1498 cc 48V Mild Hybrid DSG"),
                opt("1.5 TSI 150 HP", "Benzin", "1498 cc ACT Turbo Benzin DSG"),
                opt("1.0 TSI e-TEC 110 HP", "Hibrit", "999 cc 48V Mild Hybrid DSG"),
                opt("1.0 TSI 115 HP", "Benzin", "999 cc Turbo Benzin"),
                opt("1.6 TDI 115 HP", "Dizel", "1598 cc Turbo Dizel DSG"),
                opt("2.0 TDI 150 HP", "Dizel", "1968 cc Turbo Dizel DSG"),
                opt("2.0 TSI VZ 310 HP 4Drive", "Benzin", "1984 cc Performans Turbo")
            )

            b.contains("bmw") -> return listOf(
                opt("1.6 Turbo 170 HP (20i)", "Benzin", "1597 cc TwinPower Turbo Otomatik"),
                opt("1.5 Turbo 136 HP (18i)", "Benzin", "1499 cc TwinPower Turbo Otomatik"),
                opt("1.5 Mild-Hybrid 170 HP (20)", "Hibrit", "1499 cc 48V Mild Hybrid"),
                opt("1.6 Mild-Hybrid 190 HP (520i)", "Hibrit", "1597 cc 48V Mild Hybrid"),
                opt("2.0 Dizel 190 HP (20d)", "Dizel", "1995 cc TwinPower Turbo Dizel"),
                opt("1.5 Dizel 116 HP (16d)", "Dizel", "1496 cc Turbo Dizel")
            )

            b.contains("mercedes") -> return when {
                m.contains("vito") || m.contains("sprinter") -> listOf(
                    opt("2.0 CDI 163 HP (116 CDI)", "Dizel", "1950 cc Ticari Turbo Dizel"),
                    opt("2.0 CDI 190 HP (119 CDI)", "Dizel", "1950 cc 9G-TRONIC Dizel"),
                    opt("2.1 CDI 136 HP (114 CDI)", "Dizel", "2143 cc Turbo Dizel")
                )
                else -> listOf(
                    opt("1.33 Turbo 163 HP (200)", "Benzin", "1332 cc Turbo 7G-DCT"),
                    opt("1.5 Mild-Hybrid 204 HP (200 4MATIC)", "Hibrit", "1496 cc 48V EQ Boost 9G-TRONIC"),
                    opt("1.5 Turbo 170 HP (E 180)", "Benzin", "1496 cc 9G-TRONIC Turbo Benzin"),
                    opt("1.5 d 116 HP (180d)", "Dizel", "1461 cc Turbo Dizel"),
                    opt("2.0 d 197 HP (220d)", "Dizel", "1993 cc Turbo Dizel 9G-TRONIC")
                )
            }

            b.contains("audi") -> return listOf(
                opt("35 TFSI 1.5 150 HP Mild-Hybrid", "Hibrit", "1498 cc 48V S tronic"),
                opt("30 TFSI 1.0 110 HP", "Benzin", "999 cc Turbo S tronic"),
                opt("45 TFSI 2.0 265 HP Quattro", "Benzin", "1984 cc Turbo AWD"),
                opt("35 TDI 2.0 150 HP", "Dizel", "1968 cc Turbo Dizel S tronic"),
                opt("40 TDI 2.0 204 HP Quattro", "Dizel", "1968 cc Turbo Dizel AWD"),
                opt("30 TDI 1.6 116 HP", "Dizel", "1598 cc Turbo Dizel")
            )

            b.contains("chery") -> return listOf(
                opt("1.6 TGDI 145 HP Acteco", "Benzin", "1598 cc Turbo 7-DCT"),
                opt("1.6 TGDI 183 HP Acteco", "Benzin", "1598 cc Turbo 7-DCT"),
                opt("1.5 TCI 147 HP", "Benzin", "1498 cc Turbo Benzin")
            )

            b.contains("kia") -> return listOf(
                opt("1.6 T-GDI MHEV 150 HP", "Hibrit", "1598 cc 48V Mild Hybrid DCT"),
                opt("1.6 T-GDI 180 HP", "Benzin", "1598 cc Turbo DCT"),
                opt("1.6 CRDi MHEV 136 HP", "Dizel", "1598 cc Turbo Dizel DCT"),
                opt("1.0 T-GDI 100 HP", "Benzin", "998 cc Turbo Benzin"),
                opt("1.4 MPi 100 HP", "Benzin", "1368 cc Atmosferik Benzin"),
                opt("1.25 MPi 84 HP", "Benzin", "1248 cc Benzin")
            )

            b.contains("nissan") -> return listOf(
                opt("1.3 DIG-T Mild Hybrid 158 HP", "Hibrit", "1332 cc X-Tronic Mild Hybrid"),
                opt("1.5 e-POWER 190 HP", "Hibrit", "1497 cc Seri Hibrit"),
                opt("1.0 DIG-T 114 HP", "Benzin", "999 cc Turbo DCT"),
                opt("1.5 dCi 115 HP", "Dizel", "1461 cc Turbo Dizel"),
                opt("1.6 dCi 130 HP", "Dizel", "1598 cc Turbo Dizel")
            )

            b.contains("volvo") -> return listOf(
                opt("2.0 B4 Mild-Hybrid 197 HP", "Hibrit", "1969 cc 48V Benzin/Dizel Mild Hybrid"),
                opt("2.0 B5 AWD Mild-Hybrid 250 HP", "Hibrit", "1969 cc AWD Mild Hybrid"),
                opt("1.5 T3 Turbo 163 HP", "Benzin", "1477 cc Geartronic Turbo Benzin"),
                opt("1.5 T5 Recharge PHEV 262 HP", "Hibrit", "1477 cc Plug-in Hybrid"),
                opt("2.0 D4 190 HP", "Dizel", "1969 cc Twin-Turbo Dizel")
            )

            b.contains("byd") -> return if (m.contains("dm i")) {
                listOf(
                    opt("1.5 Xiaoyun DM-i PHEV 218 HP", "Hibrit", "1498 cc Şarj Edilebilir Hibrit"),
                    opt("1.5 Turbo DM-i AWD 324 HP", "Hibrit", "1498 cc Çift Motorlu Hibrit AWD")
                )
            } else {
                electricEnginesFor(b, m)
            }

            b.contains("mg") -> return if (m.contains("hs") || (m.contains("zs") && !m.contains("ev"))) {
                listOf(
                    opt("1.5 T-GDI 162 HP", "Benzin", "1490 cc Turbo Otomatik"),
                    opt("1.5 T-GDI PHEV 258 HP", "Hibrit", "1490 cc Şarj Edilebilir Hibrit"),
                    opt("1.0 T-GDI 111 HP", "Benzin", "999 cc Turbo Otomatik"),
                    opt("1.5 VTI-Tech 106 HP", "Benzin", "1498 cc Atmosferik Benzin")
                )
            } else {
                electricEnginesFor(b, m)
            }

            b.contains("suzuki") -> return listOf(
                opt("1.4 Boosterjet 48V Hybrid 129 HP", "Hibrit", "1373 cc AllGrip / 4x2 Mild Hybrid"),
                opt("1.2 Dualjet Hybrid 83 HP", "Hibrit", "1197 cc Mild Hybrid CVT"),
                opt("1.5 Dualjet AllGrip 102 HP", "Benzin", "1462 cc 4x4 Benzin"),
                opt("1.6 VVT 120 HP", "Benzin", "1586 cc Atmosferik Benzin")
            )

            b.contains("jeep") || b.contains("alfa romeo") -> return listOf(
                opt("1.5 T4 e-Hybrid 130 HP", "Hibrit", "1469 cc 48V Hibrit DCT"),
                opt("1.2 e-Hybrid 100 HP", "Hibrit", "1199 cc 48V Hibrit"),
                opt("1.6 MultiJet 130 HP", "Dizel", "1598 cc Turbo Dizel DDCT"),
                opt("1.3 GSE T4 Turbo 150 HP", "Benzin", "1332 cc Turbo Benzin"),
                opt("2.0 Turbo 280 HP Q4", "Benzin", "1995 cc AWD Turbo Benzin")
            )

            b.contains("mazda") -> return listOf(
                opt("2.0 Skyactiv-G 150 HP", "Benzin", "1998 cc Atmosferik Benzin"),
                opt("1.5 Skyactiv-G 120 HP", "Benzin", "1496 cc Atmosferik Benzin"),
                opt("1.5 Skyactiv-D 105 HP", "Dizel", "1499 cc Turbo Dizel"),
                opt("2.5 e-Skyactiv PHEV 327 HP", "Hibrit", "2488 cc Plug-in Hybrid AWD")
            )

            b.contains("mitsubishi") -> return listOf(
                opt("1.2 MIVEC 71 HP", "Benzin", "1193 cc CVT Ekonomik Benzin"),
                opt("1.5 MIVEC Turbo 163 HP", "Benzin", "1499 cc Turbo CVT"),
                opt("2.2 DI-D 150 HP 4x4", "Dizel", "2268 cc Pick-up / SUV Turbo Dizel"),
                opt("1.6 MIVEC 117 HP", "Benzin", "1590 cc Benzin")
            )

            b.contains("mini") -> return listOf(
                opt("1.5 TwinPower Turbo 136 HP", "Benzin", "1499 cc Cooper Otomatik"),
                opt("1.5 Mild-Hybrid 170 HP", "Hibrit", "1499 cc Cooper C 48V"),
                opt("2.0 TwinPower Turbo 204 HP", "Benzin", "1998 cc Cooper S"),
                opt("1.5 Cooper D 116 HP", "Dizel", "1496 cc Turbo Dizel")
            )

            b.contains("land rover") -> return listOf(
                opt("2.0 D200 Mild-Hybrid 204 HP", "Dizel", "1997 cc AWD Ingenium Dizel"),
                opt("3.0 D300 Mild-Hybrid 300 HP", "Dizel", "2997 cc AWD 6 Silindir Dizel"),
                opt("1.5 P300e PHEV 309 HP", "Hibrit", "1498 cc Şarj Edilebilir Hibrit AWD"),
                opt("2.0 Si4 Turbo 240 HP", "Benzin", "1997 cc AWD Turbo Benzin")
            )

            b.contains("porsche") -> return listOf(
                opt("2.0 Turbo 265 HP PDK", "Benzin", "1984 cc AWD Turbo Benzin"),
                opt("3.0 V6 E-Hybrid 470 HP", "Hibrit", "2995 cc Plug-in Hybrid AWD"),
                opt("3.0 Twin-Turbo Boxer 385 HP", "Benzin", "2981 cc PDK Performans")
            )

            b.contains("subaru") -> return listOf(
                opt("2.0i e-Boxer Hybrid 150 HP", "Hibrit", "1995 cc Symmetrical AWD Lineartronic"),
                opt("1.6i Boxer 114 HP", "Benzin", "1600 cc Symmetrical AWD CVT"),
                opt("2.5i Boxer 169 HP", "Benzin", "2498 cc Symmetrical AWD")
            )

            b.contains("ssangyong") || b.contains("kgm") -> return listOf(
                opt("1.5 GDI-Turbo 163 HP", "Benzin", "1497 cc Aisin Otomatik"),
                opt("2.2 e-XDi 202 HP 4x4", "Dizel", "2157 cc Turbo Dizel Otomatik"),
                opt("1.6 e-XDi 136 HP", "Dizel", "1597 cc Turbo Dizel")
            )

            b.contains("chevrolet") -> return listOf(
                opt("1.6 Ecotec 124 HP", "Benzin", "1598 cc Atmosferik Benzin"),
                opt("1.6 Ecotec LPG 124 HP", "LPG", "1598 cc Benzin / LPG"),
                opt("1.4 Turbo 140 HP", "Benzin", "1364 cc Turbo Otomatik"),
                opt("2.0 VCDi 163 HP", "Dizel", "1998 cc Turbo Dizel"),
                opt("1.3 VCDi 95 HP", "Dizel", "1248 cc Turbo Dizel")
            )

            else -> return listOf(
                opt("1.5 Turbo 150 HP", "Benzin", "Turbo Benzinli Motor"),
                opt("1.6 Dizel 130 HP", "Dizel", "Turbo Dizel Motor"),
                opt("1.5 Mild-Hybrid 140 HP", "Hibrit", "Hibrit Motor"),
                opt("1.4 Atmosferik 100 HP", "LPG", "Benzin / LPG Motor"),
                opt("150 kW (204 HP) Elektrik", "Elektrik", "Tam Elektrikli Motor")
            )
        }
    }

    private fun isPureElectricModel(normBrand: String, normModel: String): Boolean {
        if (normBrand in setOf("togg", "tesla")) return true
        if (normBrand == "byd" && !normModel.contains("dm i")) return true
        val evExactTokens = listOf(
            "500e", "topolino", "megane e tech", "zoe", "twingo e",
            "id.3", "id.4", "id.5", "id.7", "mach e", "bz4x",
            "ioniq 5", "ioniq 6", "e 208", "e 2008", "e 308",
            "corsa e", "e c4", "ami", "e:ny1", "spring", "enyaq",
            "i4", "i5", "i7", "ix", "ix1", "ix3",
            "eqa", "eqb", "eqe", "eqs", "e tron",
            "ev3", "ev6", "ev9", "leaf", "ex30", "ex40", "ec40",
            "born", "tavascan", "zs ev", "mg4", "marvel r", "cyberster",
            "cooper electric", "taycan", "solterra", "torres evx",
            "atto 3", "seal u ev", "dolphin"
        )
        return evExactTokens.any { normModel.contains(it) }
    }

    private fun electricEnginesFor(normBrand: String, normModel: String): List<VehicleEngineOption> {
        return when {
            normBrand == "togg" -> listOf(
                opt("160 kW (218 HP) RWD Tek Motor", "Elektrik", "Arkadan İtiş • 52.4 / 88.5 kWh Batarya"),
                opt("320 kW (435 HP) AWD Çift Motor", "Elektrik", "Dört Çeker Çift Elektrik Motoru")
            )
            normBrand == "tesla" -> listOf(
                opt("159 kW (216 HP) RWD Standart Menzil", "Elektrik", "Arkadan İtiş LFP Batarya"),
                opt("220 kW (299 HP) RWD", "Elektrik", "Arkadan İtiş Elektrik Motoru"),
                opt("378 kW (514 HP) Long Range AWD", "Elektrik", "Çift Motor Dört Çeker"),
                opt("393 kW (534 HP) Performance AWD", "Elektrik", "Yüksek Performans Çift Motor")
            )
            normBrand == "byd" -> listOf(
                opt("150 kW (204 HP) Blade EV", "Elektrik", "Önden Çekiş Elektrik Motoru"),
                opt("160 kW (218 HP) Comfort/Design EV", "Elektrik", "Blade Batarya Elektrik"),
                opt("230 kW (313 HP) RWD EV", "Elektrik", "Arkadan İtiş Elektrik"),
                opt("390 kW (530 HP) AWD EV", "Elektrik", "Çift Motor AWD Elektrik")
            )
            normModel.contains("ami") || normModel.contains("topolino") || normModel.contains("spring") -> listOf(
                opt("48 kW (65 HP) Elektrik", "Elektrik", "Şehir İçi Elektrik Motoru"),
                opt("33 kW (45 HP) Elektrik", "Elektrik", "Kompakt Elektrik Motoru"),
                opt("6 kW Mikro Elektrik", "Elektrik", "Şehir İçi Mikro Mobilite")
            )
            else -> listOf(
                opt("100 kW (136 HP) Elektrik", "Elektrik", "Tek Motor Elektrik"),
                opt("150 kW (204 HP) Elektrik", "Elektrik", "Uzun Menzil Elektrik Motoru"),
                opt("210 kW (286 HP) Elektrik", "Elektrik", "Yüksek Güçlü Elektrik Motoru"),
                opt("300 kW (408 HP) AWD Elektrik", "Elektrik", "Çift Motor AWD Elektrik")
            )
        }
    }

    private fun fetchFromCarQuery(
        brand: String,
        model: String,
        year: String,
        httpClient: OkHttpClient
    ): List<VehicleEngineOption> {
        return try {
            val cleanMake = Uri.encode(brand.trim().lowercase(Locale.US))
            val cleanModel = Uri.encode(model.trim().split(" ").firstOrNull().orEmpty())
            val yearParam = year.trim().toIntOrNull()?.takeIf { it in 1990..2026 }?.let { "&year=$it" }.orEmpty()
            val url = "$CARQUERY_TRIMS_URL&make=$cleanMake&model=$cleanModel$yearParam"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "AracKarnesi-Android/1.0")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val rawBody = response.body?.string()?.trim().orEmpty()
                val jsonStart = rawBody.indexOf('{')
                val jsonEnd = rawBody.lastIndexOf('}')
                if (jsonStart < 0 || jsonEnd <= jsonStart) return emptyList()

                val root = JSONObject(rawBody.substring(jsonStart, jsonEnd + 1))
                val trims = root.optJSONArray("Trims") ?: return emptyList()
                val parsed = mutableListOf<VehicleEngineOption>()

                for (i in 0 until trims.length().coerceAtMost(40)) {
                    val item = trims.optJSONObject(i) ?: continue
                    val cc = item.optString("model_engine_cc", "").toIntOrNull()
                    val powerPs = item.optString("model_engine_power_ps", "").toIntOrNull()
                    val rawFuel = item.optString("model_engine_fuel", "").lowercase(Locale.US)
                    val engineType = item.optString("model_engine_type", "")

                    val mappedFuel = when {
                        rawFuel.contains("electric") -> "Elektrik"
                        rawFuel.contains("hybrid") -> "Hibrit"
                        rawFuel.contains("diesel") -> "Dizel"
                        rawFuel.contains("lpg") -> "LPG"
                        else -> "Benzin"
                    }

                    if (cc != null && cc in 600..6500) {
                        val litersStr = String.format(Locale.US, "%.1f", cc / 1000.0)
                        val turboTag = if (engineType.contains("turbo", ignoreCase = true)) " Turbo" else ""
                        val hpTag = if (powerPs != null && powerPs in 45..800) " $powerPs HP" else ""
                        val label = "$litersStr$turboTag $mappedFuel$hpTag".replace(Regex("\\s+"), " ").trim()
                        parsed.add(
                            VehicleEngineOption(
                                engineName = label,
                                fuelType = mappedFuel,
                                specSummary = "${cc} cc • $mappedFuel"
                            )
                        )
                    }
                }
                parsed.distinctBy { norm(it.engineName) }.take(8)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchFromEpaMenu(
        brand: String,
        model: String,
        year: String,
        httpClient: OkHttpClient
    ): List<VehicleEngineOption> {
        return try {
            val targetYear = year.trim().toIntOrNull()?.coerceIn(2005, 2025) ?: 2023
            val encodedMake = Uri.encode(brand.trim())
            val encodedModel = Uri.encode(model.trim().split(" ").firstOrNull().orEmpty())
            val url = "$EPA_OPTIONS_URL?year=$targetYear&make=$encodedMake&model=$encodedModel"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string()?.trim().orEmpty()
                if (!body.startsWith("{")) return emptyList()

                val root = JSONObject(body)
                val items = mutableListOf<String>()
                val menuItemAny = root.opt("menuItem")
                when (menuItemAny) {
                    is JSONArray -> {
                        for (i in 0 until menuItemAny.length()) {
                            val txt = menuItemAny.optJSONObject(i)?.optString("text", "").orEmpty().trim()
                            if (txt.isNotEmpty()) items.add(txt)
                        }
                    }
                    is JSONObject -> {
                        val txt = menuItemAny.optString("text", "").trim()
                        if (txt.isNotEmpty()) items.add(txt)
                    }
                }

                items.mapNotNull { rawText ->
                    // Example EPA text: "Auto (S8), 4 cyl, 2.0 L, Turbo"
                    val dispMatch = Regex("(\\d\\.\\d)\\s*L", RegexOption.IGNORE_CASE).find(rawText)
                    val disp = dispMatch?.groupValues?.getOrNull(1)
                    val isTurbo = rawText.contains("Turbo", ignoreCase = true)
                    val isEv = rawText.contains("Electric", ignoreCase = true) || rawText.contains("kW", ignoreCase = true)
                    val isHybrid = rawText.contains("Hybrid", ignoreCase = true)
                    val isDiesel = rawText.contains("Diesel", ignoreCase = true)

                    when {
                        isEv -> VehicleEngineOption("Elektrik Motoru ($rawText)".take(34), "Elektrik", "Elektrikli Güç Ünitesi")
                        disp != null -> {
                            val fuel = when {
                                isHybrid -> "Hibrit"
                                isDiesel -> "Dizel"
                                else -> "Benzin"
                            }
                            val turboPart = if (isTurbo) " Turbo" else ""
                            VehicleEngineOption("$disp$turboPart $fuel", fuel, rawText)
                        }
                        else -> null
                    }
                }.distinctBy { norm(it.engineName) }.take(6)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mergeUniqueEngines(
        primary: List<VehicleEngineOption>,
        secondary: List<VehicleEngineOption>
    ): List<VehicleEngineOption> {
        val seen = HashSet<String>()
        val result = mutableListOf<VehicleEngineOption>()
        (primary + secondary).forEach { item ->
            val key = norm(item.engineName)
            if (key.isNotEmpty() && seen.add(key)) {
                result.add(item)
            }
        }
        return result
    }

    private fun cacheKey(brand: String, model: String): String {
        return ENGINE_CACHE_PREFIX + norm(brand).replace(" ", "_") + "__" + norm(model).replace(" ", "_")
    }

    private fun saveCachedEngines(
        brand: String,
        model: String,
        engines: List<VehicleEngineOption>,
        prefs: SharedPreferences?
    ) {
        if (prefs == null || engines.isEmpty()) return
        try {
            val arr = JSONArray()
            engines.forEach { e ->
                val obj = JSONObject()
                obj.put("name", e.engineName)
                obj.put("fuel", e.fuelType)
                obj.put("spec", e.specSummary)
                arr.put(obj)
            }
            prefs.edit().putString(cacheKey(brand, model), arr.toString()).apply()
        } catch (_: Exception) {
            // Ignore cache errors
        }
    }

    private fun loadCachedEngines(
        brand: String,
        model: String,
        prefs: SharedPreferences?
    ): List<VehicleEngineOption> {
        val raw = prefs?.getString(cacheKey(brand, model), null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<VehicleEngineOption>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                val fuel = obj.optString("fuel", "Benzin").trim()
                val spec = obj.optString("spec", "").trim()
                if (name.isNotEmpty()) {
                    list.add(VehicleEngineOption(name, fuel, spec))
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
