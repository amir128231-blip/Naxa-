package com.example.data.model

data class StickerItem(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val packId: String,
    val accentColorHex: Long = 0xFF2AABEE,
    val secondaryEmoji: String = ""
)

data class StickerPack(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val isInstalled: Boolean = true,
    val stickers: List<StickerItem>
)

object TelegramStickersCatalog {
    val packs = listOf(
        StickerPack(
            id = "telegram_ducks",
            title = "اردک نکسا (Duck)",
            subtitle = "مجموعه رسمی اردک زرد پرانرژی نکسا",
            iconEmoji = "🦆",
            isInstalled = true,
            stickers = listOf(
                StickerItem("duck_hello", "🦆👋", "سلام و ارادت", "خوش‌آمدگویی پرانرژی", "telegram_ducks", 0xFFFFD21E, "👋"),
                StickerItem("duck_cool", "🦆🕶️", "عالی و خفن", "عینک آفتابی با ژست گانگستری", "telegram_ducks", 0xFF2481CC, "😎"),
                StickerItem("duck_love", "🦆❤️", "عشق و محبت", "قلب سرخ درخشان", "telegram_ducks", 0xFFFF2D55, "❤️"),
                StickerItem("duck_fire", "🦆🔥", "آتیشی و خفن", "انرژی صد درصدی و هیجان", "telegram_ducks", 0xFFFF9500, "🔥"),
                StickerItem("duck_party", "🦆🎉", "جشن و پارتی", "تبریک و جشن پیروزی", "telegram_ducks", 0xFFAF52DE, "🎉"),
                StickerItem("duck_thinking", "🦆🤔", "در حال تفکر", "داری چی میگی رفیق؟", "telegram_ducks", 0xFF5856D6, "💡"),
                StickerItem("duck_crying", "🦆😭", "اشک شوق یا غم", "گریه طنز و احساسی", "telegram_ducks", 0xFF34C759, "🥺"),
                StickerItem("duck_sleep", "🦆💤", "خوابالو", "شب بخیر رفقا وقت خوابه", "telegram_ducks", 0xFF8E8E93, "🌙"),
                StickerItem("duck_rich", "🦆💎", "لاکچری و پولدار", "الماس درخشان و سرمایه‌دار", "telegram_ducks", 0xFF00C7BE, "💰"),
                StickerItem("duck_coffee", "🦆☕", "کافه و آرامش", "یه قهوه گرم با تمرکز بالا", "telegram_ducks", 0xFF8D6E63, "☕"),
                StickerItem("duck_rock", "🦆🎸", "راکستار", "ساز و آواز و موزیک شاد", "telegram_ducks", 0xFFFF5722, "⚡"),
                StickerItem("duck_rocket", "🦆🚀", "پرواز تا ماه", "رشد سریع و جهش بزرگ", "telegram_ducks", 0xFF2AABEE, "✨")
            )
        ),
        StickerPack(
            id = "telegram_cats",
            title = "گربه ملوس (Persian Cat)",
            subtitle = "ری‌اکشن‌های گربه شیطون و بازیگوش",
            iconEmoji = "🐱",
            isInstalled = true,
            stickers = listOf(
                StickerItem("cat_peek", "🐱👀", "چشم چرانی", "زیرچشمی نگاه کردن", "telegram_cats", 0xFFFF9F0A, "👀"),
                StickerItem("cat_heart", "😻💖", "شیفته و مجذوب", "قلب تو چشام جمع شده", "telegram_cats", 0xFFFF375F, "💘"),
                StickerItem("cat_screaming", "🙀💥", "شوک بزرگ", "باورم نمیشه این اتفاق افتاده!", "telegram_cats", 0xFFFFD60A, "⚡"),
                StickerItem("cat_smirk", "😼😏", "لبخند مرموز", "میدونم چیکار کردی!", "telegram_cats", 0xFF30D158, "✨"),
                StickerItem("cat_paws", "🐾✨", "رد پای گربه", "یواشکی رد شدم", "telegram_cats", 0xFF64D2FF, "🐾"),
                StickerItem("cat_fish", "🐱🐟", "ماهی لذیذ", "غذا کجاست؟ گشنمه!", "telegram_cats", 0xFFBF5AF2, "🍣"),
                StickerItem("cat_box", "📦🐱", "جا خوش کرده", "من توی کارتن خودمم راحت!", "telegram_cats", 0xFFFF9500, "📦"),
                StickerItem("cat_kiss", "😽💋", "بوس گربه‌ای", "بوسه از راه دور", "telegram_cats", 0xFFFF2D55, "😘"),
                StickerItem("cat_crown", "🐱👑", "پادشاه خانه", "رئیس منم بقیه ادام درمیارن", "telegram_cats", 0xFFFFD21E, "👑"),
                StickerItem("cat_gamer", "🐱🎮", "گیمر خفن", "پلی‌استیشن و بازی آنلاین", "telegram_cats", 0xFF5856D6, "🎮"),
                StickerItem("cat_ninja", "🐱🥷", "نینجا مخفی", "حمله غافلگیرکننده", "telegram_cats", 0xFF17212B, "⚔️"),
                StickerItem("cat_magic", "🐱🔮", "جادوگر", "پیشگویی آینده گفتگو", "telegram_cats", 0xFFAF52DE, "✨")
            )
        ),
        StickerPack(
            id = "telegram_reactions",
            title = "ری‌اکشن‌های سریع (Reactions)",
            subtitle = "ایموجی‌های بزرگ متحرک و باکیفیت",
            iconEmoji = "🔥",
            isInstalled = true,
            stickers = listOf(
                StickerItem("react_fire", "🔥", "آتشین", "فوق‌العاده و ترند", "telegram_reactions", 0xFFFF5722, "💥"),
                StickerItem("react_100", "💯", "صد در صد", "تایید کامل بدون شک", "telegram_reactions", 0xFFE91E63, "🎯"),
                StickerItem("react_party", "🥳", "مبارکه", "پارتی و خوشحالی", "telegram_reactions", 0xFFFF9800, "🎉"),
                StickerItem("react_rofl", "🤣", "انفجار خنده", "از خنده غش کردم", "telegram_reactions", 0xFFFFC107, "😂"),
                StickerItem("react_heart", "❤️", "قلب سرخ", "عشق خالص", "telegram_reactions", 0xFFF44336, "🌹"),
                StickerItem("react_mindblown", "🤯", "مغزم منفجر شد", "تعجب شدید", "telegram_reactions", 0xFF9C27B0, "💡"),
                StickerItem("react_salute", "🫡", "اطاعت قربان", "سلام نظامی و احترام", "telegram_reactions", 0xFF3F51B5, "🤝"),
                StickerItem("react_pray", "🙏", "دست به دعا", "ممنونم و سپاسگزارم", "telegram_reactions", 0xFF009688, "✨"),
                StickerItem("react_trophy", "🏆", "جام قهرمانی", "برنده اول مسابقه", "telegram_reactions", 0xFFFFB300, "🥇"),
                StickerItem("react_rocket", "🚀", "شلیک فضایی", "سرعت مافوق صوت", "telegram_reactions", 0xFF0288D1, "🪐"),
                StickerItem("react_eyes", "👀", "دارم می‌بینم", "نظارت دقیق", "telegram_reactions", 0xFF607D8B, "🔍"),
                StickerItem("react_cool", "😎", "آسون بود", "خیلی ریلکس و جذاب", "telegram_reactions", 0xFF00BCD4, "🕶️")
            )
        ),
        StickerPack(
            id = "telegram_quick_phrases",
            title = "عبارات کاربردی نکسا (Quick Badges)",
            subtitle = "بج‌های اختصاصی پیام‌های سریع ایرانی",
            iconEmoji = "💬",
            isInstalled = true,
            stickers = listOf(
                StickerItem("phrase_salam", "سلام رفیق 👋", "سلام", "احوالپرسی گرم", "telegram_quick_phrases", 0xFF2481CC, "👋"),
                StickerItem("phrase_damet", "دمت گرم! 👍", "تشکر", "قدردانی باحال", "telegram_quick_phrases", 0xFF34C759, "✨"),
                StickerItem("phrase_payeh", "پایه‌ام شدید 🚀", "موافقت", "همراهی پرانرژی", "telegram_quick_phrases", 0xFFFF9500, "🔥"),
                StickerItem("phrase_eyval", "ایول، ترکوندی! 👏", "تحسین", "تشویق عملکرد عالی", "telegram_quick_phrases", 0xFFAF52DE, "🏆"),
                StickerItem("phrase_koja", "کجایی تو برادر؟ 👀", "پیگیری", "سراغ گرفتن دوستانه", "telegram_quick_phrases", 0xFF00C7BE, "📍"),
                StickerItem("phrase_doroste", "دقیقاً درسته 🤝", "تایید", "توافق کامل روی موضوع", "telegram_quick_phrases", 0xFF5856D6, "👌"),
                StickerItem("phrase_shab", "شب خوش و آرام 🌙", "خداحافظی", "آرزوی شب آرام", "telegram_quick_phrases", 0xFF17212B, "⭐"),
                StickerItem("phrase_zang", "تایم داری زنگ بزنم؟ 📞", "تماس", "هماهنگی مکالمه تلفنی", "telegram_quick_phrases", 0xFF30D158, "📱")
            )
        )
    )

    val recentStickers = listOf(
        StickerItem("duck_cool", "🦆🕶️", "عالی و خفن", "عینک آفتابی با ژست گانگستری", "telegram_ducks", 0xFF2481CC, "😎"),
        StickerItem("react_fire", "🔥", "آتشین", "فوق‌العاده و ترند", "telegram_reactions", 0xFFFF5722, "💥"),
        StickerItem("cat_heart", "😻💖", "شیفته و مجذوب", "قلب تو چشام جمع شده", "telegram_cats", 0xFFFF375F, "💘"),
        StickerItem("duck_rocket", "🦆🚀", "پرواز تا ماه", "رشد سریع و جهش بزرگ", "telegram_ducks", 0xFF2AABEE, "✨"),
        StickerItem("react_100", "💯", "صد در صد", "تایید کامل بدون شک", "telegram_reactions", 0xFFE91E63, "🎯"),
        StickerItem("phrase_damet", "دمت گرم! 👍", "تشکر", "قدردانی باحال", "telegram_quick_phrases", 0xFF34C759, "✨")
    )
}
