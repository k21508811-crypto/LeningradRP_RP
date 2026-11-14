package ru.siteguard.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var siteInput: EditText
    private lateinit var chatInput: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10, 14, 24)
        window.navigationBarColor = Color.rgb(10, 14, 24)
        buildScreen()
    }

    private fun buildScreen() {
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 28, 22, 24); setBackgroundColor(Color.rgb(10, 14, 24)) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(label("SITEGUARD", 27, Color.WHITE, true))
        content.addView(label("Проверка сайтов и ИИ-помощник", 14, Color.rgb(157, 170, 192)))
        content.addView(space(22))
        content.addView(actionButton("Войти через Яндекс", Color.rgb(255, 204, 0), Color.BLACK) {
            AlertDialog.Builder(this).setTitle("Яндекс ID")
                .setMessage("Вход подключается через официальный OAuth Яндекса. Сначала нужно создать OAuth-приложение и настроить серверный callback. Инструкция — в README проекта.")
                .setPositiveButton("Понятно", null).show()
        })
        content.addView(space(22))
        content.addView(label("ПРОВЕРКА САЙТА", 18, Color.WHITE, true))
        siteInput = edit("https://example.com")
        content.addView(siteInput, matchWrap())
        content.addView(space(10))
        content.addView(actionButton("Проверить сайт", Color.rgb(69, 125, 255), Color.WHITE) { scanSite() })
        status = label("Готов к проверке. Введите адрес сайта.", 14, Color.rgb(160, 180, 205))
        content.addView(status)
        result = label("", 14, Color.WHITE)
        content.addView(result)
        content.addView(space(24))
        content.addView(label("ИИ-ПОМОЩНИК", 18, Color.WHITE, true))
        content.addView(label("Задайте вопрос о сайте или результатах проверки.", 13, Color.rgb(157, 170, 192)))
        chatInput = edit("Напишите вопрос...")
        content.addView(chatInput, matchWrap())
        content.addView(space(10))
        content.addView(actionButton("Спросить ИИ", Color.rgb(42, 190, 145), Color.BLACK) { askAssistant() })
        content.addView(space(12))
        content.addView(label("Помощник подключается через серверный API Яндекса. Приложение не запрашивает и не хранит пароль Яндекса.", 12, Color.rgb(125, 140, 164)))
        setContentView(page)
    }

    private fun scanSite() {
        val raw = siteInput.text.toString().trim()
        val normalized = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
        val uri = try { URL(normalized) } catch (_: Exception) { status.text = "Введите корректный адрес сайта."; return }
        if (uri.protocol != "https" && uri.protocol != "http") { status.text = "Поддерживаются только HTTP и HTTPS адреса."; return }
        val base = BuildConfigField.API_BASE_URL
        if (base.isBlank()) {
            status.text = "Адрес принят: ${uri.host}"
            result.text = "Сервер проверки ещё не настроен. Сейчас проверен только формат адреса — это НЕ проверка на вирусы или фишинг."
            return
        }
        status.text = "Проверяем ${uri.host}…"
        worker.execute {
            val response = postJson(base, "/api/scan", JSONObject().put("url", normalized))
            runOnUiThread { if (response == null) status.text = "Не удалось связаться с сервером."
                else { status.text = "Проверка завершена"; result.text = response.optString("report", response.toString()) } }
        }
    }

    private fun askAssistant() {
        val question = chatInput.text.toString().trim()
        if (question.isEmpty()) { Toast.makeText(this, "Напишите вопрос", Toast.LENGTH_SHORT).show(); return }
        val base = BuildConfigField.API_BASE_URL
        if (base.isBlank()) { result.text = "ИИ пока не подключён. Нужен серверный API Яндекса; ключи нельзя хранить внутри APK."; return }
        status.text = "ИИ готовит ответ…"
        worker.execute {
            val response = postJson(base, "/api/chat", JSONObject().put("message", question))
            runOnUiThread { status.text = if (response == null) "Ошибка соединения с ИИ" else "Ответ ИИ"
                result.text = response?.optString("answer", response.toString()) ?: "Проверьте подключение сервера." }
        }
    }

    private fun postJson(base: String, path: String, body: JSONObject): JSONObject? = try {
        val conn = (URL(base.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 12000; readTimeout = 20000; doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val data = stream.bufferedReader().use { it.readText() }; conn.disconnect(); JSONObject(data)
    } catch (_: Exception) { null }

    private fun edit(hintText: String) = EditText(this).apply {
        hint = hintText; setSingleLine(true); setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
        setPadding(16, 14, 16, 14); setBackgroundColor(Color.rgb(27, 35, 51))
    }
    private fun actionButton(title: String, bg: Int, fg: Int, click: () -> Unit) = Button(this).apply {
        text = title; setTextColor(fg); setBackgroundColor(bg); isAllCaps = false; textSize = 15f; setOnClickListener { click() }
    }
    private fun label(value: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD); setPadding(0, 5, 0, 5)
    }
    private fun space(height: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, height) }
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 6 }
}
private object BuildConfigField {
    // Set the HTTPS backend URL here before release; keep API secrets on the server.
    const val API_BASE_URL = ""
}
