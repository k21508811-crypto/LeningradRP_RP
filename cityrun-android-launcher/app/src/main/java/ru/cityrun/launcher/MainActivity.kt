package ru.cityrun.launcher

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var play: Button
    private var licensed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 12, 15)
        window.navigationBarColor = Color.rgb(12, 12, 15)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(12, 12, 15))
        }
        val logo = TextView(this).apply {
            text = "CITY RUN"
            textSize = 34f
            setTextColor(Color.rgb(235, 55, 55))
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "BETA LAUNCHER • ANDROID TEST"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            text = "Проверка лицензии не выполнена"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 34, 0, 24)
        }
        val check = Button(this).apply {
            text = "ПРОВЕРИТЬ ЛИЦЕНЗИЮ"
            setOnClickListener { checkLicense() }
        }
        play = Button(this).apply {
            text = "ЗАПУСТИТЬ БЕТА-ВЕРСИЮ"
            isEnabled = false
            setOnClickListener {
                status.text = "Android-версия игры пока не собрана. Текущая City Run доступна для Windows."
            }
        }
        val note = TextView(this).apply {
            text = "Лицензия: 3 года с подтверждённой оплаты. Одноразовый платёж. Данные банковских карт приложение не запрашивает и не хранит."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 0)
        }
        root.addView(logo)
        root.addView(subtitle)
        root.addView(status)
        root.addView(check, LinearLayout.LayoutParams(-1, -2))
        root.addView(play, LinearLayout.LayoutParams(-1, -2))
        root.addView(note)
        setContentView(root)
        checkLicense()
    }

    private fun checkLicense() {
        val base = BuildConfig.LICENSE_API_BASE_URL.trim().trimEnd('/')
        if (base.isEmpty()) {
            licensed = false
            play.isEnabled = false
            status.text = "ТЕСТОВЫЙ РЕЖИМ: сервер лицензий ещё не подключён. Запуск закрыт."
            return
        }
        status.text = "Проверяем лицензию…"
        play.isEnabled = false
        thread {
            var message = "Не удалось проверить лицензию"
            var active = false
            try {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val connection = (URL("$base/v1/license/status?device_id=$deviceId").openConnection() as HttpURLConnection)
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.setRequestProperty("Accept", "application/json")
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() } ?: "{}"
                val json = JSONObject(body)
                active = code in 200..299 && json.optBoolean("active", false)
                message = if (active) {
                    "ЛИЦЕНЗИЯ АКТИВНА • до " + json.optString("expires_at", "даты окончания")
                } else {
                    "Лицензия не найдена или истекла"
                }
                connection.disconnect()
            } catch (_: Exception) {
                message = "Сервер лицензий недоступен. Запуск заблокирован."
            }
            runOnUiThread {
                licensed = active
                status.text = message
                play.isEnabled = licensed
            }
        }
    }
}
