package com.example.data.model

import com.example.data.local.FavoritePlaceEntity
import com.example.data.local.NavigationHistoryEntity
import com.example.data.local.OfflineGovernorateEntity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class PlaceCategory(val labelAr: String, val keywordsAr: List<String>) {
    ALL("الكل", emptyList()),
    LANDMARK("المعالم والمراقد", listOf("معلم", "مرقد", "عتبة", "نصب", "قلعة", "زقورة", "متحف", "سياحة")),
    HOSPITAL("المستشفيات والطوارئ", listOf("مستشفى", "طوارئ", "طب", "صحة", "عيادة", "اسعاف")),
    FUEL("محطات الوقود والاستراحة", listOf("وقود", "بنزين", "بانزين", "محطة", "استراحة", "غاز", "شحن")),
    AIRPORT_UNI("المطارات والجامعات", listOf("مطار", "جامعة", "كلية", "معهد", "محطة قطار", "كراج")),
    MARKET_FOOD("الأسواق والمطاعم", listOf("سوق", "مول", "مطعم", "شارع", "تراث", "كورنيش"))
}

enum class ManeuverType(val titleAr: String) {
    DEPART("انطلاق"),
    STRAIGHT("استمر مباشرة"),
    TURN_RIGHT("انعطف يميناً"),
    TURN_LEFT("انعطف يساراً"),
    HIGHWAY_MERGE("اسلك الطريق السريع"),
    ROUNDABOUT("ادخل الفلكة / التقاطع"),
    ARRIVE("وصول إلى الوجهة")
}

data class IraqPlace(
    val id: String,
    val nameAr: String,
    val governorateAr: String,
    val category: PlaceCategory,
    val addressAr: String,
    val latitude: Double,
    val longitude: Double,
    val phoneOrCode: String,
    val openHoursAr: String,
    val rating: Double,
    val tagsAr: List<String>
)

data class RouteStep(
    val index: Int,
    val instructionAr: String,
    val voicePromptAr: String,
    val roadNameAr: String,
    val distanceKm: Double,
    val durationMin: Int,
    val speedLimitKmh: Int,
    val maneuver: ManeuverType,
    val location: LatLng
)

data class ActiveIraqRoute(
    val originNameAr: String,
    val destinationPlace: IraqPlace,
    val originLatLng: LatLng,
    val destinationLatLng: LatLng,
    val polylinePoints: List<LatLng>,
    val steps: List<RouteStep>,
    val totalDistanceKm: Double,
    val totalDurationMin: Int,
    val primaryHighwayAr: String,
    val isOfflineCalculated: Boolean
)

data class HighwayCorridor(
    val nameAr: String,
    val code: String,
    val points: List<LatLng>
)

object IraqMapCatalog {
    // الحدود الجغرافية الصارمة لجمهورية العراق
    const val IRAQ_MIN_LAT = 29.06
    const val IRAQ_MAX_LAT = 37.38
    const val IRAQ_MIN_LNG = 38.79
    const val IRAQ_MAX_LNG = 48.57

    val BAGHDAD_CENTER = LatLng(33.3152, 44.3661)

    val IRAQ_LAT_LNG_BOUNDS = LatLngBounds(
        LatLng(IRAQ_MIN_LAT, IRAQ_MIN_LNG),
        LatLng(IRAQ_MAX_LAT, IRAQ_MAX_LNG)
    )

    fun isInsideIraq(lat: Double, lng: Double): Boolean {
        return lat in IRAQ_MIN_LAT..IRAQ_MAX_LAT && lng in IRAQ_MIN_LNG..IRAQ_MAX_LNG
    }

    fun clampToIraq(lat: Double, lng: Double): LatLng {
        return LatLng(
            lat.coerceIn(IRAQ_MIN_LAT + 0.15, IRAQ_MAX_LAT - 0.15),
            lng.coerceIn(IRAQ_MIN_LNG + 0.15, IRAQ_MAX_LNG - 0.15)
        )
    }

    // مضلع حدود العراق لرسم الخريطة التفاعلية عالية الدقة دون اتصال بالإنترنت
    val IRAQ_BORDER_POLYGON = listOf(
        LatLng(37.33, 42.35), // فيشخابور - دهوك
        LatLng(37.15, 43.35), // شمال دهوك
        LatLng(37.08, 44.78), // حاج عمران - أربيل
        LatLng(35.95, 45.95), // السليمانية - بنجوين
        LatLng(35.12, 46.15), // حلبجة
        LatLng(34.35, 45.45), // خانقين - ديالى
        LatLng(33.15, 46.05), // بدرة وزرباطية - واسط
        LatLng(32.45, 47.45), // الطيب - ميسان
        LatLng(30.98, 47.72), // الشلامجة - البصرة
        LatLng(29.93, 48.48), // رأس البيشة - الفاو
        LatLng(30.08, 47.70), // أم قصر - سفوان
        LatLng(29.10, 46.55), // بصية - المثنى
        LatLng(29.85, 44.70), // السلمان
        LatLng(31.35, 42.10), // النخيب - الأنبار
        LatLng(32.22, 39.20), // طريبيل
        LatLng(33.38, 38.85), // الوليد / التنف
        LatLng(34.40, 41.00), // القائم - الأنبار
        LatLng(36.35, 41.28), // سنجار - نينوى
        LatLng(37.06, 42.10), // ربيعة
        LatLng(37.33, 42.35)
    )

    // مسار نهر دجلة داخل العراق
    val TIGRIS_RIVER_POINTS = listOf(
        LatLng(37.10, 42.38),
        LatLng(36.34, 43.13), // الموصل
        LatLng(35.46, 43.28), // الشرقاط / بيجي
        LatLng(34.60, 43.68), // تكريت
        LatLng(34.19, 43.88), // سامراء
        LatLng(33.32, 44.37), // بغداد
        LatLng(32.51, 45.82), // الكوت
        LatLng(31.84, 47.14), // العمارة
        LatLng(31.00, 47.43), // القرنة (التقاء الرافدين)
        LatLng(30.50, 47.82), // شط العرب - البصرة
        LatLng(29.97, 48.47)  // الفاو - الخليج العربي
    )

    // مسار نهر الفرات داخل العراق
    val EUPHRATES_RIVER_POINTS = listOf(
        LatLng(34.39, 40.98), // القائم
        LatLng(34.14, 42.37), // حديثة
        LatLng(33.43, 43.30), // الرمادي
        LatLng(33.35, 43.78), // الفلوجة
        LatLng(32.78, 44.28), // سدة الهندية
        LatLng(32.47, 44.43), // الحلة - بابل
        LatLng(32.02, 44.40), // الكوفة / النجف
        LatLng(31.32, 45.29), // السماوة
        LatLng(31.04, 46.25), // الناصرية
        LatLng(30.96, 46.98), // الجبايش
        LatLng(31.00, 47.43)  // القرنة
    )

    // شبكة الطرق السريعة الوطنية في العراق
    val IRAQ_HIGHWAYS = listOf(
        HighwayCorridor(
            nameAr = "طريق المرور السريع الدولي رقم 1",
            code = "M1",
            points = listOf(
                LatLng(33.38, 38.95), // طريبيل
                LatLng(33.42, 43.29), // الرمادي
                LatLng(33.31, 44.36), // بغداد
                LatLng(32.47, 44.43), // الحلة
                LatLng(31.99, 44.92), // الديوانية
                LatLng(31.05, 46.26), // الناصرية
                LatLng(30.51, 47.78), // البصرة
                LatLng(30.12, 47.71)  // سفوان
            )
        ),
        HighwayCorridor(
            nameAr = "طريق بغداد - سامراء - الموصل - دهوك (طريق 1 الشمالي)",
            code = "H1",
            points = listOf(
                LatLng(33.31, 44.36), // بغداد
                LatLng(34.20, 43.88), // سامراء
                LatLng(34.61, 43.68), // تكريت
                LatLng(36.34, 43.13), // الموصل
                LatLng(36.86, 42.99), // دهوك
                LatLng(37.14, 42.68)  // زاخو
            )
        ),
        HighwayCorridor(
            nameAr = "طريق بغداد - الخالص - كركوك - أربيل (طريق 2)",
            code = "H2",
            points = listOf(
                LatLng(33.31, 44.36), // بغداد
                LatLng(33.75, 44.63), // بعقوبة / الخالص
                LatLng(34.88, 44.63), // طوزخورماتو
                LatLng(35.47, 44.39), // كركوك
                LatLng(36.19, 44.01)  // أربيل
            )
        ),
        HighwayCorridor(
            nameAr = "طريق الزائرين (بغداد - كربلاء - النجف الأشرف)",
            code = "H9",
            points = listOf(
                LatLng(33.31, 44.36), // بغداد
                LatLng(32.85, 44.20), // المسيب
                LatLng(32.61, 44.03), // كربلاء المقدسة
                LatLng(31.99, 44.31)  // النجف الأشرف
            )
        ),
        HighwayCorridor(
            nameAr = "طريق دجلة الشرقي (بغداد - الكوت - العمارة - البصرة)",
            code = "H6",
            points = listOf(
                LatLng(33.31, 44.36), // بغداد
                LatLng(32.51, 45.82), // الكوت
                LatLng(31.84, 47.14), // العمارة
                LatLng(30.51, 47.78)  // البصرة
            )
        )
    )

    // محافظات العراق الـ 18 كاملة مع حزم الخرائط دون اتصال
    val IRAQ_GOVERNORATES = listOf(
        OfflineGovernorateEntity("BGD", "بغداد", "بغداد العاصمة", 33.3152, 44.3661, 84, 1450, 4820, true),
        OfflineGovernorateEntity("BSR", "البصرة", "البصرة الفيحاء", 30.5085, 47.7804, 68, 720, 3150, true),
        OfflineGovernorateEntity("NIN", "نينوى", "الموصل الحدباء", 36.3450, 43.1575, 72, 680, 3690, true),
        OfflineGovernorateEntity("EBL", "أربيل", "أربيل القلعة", 36.1901, 44.0091, 64, 610, 2940, true),
        OfflineGovernorateEntity("NJF", "النجف الأشرف", "النجف والكوفة", 31.9959, 44.3146, 52, 540, 2100, true),
        OfflineGovernorateEntity("KBL", "كربلاء المقدسة", "كربلاء", 32.6160, 44.0323, 49, 510, 1850, true),
        OfflineGovernorateEntity("SUL", "السليمانية", "السليمانية", 35.5613, 45.4304, 59, 490, 2580, false),
        OfflineGovernorateEntity("BAB", "بابل", "الحلة الأثرية", 32.4722, 44.4328, 46, 410, 1980, false),
        OfflineGovernorateEntity("ANB", "الأنبار", "الرمادي والفلوجة", 33.4259, 43.2995, 76, 390, 4420, false),
        OfflineGovernorateEntity("DQR", "ذي قار", "الناصرية وأور", 31.0439, 46.2575, 48, 360, 2140, false),
        OfflineGovernorateEntity("KRK", "كركوك", "كركوك", 35.4681, 44.3922, 45, 340, 1920, false),
        OfflineGovernorateEntity("DHK", "دهوك", "دهوك وزاخو", 36.8671, 42.9884, 44, 320, 1760, false),
        OfflineGovernorateEntity("SLD", "صلاح الدين", "تكريت وسامراء", 34.6095, 43.6787, 51, 350, 2410, false),
        OfflineGovernorateEntity("DIY", "ديالى", "بعقوبة وخانقين", 33.7485, 44.6433, 47, 310, 2080, false),
        OfflineGovernorateEntity("WST", "واسط", "الكوت", 32.5128, 45.8182, 43, 280, 1890, false),
        OfflineGovernorateEntity("MYS", "ميسان", "العمارة والأهوار", 31.8389, 47.1456, 42, 260, 1740, false),
        OfflineGovernorateEntity("QAD", "القادسية", "الديوانية", 31.9889, 44.9247, 40, 245, 1650, false),
        OfflineGovernorateEntity("MUT", "المثنى", "السماوة وساوة", 31.3200, 45.2800, 50, 220, 2320, false)
    )

    // قاعدة بيانات شاملة للأماكن والمعالم والخدمات في العراق (تعمل مع البحث الذكي ودون اتصال بالإنترنت)
    val IRAQ_PLACES = listOf(
        // بغداد
        IraqPlace(
            id = "bgd_mutanabbi",
            nameAr = "شارع المتنبي والقشلة التراثية",
            governorateAr = "بغداد",
            category = PlaceCategory.MARKET_FOOD,
            addressAr = "الرصافة - ضفاف نهر دجلة قرب جسر الشهداء، بغداد",
            latitude = 33.3406,
            longitude = 44.3886,
            phoneOrCode = "BGD-101",
            openHoursAr = "08:00 ص - 10:00 م",
            rating = 4.9,
            tagsAr = listOf("كتب", "تراث", "دجلة", "الرصافة", "مقهى الشابندر")
        ),
        IraqPlace(
            id = "bgd_martyr",
            nameAr = "نصب الشهيد ومتحف بغداد",
            governorateAr = "بغداد",
            category = PlaceCategory.LANDMARK,
            addressAr = "شارع فلسطين - تقاطع المثنى، بغداد",
            latitude = 33.3433,
            longitude = 44.4456,
            phoneOrCode = "BGD-102",
            openHoursAr = "09:00 ص - 08:00 م",
            rating = 4.8,
            tagsAr = listOf("معلم", "نصب", "شارع فلسطين", "سياحة")
        ),
        IraqPlace(
            id = "bgd_kadhimiya",
            nameAr = "العتبة الكاظمية المقدسة",
            governorateAr = "بغداد",
            category = PlaceCategory.LANDMARK,
            addressAr = "مدينة الكاظمية - ساحة باب المراد، بغداد",
            latitude = 33.3799,
            longitude = 44.3381,
            phoneOrCode = "BGD-103",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 5.0,
            tagsAr = listOf("مرقد", "الكاظمية", "زيارة", "معلم ديني")
        ),
        IraqPlace(
            id = "bgd_medical_city",
            nameAr = "مجمع مدينة الطب التعليمي",
            governorateAr = "بغداد",
            category = PlaceCategory.HOSPITAL,
            addressAr = "باب المعظم - قرب جسر باب المعظم، بغداد",
            latitude = 33.3568,
            longitude = 44.3765,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.6,
            tagsAr = listOf("مستشفى", "طوارئ", "باب المعظم", "اسعاف")
        ),
        IraqPlace(
            id = "bgd_yarmouk_hosp",
            nameAr = "مستشفى اليرموك التعليمي",
            governorateAr = "بغداد",
            category = PlaceCategory.HOSPITAL,
            addressAr = "اليرموك - أربع شوارع، الكرخ، بغداد",
            latitude = 33.2974,
            longitude = 44.3422,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.5,
            tagsAr = listOf("مستشفى", "اليرموك", "طوارئ", "الكرخ")
        ),
        IraqPlace(
            id = "bgd_airport",
            nameAr = "مطار بغداد الدولي (BGW)",
            governorateAr = "بغداد",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "طريق مطار بغداد الدولي السريع، غرب بغداد",
            latitude = 33.2625,
            longitude = 44.2346,
            phoneOrCode = "BGW-INT",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("مطار", "سفر", "طيران", "الكرخ")
        ),
        IraqPlace(
            id = "bgd_uni_jadriya",
            nameAr = "جامعة بغداد - مجمع الجادرية",
            governorateAr = "بغداد",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "الجادرية - الكرادة، ضفاف دجلة، بغداد",
            latitude = 33.2735,
            longitude = 44.3788,
            phoneOrCode = "UOB-01",
            openHoursAr = "07:30 ص - 05:00 م",
            rating = 4.8,
            tagsAr = listOf("جامعة", "الجادرية", "الكرادة", "تعليم")
        ),
        IraqPlace(
            id = "bgd_mansour_fuel",
            nameAr = "محطة وقود المنصور النموذجية + شحن سريع",
            governorateAr = "بغداد",
            category = PlaceCategory.FUEL,
            addressAr = "المنصور - قرب تقاطع الرواد، بغداد",
            latitude = 33.3168,
            longitude = 44.3442,
            phoneOrCode = "FUEL-BGD1",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("وقود", "بنزين محسن", "المنصور", "استراحة")
        ),
        IraqPlace(
            id = "bgd_karrada_market",
            nameAr = "شارع الكرادة داخل ومطاعم الجادرية",
            governorateAr = "بغداد",
            category = PlaceCategory.MARKET_FOOD,
            addressAr = "الكرادة داخل - ساحة كهرمانة، بغداد",
            latitude = 33.3025,
            longitude = 44.4150,
            phoneOrCode = "BGD-FOOD",
            openHoursAr = "09:00 ص - 01:00 ص",
            rating = 4.8,
            tagsAr = listOf("مطعم", "سوق", "الكرادة", "مسكوف")
        ),

        // النجف الأشرف وكربلاء المقدسة
        IraqPlace(
            id = "njf_imam_ali",
            nameAr = "العتبة العلوية المقدسة - مرقد الإمام علي (ع)",
            governorateAr = "النجف الأشرف",
            category = PlaceCategory.LANDMARK,
            addressAr = "المدينة القديمة - مركز النجف الأشرف",
            latitude = 31.9959,
            longitude = 44.3146,
            phoneOrCode = "NJF-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 5.0,
            tagsAr = listOf("مرقد", "النجف", "العتبة العلوية", "زيارة")
        ),
        IraqPlace(
            id = "njf_airport",
            nameAr = "مطار النجف الأشرف الدولي (NJF)",
            governorateAr = "النجف الأشرف",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "طريق المطار - جنوب شرق النجف الأشرف",
            latitude = 31.9897,
            longitude = 44.4042,
            phoneOrCode = "NJF-AIR",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("مطار", "النجف", "رحلات")
        ),
        IraqPlace(
            id = "kbl_imam_hussain",
            nameAr = "العتبة الحسينية والعباسية المقدسة - بين الحرمين",
            governorateAr = "كربلاء المقدسة",
            category = PlaceCategory.LANDMARK,
            addressAr = "مركز مدينة كربلاء المقدسة - ساحة بين الحرمين",
            latitude = 32.6160,
            longitude = 44.0323,
            phoneOrCode = "KBL-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 5.0,
            tagsAr = listOf("مرقد", "كربلاء", "بين الحرمين", "زيارة")
        ),
        IraqPlace(
            id = "kbl_kafeel_hosp",
            nameAr = "مستشفى الكفيل التخصصي",
            governorateAr = "كربلاء المقدسة",
            category = PlaceCategory.HOSPITAL,
            addressAr = "حي الإبراهيمي - طريق كربلاء النجف، كربلاء",
            latitude = 32.5965,
            longitude = 44.0410,
            phoneOrCode = "طوارئ 5152",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.9,
            tagsAr = listOf("مستشفى", "الكفيل", "طوارئ", "كربلاء")
        ),

        // البصرة
        IraqPlace(
            id = "bsr_corniche",
            nameAr = "كورنيش شط العرب وجسر التنومة المعلق",
            governorateAr = "البصرة",
            category = PlaceCategory.LANDMARK,
            addressAr = "العشار - ضفاف شط العرب، البصرة",
            latitude = 30.5172,
            longitude = 47.8403,
            phoneOrCode = "BSR-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.9,
            tagsAr = listOf("شط العرب", "كورنيش", "العشار", "سياحة")
        ),
        IraqPlace(
            id = "bsr_sports_city",
            nameAr = "مدينة البصرة الرياضية (جذع النخلة)",
            governorateAr = "البصرة",
            category = PlaceCategory.LANDMARK,
            addressAr = "طريق البصرة - الزبير السريع، البصرة",
            latitude = 30.4400,
            longitude = 47.7794,
            phoneOrCode = "BSR-SPORT",
            openHoursAr = "08:00 ص - 11:00 م",
            rating = 4.9,
            tagsAr = listOf("ملعب", "جذع النخلة", "البصرة", "الزبير")
        ),
        IraqPlace(
            id = "bsr_airport",
            nameAr = "مطار البصرة الدولي (BSR)",
            governorateAr = "البصرة",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "طريق بغداد - البصرة السريع، البصرة",
            latitude = 30.5491,
            longitude = 47.6621,
            phoneOrCode = "BSR-AIR",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("مطار", "البصرة", "طيران")
        ),
        IraqPlace(
            id = "bsr_hosp",
            nameAr = "مستشفى البصرة التعليمي (الجمهوري)",
            governorateAr = "البصرة",
            category = PlaceCategory.HOSPITAL,
            addressAr = "البراضعية - مركز مدينة البصرة",
            latitude = 30.4958,
            longitude = 47.8225,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.6,
            tagsAr = listOf("مستشفى", "طوارئ", "البصرة")
        ),
        IraqPlace(
            id = "bsr_highway_fuel",
            nameAr = "محطة واستراحة بوابة البصرة السريعة (M1)",
            governorateAr = "البصرة",
            category = PlaceCategory.FUEL,
            addressAr = "مدخل الطريق السريع الدولي رقم 1 - الرميلة، البصرة",
            latitude = 30.5620,
            longitude = 47.5810,
            phoneOrCode = "FUEL-BSR",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("وقود", "استراحة", "طريق دولي", "البصرة")
        ),

        // نينوى (الموصل)
        IraqPlace(
            id = "nin_hadba",
            nameAr = "الجامع النوري الكبير ومنارة الحدباء التاريخية",
            governorateAr = "نينوى",
            category = PlaceCategory.LANDMARK,
            addressAr = "الموصل القديمة - الجانب الأيمن، الموصل",
            latitude = 36.3431,
            longitude = 43.1269,
            phoneOrCode = "MOS-001",
            openHoursAr = "07:00 ص - 09:00 م",
            rating = 4.9,
            tagsAr = listOf("منارة الحدباء", "الموصل", "تراث", "معلم")
        ),
        IraqPlace(
            id = "nin_mosul_uni",
            nameAr = "جامعة الموصل - المجموعة الثقافية",
            governorateAr = "نينوى",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "الجانب الأيسر - حي الجامعة، الموصل",
            latitude = 36.3775,
            longitude = 43.1442,
            phoneOrCode = "UOM-01",
            openHoursAr = "08:00 ص - 04:00 م",
            rating = 4.8,
            tagsAr = listOf("جامعة الموصل", "نينوى", "المكتبة المركزية")
        ),
        IraqPlace(
            id = "nin_salam_hosp",
            nameAr = "مستشفى السلام التعليمي العام",
            governorateAr = "نينوى",
            category = PlaceCategory.HOSPITAL,
            addressAr = "حي الوحدة - الجانب الأيسر، الموصل",
            latitude = 36.3228,
            longitude = 43.1684,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.6,
            tagsAr = listOf("مستشفى", "طوارئ", "الموصل")
        ),

        // أربيل والسليمانية ودهوك
        IraqPlace(
            id = "ebl_citadel",
            nameAr = "قلعة أربيل التاريخية وسوق القيصرية",
            governorateAr = "أربيل",
            category = PlaceCategory.LANDMARK,
            addressAr = "مركز مدينة أربيل - ساحة النافورة",
            latitude = 36.1911,
            longitude = 44.0092,
            phoneOrCode = "EBL-001",
            openHoursAr = "08:00 ص - 11:00 م",
            rating = 4.9,
            tagsAr = listOf("قلعة أربيل", "سوق القيصرية", "سياحة", "يونسكو")
        ),
        IraqPlace(
            id = "ebl_airport",
            nameAr = "مطار أربيل الدولي (EBL)",
            governorateAr = "أربيل",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "عنكاوا - شمال غرب أربيل",
            latitude = 36.2376,
            longitude = 43.9632,
            phoneOrCode = "EBL-AIR",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.8,
            tagsAr = listOf("مطار أربيل", "طيران", "سفر")
        ),
        IraqPlace(
            id = "ebl_rizgari_hosp",
            nameAr = "مستشفى رزكاري التعليمي",
            governorateAr = "أربيل",
            category = PlaceCategory.HOSPITAL,
            addressAr = "شارع كويه - أربيل",
            latitude = 36.1885,
            longitude = 44.0412,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.6,
            tagsAr = listOf("مستشفى", "طوارئ", "أربيل")
        ),
        IraqPlace(
            id = "sul_azmar",
            nameAr = "جبل أزمر ومتنزه هواري شار",
            governorateAr = "السليمانية",
            category = PlaceCategory.LANDMARK,
            addressAr = "شمال شرق مدينة السليمانية",
            latitude = 35.5882,
            longitude = 45.4651,
            phoneOrCode = "SUL-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.9,
            tagsAr = listOf("جبل أزمر", "السليمانية", "طبيعة", "مصيف")
        ),
        IraqPlace(
            id = "dhk_delal",
            nameAr = "جسر دلال العباسي وسد دهوك",
            governorateAr = "دهوك",
            category = PlaceCategory.LANDMARK,
            addressAr = "زاخو ومركز دهوك",
            latitude = 36.8902,
            longitude = 43.0014,
            phoneOrCode = "DHK-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.9,
            tagsAr = listOf("دهوك", "زاخو", "جسر دلال", "سياحة")
        ),

        // بابل، ذي قار، كركوك، صلاح الدين، الأنبار، وميسان
        IraqPlace(
            id = "bab_ancient",
            nameAr = "مدينة بابل الأثرية وبوابة عشتار",
            governorateAr = "بابل",
            category = PlaceCategory.LANDMARK,
            addressAr = "طريق الحلة - بغداد، شمال الحلة، بابل",
            latitude = 32.5422,
            longitude = 44.4211,
            phoneOrCode = "BAB-001",
            openHoursAr = "08:00 ص - 06:00 م",
            rating = 4.9,
            tagsAr = listOf("آثار بابل", "أسد بابل", "الحلة", "يونسكو")
        ),
        IraqPlace(
            id = "dqr_ur_ziggurat",
            nameAr = "زقورة أور السومرية ومدينة أور الأثرية",
            governorateAr = "ذي قار",
            category = PlaceCategory.LANDMARK,
            addressAr = "غرب مدينة الناصرية - ذي قار",
            latitude = 30.9628,
            longitude = 46.1031,
            phoneOrCode = "DQR-001",
            openHoursAr = "08:00 ص - 05:30 م",
            rating = 5.0,
            tagsAr = listOf("زقورة أور", "الناصرية", "سومر", "آثار")
        ),
        IraqPlace(
            id = "dqr_chibayish",
            nameAr = "أهوار الجبايش الوسطى (التراث العالمي)",
            governorateAr = "ذي قار",
            category = PlaceCategory.LANDMARK,
            addressAr = "قضاء الجبايش - شرق ذي قار",
            latitude = 30.9652,
            longitude = 46.9791,
            phoneOrCode = "DQR-002",
            openHoursAr = "06:00 ص - 07:00 م",
            rating = 4.8,
            tagsAr = listOf("الأهوار", "الجبايش", "الناصرية", "طبيعة")
        ),
        IraqPlace(
            id = "sld_malwiya",
            nameAr = "ملوية سامراء والعتبة العسكرية المقدسة",
            governorateAr = "صلاح الدين",
            category = PlaceCategory.LANDMARK,
            addressAr = "مركز قضاء سامراء - صلاح الدين",
            latitude = 34.2059,
            longitude = 43.8799,
            phoneOrCode = "SLD-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.9,
            tagsAr = listOf("ملوية سامراء", "سامراء", "مرقد", "تراث")
        ),
        IraqPlace(
            id = "krk_citadel",
            nameAr = "قلعة كركوك التاريخية وجسر خاصة صو",
            governorateAr = "كركوك",
            category = PlaceCategory.LANDMARK,
            addressAr = "مركز مدينة كركوك",
            latitude = 35.4721,
            longitude = 44.3975,
            phoneOrCode = "KRK-001",
            openHoursAr = "08:00 ص - 08:00 م",
            rating = 4.7,
            tagsAr = listOf("قلعة كركوك", "كركوك", "تراث")
        ),
        IraqPlace(
            id = "anb_habbaniyah",
            nameAr = "بحيرة الحبانية واستراحة الطريق السريع الدولي",
            governorateAr = "الأنبار",
            category = PlaceCategory.FUEL,
            addressAr = "بين الفلوجة والرمادي - طريق السريع الدولي رقم 1، الأنبار",
            latitude = 33.3685,
            longitude = 43.5750,
            phoneOrCode = "ANB-FUEL",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("وقود", "بحيرة الحبانية", "الرمادي", "الفلوجة", "استراحة")
        ),
        IraqPlace(
            id = "wst_kut_barrage",
            nameAr = "سدة الكوت وكورنيش دجلة في واسط",
            governorateAr = "واسط",
            category = PlaceCategory.LANDMARK,
            addressAr = "مركز مدينة الكوت - ضفاف نهر دجلة، واسط",
            latitude = 32.5025,
            longitude = 45.8195,
            phoneOrCode = "WST-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.8,
            tagsAr = listOf("سدة الكوت", "واسط", "دجلة", "كورنيش")
        ),
        IraqPlace(
            id = "mys_amara_corniche",
            nameAr = "كورنيش السراي في العمارة ومحطة وقود ميسان",
            governorateAr = "ميسان",
            category = PlaceCategory.FUEL,
            addressAr = "مركز مدينة العمارة - ميسان",
            latitude = 31.8415,
            longitude = 47.1450,
            phoneOrCode = "MYS-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("العمارة", "ميسان", "وقود", "كورنيش")
        ),
        IraqPlace(
            id = "diy_baqubah_hosp",
            nameAr = "مستشفى بعقوبة التعليمي ومحطة وقود ديالى",
            governorateAr = "ديالى",
            category = PlaceCategory.HOSPITAL,
            addressAr = "مركز بعقوبة - تقاطع البلدة، ديالى",
            latitude = 33.7492,
            longitude = 44.6448,
            phoneOrCode = "طوارئ 122",
            openHoursAr = "طوارئ 24 ساعة",
            rating = 4.6,
            tagsAr = listOf("بعقوبة", "ديالى", "مستشفى", "طوارئ")
        ),
        IraqPlace(
            id = "qad_qadisiyah_uni",
            nameAr = "جامعة القادسية ومحطة وقود الديوانية",
            governorateAr = "القادسية",
            category = PlaceCategory.AIRPORT_UNI,
            addressAr = "طريق الديوانية - الحلة، القادسية",
            latitude = 31.9950,
            longitude = 44.9180,
            phoneOrCode = "QAD-UNI",
            openHoursAr = "07:00 ص - 09:00 م",
            rating = 4.7,
            tagsAr = listOf("الديوانية", "القادسية", "جامعة", "وقود")
        ),
        IraqPlace(
            id = "mut_sawa_lake",
            nameAr = "بحيرة ساوة واستراحة السماوة على السريع الدولي",
            governorateAr = "المثنى",
            category = PlaceCategory.FUEL,
            addressAr = "غرب السماوة - طريق المرور السريع رقم 1، المثنى",
            latitude = 31.3145,
            longitude = 45.0065,
            phoneOrCode = "MUT-001",
            openHoursAr = "مفتوح 24 ساعة",
            rating = 4.7,
            tagsAr = listOf("السماوة", "المثنى", "بحيرة ساوة", "استراحة", "وقود")
        )
    )

    val DEFAULT_FAVORITES = listOf(
        FavoritePlaceEntity(
            nameAr = "شارع المتنبي والقشلة التراثية",
            governorateAr = "بغداد",
            categoryAr = PlaceCategory.MARKET_FOOD.labelAr,
            addressAr = "الرصافة - ضفاف نهر دجلة قرب جسر الشهداء، بغداد",
            latitude = 33.3406,
            longitude = 44.3886,
            notesAr = "موقع ثقافي وتراثي مميز في قلب بغداد - محفوظ للعمل دون إنترنت",
            isOfflineReady = true
        ),
        FavoritePlaceEntity(
            nameAr = "العتبة العلوية المقدسة - مرقد الإمام علي (ع)",
            governorateAr = "النجف الأشرف",
            categoryAr = PlaceCategory.LANDMARK.labelAr,
            addressAr = "المدينة القديمة - مركز النجف الأشرف",
            latitude = 31.9959,
            longitude = 44.3146,
            notesAr = "المسار المفضل عبر طريق بغداد - كربلاء - النجف",
            isOfflineReady = true
        ),
        FavoritePlaceEntity(
            nameAr = "كورنيش شط العرب وجسر التنومة المعلق",
            governorateAr = "البصرة",
            categoryAr = PlaceCategory.LANDMARK.labelAr,
            addressAr = "العشار - ضفاف شط العرب، البصرة",
            latitude = 30.5172,
            longitude = 47.8403,
            notesAr = "وجهة سياحية جنوب العراق عبر الطريق السريع الدولي رقم 1",
            isOfflineReady = true
        ),
        FavoritePlaceEntity(
            nameAr = "قلعة أربيل التاريخية وسوق القيصرية",
            governorateAr = "أربيل",
            categoryAr = PlaceCategory.LANDMARK.labelAr,
            addressAr = "مركز مدينة أربيل - ساحة النافورة",
            latitude = 36.1911,
            longitude = 44.0092,
            notesAr = "محفوظ مع خريطة إقليم كردستان العراق دون اتصال",
            isOfflineReady = true
        )
    )

    val DEFAULT_HISTORY = listOf(
        NavigationHistoryEntity(
            originNameAr = "بغداد - ساحة التحرير",
            destinationNameAr = "العتبة الحسينية والعباسية المقدسة - بين الحرمين",
            governorateAr = "كربلاء المقدسة",
            destLat = 32.6160,
            destLng = 44.0323,
            distanceKm = 104.5,
            durationMinutes = 78,
            highwayNameAr = "طريق الزائرين (بغداد - المحمودية - المسيب - كربلاء)",
            wasOfflineMode = true,
            timestamp = System.currentTimeMillis() - 3600_000L * 5
        ),
        NavigationHistoryEntity(
            originNameAr = "بغداد - المنصور",
            destinationNameAr = "مطار بغداد الدولي (BGW)",
            governorateAr = "بغداد",
            destLat = 33.2625,
            destLng = 44.2346,
            distanceKm = 16.4,
            durationMinutes = 19,
            highwayNameAr = "طريق مطار بغداد الدولي السريع",
            wasOfflineMode = false,
            timestamp = System.currentTimeMillis() - 3600_000L * 22
        )
    )

    // تطبيع النصوص العربية لدعم البحث الذكي المرن (الهمزات، التاء المربوطة، الألف المقصورة)
    fun normalizeArabic(input: String): String {
        return input.trim().lowercase()
            .replace(Regex("[أإآٱ]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace(Regex("[\\u064B-\\u065F]"), "") // إزالة التشكيل
    }

    // حساب المسافة الجغرافية الدقيقة بالكيلومتر (معادلة Haversine)
    fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return ((r * c) * 10.0).roundToInt() / 10.0
    }

    // تحديد الاتجاه الجغرافي بالعربية
    fun calculateBearingAr(lat1: Double, lng1: Double, lat2: Double, lng2: Double): String {
        val dLng = Math.toRadians(lng2 - lng1)
        val y = sin(dLng) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
            sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLng)
        val brng = (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
        return when {
            brng < 22.5 || brng >= 337.5 -> "شمالاً"
            brng < 67.5 -> "شمال شرق"
            brng < 112.5 -> "شرقاً"
            brng < 157.5 -> "جنوب شرق"
            brng < 202.5 -> "جنوباً"
            brng < 247.5 -> "جنوب غرب"
            brng < 292.5 -> "غرباً"
            else -> "شمال غرب"
        }
    }

    // توليد مسار ملاحة دقيق داخل العراق مع خطوات التوجيه الصوتي العربي
    fun buildIraqRoute(
        originLatLng: LatLng,
        originNameAr: String,
        destination: IraqPlace,
        isOffline: Boolean
    ): ActiveIraqRoute {
        val destLatLng = LatLng(destination.latitude, destination.longitude)
        val directDistKm = calculateDistanceKm(
            originLatLng.latitude,
            originLatLng.longitude,
            destLatLng.latitude,
            destLatLng.longitude
        ).coerceAtLeast(1.2)

        val bearingAr = calculateBearingAr(
            originLatLng.latitude,
            originLatLng.longitude,
            destLatLng.latitude,
            destLatLng.longitude
        )

        val highwayName = when {
            destination.governorateAr in listOf("البصرة", "ذي قار", "المثنى", "القادسية") ->
                "طريق المرور السريع الدولي رقم 1 (M1)"
            destination.governorateAr in listOf("النجف الأشرف", "كربلاء المقدسة", "بابل") ->
                "طريق الفرات الأوسط السريع (بغداد - الحلة - كربلاء - النجف)"
            destination.governorateAr in listOf("نينوى", "دهوك", "صلاح الدين") ->
                "طريق دجلة السريع رقم 1 (بغداد - سامراء - الموصل)"
            destination.governorateAr in listOf("أربيل", "كركوك", "السليمانية", "ديالى") ->
                "طريق بغداد - الخالص - كركوك - أربيل السريع"
            destination.governorateAr == "الأنبار" ->
                "طريق المرور السريع الدولي الغربي (بغداد - الفلوجة - الرمادي)"
            else -> "طريق محمد القاسم السريع / قناة الجيش - بغداد"
        }

        // بناء نقاط المسار المنحنية الواقعية على خارطة العراق
        val waypoints = mutableListOf<LatLng>()
        waypoints.add(originLatLng)

        val segments = 6
        for (i in 1 until segments) {
            val t = i.toDouble() / segments
            // انحناء طفيف يحاكي محاور الطرق العراقية حول نهري دجلة والفرات
            val curveOffsetLat = sin(t * Math.PI) * 0.04 * (if (destLatLng.longitude >= originLatLng.longitude) 1 else -1)
            val curveOffsetLng = sin(t * Math.PI) * 0.05
            val lat = originLatLng.latitude + (destLatLng.latitude - originLatLng.latitude) * t + curveOffsetLat * (directDistKm / 120.0).coerceAtMost(1.2)
            val lng = originLatLng.longitude + (destLatLng.longitude - originLatLng.longitude) * t + curveOffsetLng * (directDistKm / 120.0).coerceAtMost(1.2)
            waypoints.add(clampToIraq(lat, lng))
        }
        waypoints.add(destLatLng)

        val roadDistanceKm = ((directDistKm * 1.18) * 10.0).roundToInt() / 10.0
        val avgSpeedKmh = if (roadDistanceKm > 45.0) 95.0 else 52.0
        val totalDurationMin = ((roadDistanceKm / avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(4)

        val step1Dist = ((roadDistanceKm * 0.15) * 10.0).roundToInt() / 10.0
        val step2Dist = ((roadDistanceKm * 0.52) * 10.0).roundToInt() / 10.0
        val step3Dist = ((roadDistanceKm * 0.23) * 10.0).roundToInt() / 10.0
        val step4Dist = ((roadDistanceKm * 0.10) * 10.0).roundToInt() / 10.0

        val steps = listOf(
            RouteStep(
                index = 0,
                instructionAr = "انطلق من $originNameAr واتجه $bearingAr نحو المخرج الرئيسي",
                voicePromptAr = "بسم الله، بدأ التوجيه الصوتي في درب الرافدين. انطلق من $originNameAr واتجه $bearingAr لمسافة $step1Dist كيلومتر.",
                roadNameAr = "الشارع الخدمي المحلي - $originNameAr",
                distanceKm = step1Dist,
                durationMin = (totalDurationMin * 0.18).roundToInt().coerceAtLeast(1),
                speedLimitKmh = 60,
                maneuver = ManeuverType.DEPART,
                location = waypoints[0]
            ),
            RouteStep(
                index = 1,
                instructionAr = "اسلك $highwayName واستمر في المسار الأوسط",
                voicePromptAr = "ادخل الآن في $highwayName واستمر مباشرة لمسافة $step2Dist كيلومتر. السرعة المحددة مئة كيلومتر في الساعة.",
                roadNameAr = highwayName,
                distanceKm = step2Dist,
                durationMin = (totalDurationMin * 0.48).roundToInt().coerceAtLeast(2),
                speedLimitKmh = if (roadDistanceKm > 40) 100 else 80,
                maneuver = ManeuverType.HIGHWAY_MERGE,
                location = waypoints[2]
            ),
            RouteStep(
                index = 2,
                instructionAr = "عند التقاطع الرئيسي، خذ المخرج المؤدي إلى مركز ${destination.governorateAr}",
                voicePromptAr = "بعد ستمائة متر، خذ المخرج الأيمن باتجاه مركز محافظة ${destination.governorateAr}.",
                roadNameAr = "مدخل محافظة ${destination.governorateAr} الرئيسي",
                distanceKm = step3Dist,
                durationMin = (totalDurationMin * 0.22).roundToInt().coerceAtLeast(1),
                speedLimitKmh = 80,
                maneuver = ManeuverType.TURN_RIGHT,
                location = waypoints[4]
            ),
            RouteStep(
                index = 3,
                instructionAr = "ادخل الفلكة واستمر نحو ${destination.addressAr}",
                voicePromptAr = "ادخل الفلكة ثم انعطف يساراً نحو ${destination.nameAr}، تبقى $step4Dist كيلومتر.",
                roadNameAr = destination.addressAr,
                distanceKm = step4Dist,
                durationMin = (totalDurationMin * 0.12).roundToInt().coerceAtLeast(1),
                speedLimitKmh = 50,
                maneuver = ManeuverType.ROUNDABOUT,
                location = waypoints[5]
            ),
            RouteStep(
                index = 4,
                instructionAr = "وصلت إلى وجهتك: ${destination.nameAr} (${destination.governorateAr})",
                voicePromptAr = "الحمد لله على السلامة، لقد وصلت إلى وجهتك ${destination.nameAr} في محافظة ${destination.governorateAr}.",
                roadNameAr = destination.nameAr,
                distanceKm = 0.0,
                durationMin = 0,
                speedLimitKmh = 30,
                maneuver = ManeuverType.ARRIVE,
                location = destLatLng
            )
        )

        return ActiveIraqRoute(
            originNameAr = originNameAr,
            destinationPlace = destination,
            originLatLng = originLatLng,
            destinationLatLng = destLatLng,
            polylinePoints = waypoints,
            steps = steps,
            totalDistanceKm = roadDistanceKm,
            totalDurationMin = totalDurationMin,
            primaryHighwayAr = highwayName,
            isOfflineCalculated = isOffline
        )
    }
}
