package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇬🇧"),
    HINDI("hi", "Hindi", "हिन्दी", "🇮🇳"),
    SPANISH("es", "Spanish", "Español", "🇪🇸"),
    FRENCH("fr", "French", "Français", "🇫🇷"),
    GERMAN("de", "German", "Deutsch", "🇩🇪"),
    MARATHI("mr", "Marathi", "मराठी", "🇮🇳"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳"),
    BENGALI("bn", "Bengali", "বাংলা", "🇮🇳")
}

object LocalizationManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _hasSelectedLanguage = MutableStateFlow(false)
    val hasSelectedLanguage: StateFlow<Boolean> = _hasSelectedLanguage.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("app_language_prefs", android.content.Context.MODE_PRIVATE)
            val isSelected = prefs?.getBoolean("has_selected_language", false) ?: false
            _hasSelectedLanguage.value = isSelected

            val savedCode = prefs?.getString("selected_language_code", AppLanguage.ENGLISH.code)
            val savedLang = AppLanguage.values().find { it.code == savedCode } ?: AppLanguage.ENGLISH
            _currentLanguage.value = savedLang
        }
    }

    fun setLanguage(lang: AppLanguage, context: android.content.Context? = null) {
        _currentLanguage.value = lang
        _hasSelectedLanguage.value = true
        if (context != null && prefs == null) {
            init(context)
        }
        prefs?.edit()?.apply {
            putBoolean("has_selected_language", true)
            putString("selected_language_code", lang.code)
            apply()
        }
    }

    fun markLanguageSelected() {
        _hasSelectedLanguage.value = true
        prefs?.edit()?.putBoolean("has_selected_language", true)?.apply()
    }

    fun getString(key: String, lang: AppLanguage = _currentLanguage.value): String {
        val map = translations[key] ?: return key
        return map[lang] ?: map[AppLanguage.ENGLISH] ?: key
    }

    private val translations = mapOf(
        "select_language_title" to mapOf(
            AppLanguage.ENGLISH to "Select Your Language",
            AppLanguage.HINDI to "अपनी भाषा चुनें",
            AppLanguage.SPANISH to "Selecciona tu idioma",
            AppLanguage.FRENCH to "Choisissez votre langue",
            AppLanguage.GERMAN to "Wählen Sie Ihre Sprache",
            AppLanguage.MARATHI to "आपली भाषा निवडा",
            AppLanguage.GUJARATI to "તમારી ભાષા પસંદ કરો",
            AppLanguage.PUNJABI to "ਆਪਣੀ ਭਾਸ਼ਾ ਚੁਣੋ",
            AppLanguage.BENGALI to "আপনার ভাষা নির্বাচন করুন"
        ),
        "select_language_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Choose a language to customize Vikas experience",
            AppLanguage.HINDI to "विकास ऐप का अनुभव अनुकूलित करने के लिए भाषा चुनें",
            AppLanguage.SPANISH to "Elige un idioma para personalizar tu experiencia en Vikas",
            AppLanguage.FRENCH to "Choisissez une langue pour personnaliser l'expérience Vikas",
            AppLanguage.GERMAN to "Wählen Sie eine Sprache, um das Vikas-Erlebnis anzupassen",
            AppLanguage.MARATHI to "विकास ॲप अनुभव सानुकूलित करण्यासाठी भाषा निवडा",
            AppLanguage.GUJARATI to "વિકાસ ઍપ અનુભવને કસ્ટમાઇઝ કરવા ભાષા પસંદ કરો",
            AppLanguage.PUNJABI to "ਵਿਕਾਸ ਐਪ ਅਨੁਭਵ ਨੂੰ ਅਨੁਕੂਲਿਤ ਕਰਨ ਲਈ ਭਾਸ਼ਾ ਚੁਣੋ",
            AppLanguage.BENGALI to "বিকাশ অ্যাপের অভিজ্ঞতা কাস্টমাইজ করতে ভাষা নির্বাচন করুন"
        ),
        "continue" to mapOf(
            AppLanguage.ENGLISH to "Continue",
            AppLanguage.HINDI to "आगे बढ़ें",
            AppLanguage.SPANISH to "Continuar",
            AppLanguage.FRENCH to "Continuer",
            AppLanguage.GERMAN to "Fortfahren",
            AppLanguage.MARATHI to "पुढे जा",
            AppLanguage.GUJARATI to "આગળ વધો",
            AppLanguage.PUNJABI to "ਜਾਰੀ ਰੱਖੋ",
            AppLanguage.BENGALI to "এগিয়ে যান"
        ),
        "app_name" to mapOf(
            AppLanguage.ENGLISH to "Vikas",
            AppLanguage.HINDI to "विकास",
            AppLanguage.SPANISH to "Vikas",
            AppLanguage.FRENCH to "Vikas",
            AppLanguage.GERMAN to "Vikas",
            AppLanguage.MARATHI to "विकास",
            AppLanguage.GUJARATI to "વિકાસ",
            AppLanguage.PUNJABI to "ਵਿਕਾਸ",
            AppLanguage.BENGALI to "বিকাশ"
        ),
        "home" to mapOf(
            AppLanguage.ENGLISH to "Home",
            AppLanguage.HINDI to "मुख्य पृष्ठ",
            AppLanguage.SPANISH to "Inicio",
            AppLanguage.FRENCH to "Accueil",
            AppLanguage.GERMAN to "Startseite",
            AppLanguage.MARATHI to "मुख्यपृष्ठ",
            AppLanguage.GUJARATI to "હોમ",
            AppLanguage.PUNJABI to "ਹੋਮ",
            AppLanguage.BENGALI to "হোম"
        ),
        "search" to mapOf(
            AppLanguage.ENGLISH to "Search",
            AppLanguage.HINDI to "खोजें",
            AppLanguage.SPANISH to "Buscar",
            AppLanguage.FRENCH to "Rechercher",
            AppLanguage.GERMAN to "Suchen",
            AppLanguage.MARATHI to "शोधा",
            AppLanguage.GUJARATI to "શોધો",
            AppLanguage.PUNJABI to "ਖੋਜੋ",
            AppLanguage.BENGALI to "সন্ধান করুন"
        ),
        "cart" to mapOf(
            AppLanguage.ENGLISH to "Cart",
            AppLanguage.HINDI to "कार्ट",
            AppLanguage.SPANISH to "Carrito",
            AppLanguage.FRENCH to "Panier",
            AppLanguage.GERMAN to "Warenkorb",
            AppLanguage.MARATHI to "कार्ट",
            AppLanguage.GUJARATI to "કાર્ટ",
            AppLanguage.PUNJABI to "ਕਾਰਟ",
            AppLanguage.BENGALI to "কার্ট"
        ),
        "orders" to mapOf(
            AppLanguage.ENGLISH to "Orders",
            AppLanguage.HINDI to "ऑर्डर",
            AppLanguage.SPANISH to "Pedidos",
            AppLanguage.FRENCH to "Commandes",
            AppLanguage.GERMAN to "Bestellungen",
            AppLanguage.MARATHI to "ऑर्डर्स",
            AppLanguage.GUJARATI to "ઓર્ડર્સ",
            AppLanguage.PUNJABI to "ਆਰਡਰ",
            AppLanguage.BENGALI to "অর্ডার"
        ),
        "support" to mapOf(
            AppLanguage.ENGLISH to "Support",
            AppLanguage.HINDI to "सहायता",
            AppLanguage.SPANISH to "Soporte",
            AppLanguage.FRENCH to "Support",
            AppLanguage.GERMAN to "Unterstützung",
            AppLanguage.MARATHI to "मदत",
            AppLanguage.GUJARATI to "મદદ",
            AppLanguage.PUNJABI to "ਮਦਦ",
            AppLanguage.BENGALI to "সাহায্য"
        ),
        "seller_portal" to mapOf(
            AppLanguage.ENGLISH to "Seller Portal",
            AppLanguage.HINDI to "विक्रेता पोर्टल",
            AppLanguage.SPANISH to "Portal de Vendedor",
            AppLanguage.FRENCH to "Portail Vendeur",
            AppLanguage.GERMAN to "Verkäuferportal",
            AppLanguage.MARATHI to "विक्रेता पोर्टल",
            AppLanguage.GUJARATI to "વિક્રેતા પોર્ટલ",
            AppLanguage.PUNJABI to "ਵੇਚਣ ਵਾਲਾ ਪੋਰਟਲ",
            AppLanguage.BENGALI to "বিক্রেতা পোর্টাল"
        ),
        "welcome_banner_title" to mapOf(
            AppLanguage.ENGLISH to "Modern Agriculture & Hardware Store",
            AppLanguage.HINDI to "आधुनिक कृषि एवं हार्डवेयर स्टोर",
            AppLanguage.SPANISH to "Tienda Moderna de Agricultura y Ferretería",
            AppLanguage.FRENCH to "Magasin Moderne d'Agriculture et Quincaillerie",
            AppLanguage.GERMAN to "Moderner Landwirtschafts- & Hardware-Shop",
            AppLanguage.MARATHI to "आधुनिक कृषी आणि हार्डवेअर स्टोअर",
            AppLanguage.GUJARATI to "આધુનિક કૃષિ અને હાર્ડવેર સ્ટોર",
            AppLanguage.PUNJABI to "ਆਧੁਨਿਕ ਖੇਤੀਬਾੜੀ ਅਤੇ ਹਾਰਡਵੇਅਰ ਸਟੋਰ",
            AppLanguage.BENGALI to "আধুনিক কৃষি ও হার্ডওয়্যার স্টোর"
        ),
        "welcome_banner_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Quality seeds, tools, machinery & farm supplies delivered fast",
            AppLanguage.HINDI to "गुणवत्तापूर्ण बीज, उपकरण, मशीनरी और कृषि आपूर्ति तेज डिलीवरी के साथ",
            AppLanguage.SPANISH to "Semillas, herramientas y maquinaria de calidad con entrega rápida",
            AppLanguage.FRENCH to "Semences, outils et machines de qualité livrés rapidement",
            AppLanguage.GERMAN to "Hochwertige Samen, Werkzeuge & Maschinen schnell geliefert",
            AppLanguage.MARATHI to "दर्जेदार बियाणे, अवजारे आणि यंत्रसामग्री जलद डिलिव्हरीसह",
            AppLanguage.GUJARATI to "ગુણવત્તાયુક્ત બિયારણ, સાધનો અને મશીનરીઝ ઝડપી ડિલિવરી સાથે",
            AppLanguage.PUNJABI to "ਵਧੀਆ ਬੀਜ, ਸੰਦ, ਮਸ਼ੀਨਰੀ ਅਤੇ ਖੇਤੀ ਦਾ ਸਾਮਾਨ",
            AppLanguage.BENGALI to "উন্নত মানের বীজ, যন্ত্রাংশ ও কৃষি সরঞ্জাম দ্রুত সরবরাহ"
        ),
        "categories" to mapOf(
            AppLanguage.ENGLISH to "Categories",
            AppLanguage.HINDI to "श्रेणियाँ",
            AppLanguage.SPANISH to "Categorías",
            AppLanguage.FRENCH to "Catégories",
            AppLanguage.GERMAN to "Kategorien",
            AppLanguage.MARATHI to "वर्गवारी",
            AppLanguage.GUJARATI to "કેટેગરીઓ",
            AppLanguage.PUNJABI to "ਸ਼੍ਰੇਣੀਆਂ",
            AppLanguage.BENGALI to "বিভাগসমূহ"
        ),
        "featured_products" to mapOf(
            AppLanguage.ENGLISH to "Featured Products",
            AppLanguage.HINDI to "प्रमुख उत्पाद",
            AppLanguage.SPANISH to "Productos Destacados",
            AppLanguage.FRENCH to "Produits En Vedette",
            AppLanguage.GERMAN to "Empfohlene Produkte",
            AppLanguage.MARATHI to "खास उत्पादने",
            AppLanguage.GUJARATI to "ખાસ પ્રોડક્ટ્સ",
            AppLanguage.PUNJABI to "ਖਾਸ ਉਤਪਾਦ",
            AppLanguage.BENGALI to "বিশেষ পণ্য"
        ),
        "add_to_cart" to mapOf(
            AppLanguage.ENGLISH to "Add to Cart",
            AppLanguage.HINDI to "कार्ट में जोड़ें",
            AppLanguage.SPANISH to "Añadir al Carrito",
            AppLanguage.FRENCH to "Ajouter au Panier",
            AppLanguage.GERMAN to "In den Warenkorb",
            AppLanguage.MARATHI to "कार्टमध्ये जोडा",
            AppLanguage.GUJARATI to "કાર્ટમાં ઉમેરો",
            AppLanguage.PUNJABI to "ਕਾਰਟ ਵਿੱਚ ਪਾਓ",
            AppLanguage.BENGALI to "কার্টে যোগ করুন"
        ),
        "log_in" to mapOf(
            AppLanguage.ENGLISH to "Log In",
            AppLanguage.HINDI to "लॉग इन",
            AppLanguage.SPANISH to "Iniciar Sesión",
            AppLanguage.FRENCH to "Connexion",
            AppLanguage.GERMAN to "Anmelden",
            AppLanguage.MARATHI to "लॉग इन",
            AppLanguage.GUJARATI to "લોગ ઇન",
            AppLanguage.PUNJABI to "ਲੌਗ ਇਨ",
            AppLanguage.BENGALI to "লগ ইন"
        ),
        "sign_up" to mapOf(
            AppLanguage.ENGLISH to "Sign Up",
            AppLanguage.HINDI to "साइन अप",
            AppLanguage.SPANISH to "Registrarse",
            AppLanguage.FRENCH to "S'inscrire",
            AppLanguage.GERMAN to "Registrieren",
            AppLanguage.MARATHI to "साइन अप",
            AppLanguage.GUJARATI to "સાઇન અપ્",
            AppLanguage.PUNJABI to "ਸਾਈਨ ਅੱਪ",
            AppLanguage.BENGALI to "সাইন আপ"
        ),
        "language" to mapOf(
            AppLanguage.ENGLISH to "Language",
            AppLanguage.HINDI to "भाषा",
            AppLanguage.SPANISH to "Idioma",
            AppLanguage.FRENCH to "Langue",
            AppLanguage.GERMAN to "Sprache",
            AppLanguage.MARATHI to "भाषा",
            AppLanguage.GUJARATI to "ભાષા",
            AppLanguage.PUNJABI to "ਭਾਸ਼ਾ",
            AppLanguage.BENGALI to "ভাষা"
        )
    )
}
