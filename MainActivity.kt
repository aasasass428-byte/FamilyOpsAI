package com.familyops.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.app.AlertDialog
import android.content.SharedPreferences
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var prefs: SharedPreferences
    private lateinit var content: LinearLayout
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var activeTab = "Главная"
    private val bg = Color.rgb(246, 248, 252)
    private val ink = Color.rgb(28, 39, 58)
    private val blue = Color.rgb(46, 91, 170)
    private val muted = Color.rgb(103, 116, 137)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("familyops", MODE_PRIVATE)
        tts = TextToSpeech(this, this)
        renderShell()
        showTab("Главная")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("ru", "RU")
            ttsReady = true
        }
    }

    private fun renderShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 16)
            setBackgroundColor(Color.WHITE)
        }
        header.addView(TextView(this).apply {
            text = "FamilyOps AI"
            textSize = 26f; setTextColor(ink); setTypeface(null, 1)
        })
        header.addView(TextView(this).apply {
            text = "Ваш семейный помощник • версия 0.1"
            textSize = 13f; setTextColor(muted); setPadding(0, 4, 0, 0)
        })
        root.addView(header)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 18)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val navScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(8, 8, 8, 8)
        }
        listOf("Главная", "Диалог", "Память", "Проекты", "Речь", "Настройки").forEach { label ->
            val b = Button(this).apply {
                text = label
                textSize = 11f
                isAllCaps = false
                setOnClickListener { showTab(label) }
            }
            nav.addView(b)
        }
        navScroll.addView(nav)
        root.addView(navScroll)
        setContentView(root)
    }

    private fun title(s: String) = TextView(this).apply {
        text = s; textSize = 21f; setTextColor(ink); setTypeface(null, 1)
        setPadding(0, 0, 0, 10)
    }

    private fun paragraph(s: String) = TextView(this).apply {
        text = s; textSize = 15f; setTextColor(ink); setPadding(0, 0, 0, 10)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 15, 16, 15)
        setBackgroundColor(Color.WHITE)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = 12
        layoutParams = p
        elevation = 2f
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false
        setOnClickListener { action() }
    }

    private fun showTab(tab: String) {
        activeTab = tab
        content.removeAllViews()
        when (tab) {
            "Главная" -> home()
            "Диалог" -> chat()
            "Память" -> memory()
            "Проекты" -> projects()
            "Речь" -> coach()
            else -> settings()
        }
    }

    private fun home() {
        content.addView(title("Всё важное — в одном месте"))
        content.addView(paragraph("FamilyOps AI — начальная версия приложения. Данные заметок и проектов сохраняются на этом телефоне."))
        val features = listOf(
            "💬 Диалог" to "Задавайте вопросы текстом или голосом; ответ можно прослушать.",
            "🧠 Память" to "Записывайте, где лежат вещи, и сохраняйте важные сведения.",
            "🏡 Проекты" to "Ведите заметки о доме, участке, саде и оборудовании.",
            "🗣️ Речь" to "Практикуйте разговор и проверяйте распознанный текст.",
            "🔒 Приватность" to "Локальные заметки хранятся в данных приложения на телефоне."
        )
        features.forEach { (h, d) ->
            val c = card(); c.addView(TextView(this).apply { text = h; textSize = 17f; setTextColor(blue); setTypeface(null, 1) })
            c.addView(paragraph(d)); content.addView(c)
        }
        content.addView(paragraph("Важно: в этой сборке пока нет подключённой облачной ИИ-модели, анализа изображений, распознавания камер или клонирования голоса. Это каркас для дальнейшей разработки, а не законченный продукт со всеми заявленными возможностями."))
    }

    private fun chat() {
        content.addView(title("Диалог с помощником"))
        content.addView(paragraph("Пока доступен локальный демонстрационный режим. Для ответов на актуальные и сложные вопросы потребуется подключить ИИ-сервис."))
        val history = TextView(this).apply {
            text = prefs.getString("chat", "Начните диалог ниже.") ?: ""
            textSize = 14f; setTextColor(ink); setPadding(12, 12, 12, 12)
            setBackgroundColor(Color.WHITE)
        }
        content.addView(history)
        val input = EditText(this).apply { hint = "Напишите сообщение…"; minLines = 2; setTextColor(ink) }
        content.addView(input)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(button("Отправить") {
            val q = input.text.toString().trim()
            if (q.isNotEmpty()) {
                val answer = localReply(q)
                val updated = (prefs.getString("chat", "") ?: "") + "\n\nВы: $q\nFamilyOps: $answer"
                prefs.edit().putString("chat", updated).apply()
                history.text = updated
                input.setText("")
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(button("🎙 Голос") { startSpeech() }, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(row)
        content.addView(button("🔊 Прочитать последний ответ") {
            val last = (prefs.getString("chat", "") ?: "").substringAfterLast("FamilyOps: ", "")
            if (last.isNotBlank() && ttsReady) tts.speak(last, TextToSpeech.QUEUE_FLUSH, null, "familyops_reply")
            else Toast.makeText(this, "Пока нет ответа для озвучивания", Toast.LENGTH_SHORT).show()
        })
        content.addView(button("Очистить историю") {
            prefs.edit().remove("chat").apply(); showTab("Диалог")
        })
    }

    private fun localReply(q: String): String {
        val s = q.lowercase(Locale.ROOT)
        return when {
            "где" in s && ("дрел" in s || "молот" in s || "ключ" in s) ->
                "Я пока не нашёл сохранённого местоположения этой вещи. Добавьте её в разделе «Память», указав место и дату."
            "сад" in s || "растен" in s ->
                "Могу помочь вести записи по саду. Сохраните название растения, место, дату посадки и наблюдения в разделе «Проекты». Для надёжного определения по фото нужен модуль анализа изображений."
            "привет" in s ->
                "Здравствуйте! Я FamilyOps AI. Сейчас работаю в локальном демонстрационном режиме."
            "план" in s || "задач" in s ->
                "Запишите проект или задачу в разделе «Проекты». Автоматические напоминания пока не подключены."
            else ->
                "Я сохранил ваш вопрос в истории. В этой версии нет подключённой языковой модели, поэтому я не могу дать полноценный интеллектуальный ответ. Следующий этап — подключение ИИ-сервиса."
        }
    }

    private fun startSpeech() {
        try {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите по-русски")
            }
            startActivityForResult(i, 101)
        } catch (_: Exception) {
            Toast.makeText(this, "На устройстве недоступно распознавание речи", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Android but retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                showTab("Диалог")
                Toast.makeText(this, "Распознано: $spoken — скопируйте текст в поле диалога", Toast.LENGTH_LONG).show()
                // Keep the recognized phrase in persistent draft for easy retrieval.
                prefs.edit().putString("speech_draft", spoken).apply()
                val e = EditText(this)
                // Intentional simple UI: voice result is shown in a dialog so it can be copied.
                AlertDialog.Builder(this).setTitle("Распознанная речь").setMessage(spoken)
                    .setPositiveButton("Готово", null).show()
            }
        }
    }

    private fun memory() {
        content.addView(title("Память и места хранения"))
        content.addView(paragraph("Записывайте факты о вещах. Поиск по сохранённым заметкам будет доступен в следующем этапе."))
        val old = prefs.getString("memory", "") ?: ""
        val box = EditText(this).apply {
            hint = "Например: Дрель лежит в нижнем ящике верстака. Проверено сегодня."
            setText(old); minLines = 5; gravity = Gravity.TOP; setTextColor(ink)
        }
        content.addView(box)
        content.addView(button("Сохранить память") {
            prefs.edit().putString("memory", box.text.toString()).apply()
            Toast.makeText(this, "Записи сохранены на устройстве", Toast.LENGTH_SHORT).show()
        })
        content.addView(paragraph("Совет: указывайте дату и источник. Вещь могли переложить — запись не подтверждает её текущее местоположение."))
    }

    private fun projects() {
        content.addView(title("Проекты: дом, участок, сад"))
        content.addView(paragraph("Создавайте журнал наблюдений, планов и оборудования. Это пока текстовые записи, а не автоматически построенная 3D-модель дома или карта участка."))
        val box = EditText(this).apply {
            hint = "Название проекта и заметки…"
            setText(prefs.getString("projects", "") ?: "")
            minLines = 7; gravity = Gravity.TOP; setTextColor(ink)
        }
        content.addView(box)
        content.addView(button("Сохранить проект") {
            prefs.edit().putString("projects", box.text.toString()).apply()
            Toast.makeText(this, "Проект сохранён", Toast.LENGTH_SHORT).show()
        })
    }

    private fun coach() {
        content.addView(title("Речь и языковая практика"))
        content.addView(paragraph("Режим тренировки: произнесите фразу, посмотрите, как система её распознала, и сравните текст с исходным. Это не точная оценка произношения: распознавание зависит от микрофона, акцента и модели."))
        val target = EditText(this).apply {
            hint = "Введите фразу для тренировки"
            minLines = 2; setTextColor(ink)
        }
        content.addView(target)
        content.addView(button("🎙 Произнести фразу") { startSpeech() })
        content.addView(button("Озвучить пример") {
            val text = target.text.toString().ifBlank { "Здравствуйте! Давайте потренируем чёткую и спокойную речь." }
            if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "coach_sample")
            else Toast.makeText(this, "Синтез речи ещё не готов", Toast.LENGTH_SHORT).show()
        })
        content.addView(paragraph("Будущие режимы: свободный разговор, преподаватель с исправлениями, экзамен с итоговой оценкой. Оценка артикуляции по видео и специализированная поддержка арабского, арамейского, библейского иврита и идиша пока не реализованы."))
    }

    private fun settings() {
        content.addView(title("Настройки и безопасность"))
        content.addView(paragraph("Имя приложения: FamilyOps AI\nВерсия: 0.1.0 — стартовый прототип\nЯзык интерфейса: русский"))
        content.addView(paragraph("Данные заметок сохраняются локально в хранилище приложения. Удаление приложения обычно удаляет эти данные. Экспорт и зашифрованная резервная копия пока не реализованы."))
        content.addView(paragraph("Голосовой ввод использует системный сервис распознавания речи Android; обработка аудио может зависеть от выбранного сервиса и его политики конфиденциальности."))
        content.addView(button("Удалить локальные данные") {
            AlertDialog.Builder(this).setTitle("Удалить данные?")
                .setMessage("Будут удалены локальные заметки, проекты и история диалога.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить") { _, _ ->
                    prefs.edit().clear().apply()
                    Toast.makeText(this, "Локальные данные удалены", Toast.LENGTH_SHORT).show()
                    showTab("Главная")
                }.show()
        })
    }

    override fun onDestroy() {
        if (::tts.isInitialized) { tts.stop(); tts.shutdown() }
        super.onDestroy()
    }
}
