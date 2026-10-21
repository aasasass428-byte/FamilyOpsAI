package com.familyops.ai

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var prefs: SharedPreferences
    private lateinit var content: LinearLayout
    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("familyops", MODE_PRIVATE)
        tts = TextToSpeech(this, this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 30, 20, 20)
            setBackgroundColor(Color.rgb(245, 247, 251))
        }

        val heading = TextView(this).apply {
            text = "FamilyOps AI"
            textSize = 28f
            setTextColor(Color.rgb(35, 65, 120))
        }
        root.addView(heading)

        val description = TextView(this).apply {
            text = "Семейный помощник: заметки, проекты и голос"
            textSize = 15f
            setPadding(0, 10, 0, 20)
        }
        root.addView(description)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        listOf("Диалог", "Память", "Проекты", "Речь", "Настройки").forEach { item ->
            menu.addView(Button(this).apply {
                text = item
                isAllCaps = false
                setOnClickListener { showSection(item) }
            })
        }

        root.addView(menu)
        setContentView(root)
        showSection("Главная")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("ru", "RU")
            ttsReady = true
        }
    }

    private fun showSection(section: String) {
        content.removeAllViews()

        fun addText(value: String) {
            content.addView(TextView(this).apply {
                text = value
                textSize = 16f
                setPadding(0, 10, 0, 10)
            })
        }

        when (section) {
            "Главная" -> {
                addText("Добро пожаловать! Это первая версия FamilyOps AI.")
                addText("Выберите раздел в меню.")
            }

            "Диалог" -> {
                addText("Локальный демонстрационный режим. Настоящий ИИ пока не подключён.")

                val input = EditText(this).apply {
                    hint = "Введите сообщение"
                }
                content.addView(input)

                content.addView(Button(this).apply {
                    text = "Отправить"
                    isAllCaps = false
                    setOnClickListener {
                        val question = input.text.toString().trim()
                        if (question.isNotEmpty()) {
                            val history = prefs.getString("chat", "") ?: ""
                            val reply = "Ваш вопрос сохранён. Для интеллектуальных ответов нужно подключить ИИ-сервис."
                            prefs.edit().putString(
                                "chat",
                                "$history\nВы: $question\nFamilyOps AI: $reply\n"
                            ).apply()
                            showSection("Диалог")
                        }
                    }
                })

                addText(prefs.getString("chat", "История пока пуста.") ?: "")
            }

            "Память", "Проекты" -> {
                val key = if (section == "Память") "memory" else "projects"
                addText(if (section == "Память") {
                    "Записывайте, где находятся вещи и когда это проверено."
                } else {
                    "Записывайте наблюдения и планы для дома, участка и сада."
                })

                val editor = EditText(this).apply {
                    hint = "Введите заметки…"
                    minLines = 5
                    gravity = Gravity.TOP
                    setText(prefs.getString(key, "") ?: "")
                }
                content.addView(editor)

                content.addView(Button(this).apply {
                    text = "Сохранить"
                    isAllCaps = false
                    setOnClickListener {
                        prefs.edit().putString(key, editor.text.toString()).apply()
                        Toast.makeText(
                            this@MainActivity,
                            "Сохранено на устройстве",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }

            "Речь" -> {
                addText("Введите фразу и прослушайте пример произношения.")
                val phrase = EditText(this).apply {
                    hint = "Фраза для тренировки"
                }
                content.addView(phrase)

                content.addView(Button(this).apply {
                    text = "Озвучить"
                    isAllCaps = false
                    setOnClickListener {
                        val value = phrase.text.toString().ifBlank {
                            "Здравствуйте! Давайте потренируем речь."
                        }
                        if (ttsReady) {
                            tts.speak(
                                value,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "practice"
                            )
                        }
                    }
                })

                content.addView(Button(this).apply {
                    text = "Голосовой ввод"
                    isAllCaps = false
                    setOnClickListener { startVoiceInput() }
                })
            }

            "Настройки" -> {
                addText("FamilyOps AI — версия 0.1.0")
                addText("Заметки хранятся локально в приложении.")
                content.addView(Button(this).apply {
                    text = "Удалить локальные данные"
                    isAllCaps = false
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Удалить данные?")
                            .setMessage("История, память и проекты будут удалены.")
                            .setNegativeButton("Отмена", null)
                            .setPositiveButton("Удалить") { _, _ ->
                                prefs.edit().clear().apply()
                                showSection("Настройки")
                            }
                            .show()
                    }
                })
            }
        }
    }

    private fun startVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите")
            }
            startActivityForResult(intent, 100)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Распознавание речи недоступно",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Deprecated("Deprecated by Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val text = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!text.isNullOrBlank()) {
                AlertDialog.Builder(this)
                    .setTitle("Распознанная речь")
                    .setMessage(text)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
