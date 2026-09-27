# 📞 AI Phone Call Simulator (المتحدثات الذكيات)
### تطبيق مكالمات صوتية ذكية تفاعلية ثنائية الاتجاه لنظام Android 15 (API 35)

[![Android 15](https://img.shields.io/badge/Android-15%20(API%2035)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/15)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini Flash API](https://img.shields.io/badge/Gemini-2.5%20Flash-F472B6?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

---

## 🌟 نظرة عامة (Overview)

تطبيق **AI Phone Call** هو محاكي مكالمات هاتفية حية تفاعلية مدعوم بنموذج **Gemini 2.5 Flash** ومحركات الصوت الطبيعية لنظام أندرويد.
يقدم التطبيق تجربة محادثة صوتية واقعية ومستمرة تشبه المكالمة الهاتفية الحقيقية مع **10 شخصيات نسائية افتراضية** (5 شخصيات عربية و 5 شخصيات إنجليزية)، تتميز كل شخصية بنبرة صوتية وسرعة مخصصة وطابع حواري فريد.

---

## 🎙️ حلقة المحادثة الصوتية المستمرة (Continuous Voice Loop)

```
       ┌────────────────────────────────────────────────────────┐
       │                 بدء المكالمة الهاتفية                   │
       └──────────────────────────┬─────────────────────────────┘
                                  ▼
      ┌───────────────────────────────────────────────────────────┐
      │   1. نطق الترحيب الصوتي عبر محرك Text-to-Speech (TTS)     │
      └──────────────────────────┬────────────────────────────────┘
                                  ▼
      ┌───────────────────────────────────────────────────────────┐
      │  2. استماع مباشر لكلام المستخدم عبر SpeechRecognizer      │
      └──────────────────────────┬────────────────────────────────┘
                                  ▼
      ┌───────────────────────────────────────────────────────────┐
      │  3. إرسال النص إلى Gemini 2.5 Flash مع شخصية المتصلة     │
      │     (رد موجز جداً وعفوي خالٍ من علامات التنسيق والنجوم)   │
      └──────────────────────────┬────────────────────────────────┘
                                  ▼
      ┌───────────────────────────────────────────────────────────┐
      │  4. نطق الرد عبر TTS بالطبقة الصوتية والسرعة المحددة      │
      └──────────────────────────┬────────────────────────────────┘
                                  │
                                  └────────► العودة للخطوة 2 تلقائياً
```

1. **الاستماع الذكي (Speech-to-Text):** تحويل فوري لصوت المستخدم إلى نص عبر `SpeechRecognizer` مع دعم اللغتين العربية (`ar-SA`) والإنجليزية (`en-US`).
2. **الاستجابة الفورية (Gemini Flash):** إرسال السياق لنموذج `gemini-2.5-flash` ليقوم بالرد بجملة أو جملتين سريعتين كأي مكالمة هاتفية بشرية.
3. **توليد الصوت (Text-to-Speech):** نطق الجواب صوتياً بطبقة صوتية أنثوية هادئة ومريحة.
4. **تجدد الاستماع التلقائي:** بمجرد انتهاء نطق الرد، يُعاد فتح الميكروفون تلقائياً دون الحاجة للضغط على أي زر حتى إنهاء المكالمة.

---

## 👥 الشخصيات المتوفرة (The 10 Personas)

### 🇸🇦 الشخصيات العربية (5 شخصيات):
1. **د. سارة (طبيبة ودودة):** صوت دافئ ومريح، تقدم إرشادات صحية مطمئنة وتستمع باهتمام وتعاطف.
2. **مريم (مستشارة نفسية وتوازن حياتي):** صوت ناعم وهادئ، تساعد في تخفيف التوتر واستعادة السلام النفسي.
3. **نور (صديقة مقربة وعفوية):** صوت مرح وحيوي، تتحدث بتلقائية ومحبة تشبه مكالمة الصديق المقرب.
4. **هدى (أستاذة أدب ولغات):** صوت فصيح وأنيق، للحوارات الثقافية واللغوية الراقية.
5. **ريم (مدربة تطوير ذات):** صوت محفز ومفعم بالإيجابية والنشاط لبدء اليوم وكسر التسويف.

### 🇬🇧 English Personas (5 Personas):
1. **Dr. Emma (Family Physician):** Warm and soothing female voice offering concise wellness tips.
2. **Olivia (Life & Mindset Coach):** Elegant and inspiring voice providing grounding perspectives.
3. **Sophia (Tech Enthusiast):** Upbeat, curious, and cheerful, discussing apps and creative tech.
4. **Lily (Creative Storyteller):** Gentle, poetic, and serene artist with a peaceful phone presence.
5. **Chloe (World Traveler):** Adventurous, spontaneous, and lively explorer full of travel stories.

---

## 📱 مميزات واجهة المكالمة (Call Screen Features)

- **صورة المتصلة ونبضات الرادار (Radar Ripple Effect):** حلقات متوهجة تتمدد وتتقلص بتناغم مع التحدث والاستماع.
- **موجات الصوت التفاعلية (Audio Waveform):** محاكاة حركية مباشرة لترددات الصوت وشدة الديسيبل (`RMS`).
- **مؤشر حالة المكالمة الحي:** (Connecting... • Listening... • Thinking... • Speaking... • Muted).
- **سجل المكالمة المباشر (Live Transcript):** نافذة منسدلة تعرض كل ما يُقال بينك وبين المتصلة نصياً في الوقت الفعلي.
- **شرائح الاقتراحات السريعة (Quick Topic Chips):** مواضيع جاهزة ومخصصة لكل شخصية لتجربة سريعة أو للمحاكيات التي لا تملك ميكروفون.
- **أزرار التحكم بالمكالمة:**
  - زر كتم/إلغاء كتم الميكروفون (**Mute/Unmute**).
  - زر مكبر الصوت (**Speakerphone Toggle**).
  - زر إدخال نصي سريع باللوحة (**Keyboard Input Dialog**).
  - زر إنهاء المكالمة الدائري الأحمر المميز (**End Call**).

---

## 🔐 إعداد أسرار المستودع في GitHub (Repository Secrets)

تمت تهيئة مشروعك في ملف سير العمل (`.github/workflows/build-apk.yml`) ليتطابق بدقة 100% مع الأسرار الخمسة التي قمت بإضافتها في مستودع GitHub كما في إعداداتك:

| اسم السر (Secret Name) | الوصف (Description) |
| :--- | :--- |
| `GEMINI_API_KEY` | مفتاح Google Gemini API لتشغيل نموذج الذكاء الاصطناعي Flash. |
| `KEYSTORE_BASE64` | ملف التوقيع الرقمي (`.jks` أو `.keystore`) مشفر بصيغة Base64 لبناء نسخة Release موقعة. |
| `CM_KEYSTORE_PASSWORD` | كلمة مرور ملف الـ Keystore. |
| `CM_KEY_ALIAS` | الاسم التعريفي لمفتاح التوقيع (Alias). |
| `CM_KEY_PASSWORD` | كلمة مرور المفتاح الخاص بالتوقيع. |

---

## 🚀 خط أنابيب البناء التلقائي (GitHub Actions CI/CD)

يحتوي المستودع على سير العمل المتكامل في `.github/workflows/build-apk.yml`:

```yaml
name: Build Android APK (Android 15)

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    name: Build & Package APKs
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'
      - uses: android-actions/setup-android@v3

      # فك تشفير الكيستور وحقن مفاتيح البيئة
      - name: Setup Keystore and Secrets
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
          GEMINI_API_KEY_SECRET: ${{ secrets.GEMINI_API_KEY }}
        run: |
          echo "GEMINI_API_KEY=${GEMINI_API_KEY_SECRET}" > .env
          echo "GEMINI_API_KEY=${GEMINI_API_KEY_SECRET}" > .env.example
          if [ -n "$KEYSTORE_BASE64" ]; then
            echo "$KEYSTORE_BASE64" | base64 --decode > my-upload-key.jks
          fi

      # بناء نسخة Debug
      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      # بناء نسخة Release موقعة في حال توفر الكيستور
      - name: Build Release APK
        env:
          KEYSTORE_PATH: my-upload-key.jks
          CM_KEYSTORE_PASSWORD: ${{ secrets.CM_KEYSTORE_PASSWORD }}
          CM_KEY_ALIAS: ${{ secrets.CM_KEY_ALIAS }}
          CM_KEY_PASSWORD: ${{ secrets.CM_KEY_PASSWORD }}
        run: |
          if [ -f "my-upload-key.jks" ]; then
            ./gradlew assembleRelease --stacktrace
          fi

      # رفع ملفات الـ APK الجاهزة للتنزيل
      - uses: actions/upload-artifact@v4
        with:
          name: AI-Phone-Call-Debug-APK
          path: app/build/outputs/apk/debug/*.apk

      - uses: actions/upload-artifact@v4
        if: hashFiles('app/build/outputs/apk/release/*.apk') != ''
        with:
          name: AI-Phone-Call-Release-APK
          path: app/build/outputs/apk/release/*.apk
```

### كيفية تنزيل ملف الـ APK بعد الرفع:
1. اذهب إلى تبويب **Actions** في مستودع GitHub الخاص بك.
2. انقر على أحدث تشغيل (Workflow run).
3. انزل لأسفل الصفحة تحت قسم **Artifacts**، وستجد ملفات الـ APK:
   - `AI-Phone-Call-Debug-APK`
   - `AI-Phone-Call-Release-APK`
4. قم بتحميل الملف وفك الضغط وتثبيته مباشرة على هاتفك!

---

## 🛠️ البناء المحلي (Local Development)

### المتطلبات:
* **JDK 17** (Temurin أو Zulu أو OpenJDK).
* **Android Studio Ladybug (2024.2+)** أو أحدث مع Android SDK 35.

### أوامر البناء في الطرفية:
```bash
# بناء حزمة الـ Debug:
./gradlew assembleDebug

# بناء حزمة الـ Release:
./gradlew assembleRelease

# تشغيل الفحوصات والاختبارات:
./gradlew test
```

---

## 📁 هيكلية ملفات المشروع (Project Structure)

```
├── .github/
│   └── workflows/
│       └── build-apk.yml               # خط أنابيب بناء ورفع الـ APK في GitHub Actions
├── app/
│   ├── build.gradle.kts                # إعدادات التطبيق والتوقيع و Android 15
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml      # صلاحيات الصوت والإنترنت واستعلامات SpeechRecognizer
│           ├── java/com/example/
│           │   ├── MainActivity.kt     # النشاط الرئيسي وإدارة الصلاحيات
│           │   ├── data/
│           │   │   └── Persona.kt      # تعريف الـ 10 شخصيات، النبرات، والتوجيهات
│           │   ├── network/
│           │   │   └── GeminiCaller.kt # اتصال موثوق بنموذج Gemini 2.5 Flash عبر OkHttp
│           │   ├── audio/
│           │   │   └── VoiceManager.kt # محرك TTS + SpeechRecognizer ثنائي الاتجاه
│           │   └── ui/
│           │       ├── CallViewModel.kt # إدارة حالة المكالمة والمؤقت والمحادثة
│           │       ├── theme/           # ألوان وسمات Material 3 الحديثة
│           │       └── screens/
│           │           ├── PersonaListScreen.kt # شاشة اختيار المتحدثات مع فلتر اللغات
│           │           └── ActiveCallScreen.kt  # شاشة المكالمة التفاعلية وموجات الصوت
│           └── res/
│               ├── drawable/           # أيقونة التطبيق المخصصة المتكيفة والتدرجات
│               └── values/             # النصوص والثيمات
├── .env.example                         # قالب متغيرات البيئة
├── build.gradle.kts                    # إعدادات المشروع العامة
└── settings.gradle.kts                 # مستودعات التبعيات واسم التطبيق
```

---

## 🔒 الصلاحيات المضمنة والأمان (Permissions)

* `android.permission.INTERNET`: للاتصال بـ Google Gemini API.
* `android.permission.RECORD_AUDIO`: للاستماع لصوت المستخدم عبر الميكروفون أثناء المكالمة.
* `android.permission.MODIFY_AUDIO_SETTINGS`: للتبديل بين سماعة الأذن ومكبر الصوت (Speakerphone).
* استعلامات حزمة التعرف على الصوت: `<queries><intent><action android:name="android.speech.RecognitionService" /></intent></queries>` لضمان عمل محرك الصوت بسلاسة على Android 11 و 12 و 13 و 14 و 15 دون قيود.

---
**صُنع بحرفية لنظام Android 15 • مدعوم بنماذج Google AI Studio**
