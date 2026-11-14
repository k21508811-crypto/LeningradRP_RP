package ru.cityrun.launcher

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var licenseStatus: TextView
    private lateinit var gameStatus: TextView
    private lateinit var playButton: Button
    private lateinit var checkButton: Button
    private var licensed = false
    private val gamePackage = "ru.cityrun.game"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9, 11, 16)
        window.navigationBarColor = Color.rgb(9, 11, 16)
        window.decorView.systemUiVisibility = 0

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(26))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(9, 11, 16), Color.rgb(22, 12, 18), Color.rgb(10, 13, 19)))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(page) }

        page.addView(label("CITY RUN", 34, Color.rgb(255, 65, 65), true, Gravity.START))
        page.addView(label("MOBILE • TEST BUILD", 11, Color.rgb(155, 164, 178), true, Gravity.START).apply {
            letterSpacing = 0.18f
            setPadding(0, dp(3), 0, dp(20))
        })

        val hero = card()
        hero.addView(label("ГОРОД ЖДЁТ ТЕБЯ", 23, Color.WHITE, true, Gravity.START))
        hero.addView(label("Тестовая мобильная версия City Run. Садись за руль, забери посылку и доставь её в гараж.", 14,
            Color.rgb(190, 198, 210), false, Gravity.START).apply { setPadding(0, dp(9), 0, dp(15)) })
        hero.addView(pill("ANDROID BETA  •  v0.2.0"))
        page.addView(hero, marginParams(bottom = 14))

        val licenseCard = card()
        licenseCard.addView(label("ЛИЦЕНЗИЯ", 12, Color.rgb(255, 90, 90), true, Gravity.START))
        licenseStatus = label("Проверяем состояние…", 15, Color.WHITE, true, Gravity.START).apply {
            setPadding(0, dp(9), 0, dp(14))
        }
        licenseCard.addView(licenseStatus)
        checkButton = actionButton("ПРОВЕРИТЬ ЛИЦЕНЗИЮ", false) { checkLicense() }
        licenseCard.addView(checkButton, fullWidth())
        page.addView(licenseCard, marginParams(bottom = 14))

        val gameCard = card()
        gameCard.addView(label("ИГРА", 12, Color.rgb(255, 90, 90), true, Gravity.START))
        gameStatus = label("Проверяем установку игры…", 14, Color.rgb(190, 198, 210), false, Gravity.START)
        gameStatus.setPadding(0, dp(8), 0, dp(14))
        gameCard.addView(gameStatus)
        playButton = actionButton("ИГРАТЬ", true) { launchGame() }
        playButton.isEnabled = false
        gameCard.addView(playButton, fullWidth())
        page.addView(gameCard, marginParams(bottom = 14))

        val info = card()
        info.addView(label("ЧТО ВНУТРИ", 12, Color.rgb(255, 90, 90), true, Gravity.START))
        info.addView(label("• Сенсорное управление автомобилем\n• Небольшой 3D-город в тестовом стиле\n• Одна миссия: забрать и доставить посылку\n• Отдельный APK игры", 14,
            Color.rgb(205, 211, 222), false, Gravity.START).apply { setPadding(0, dp(10), 0, dp(8)) })
        page.addView(info, marginParams(bottom = 14))

        page.addView(label("Лицензия проверяется сервером. Тестовый режим не подтверждает оплату. Приложение не запрашивает и не хранит данные банковских карт.", 11,
            Color.rgb(125, 135, 151), false, Gravity.START).apply { setPadding(dp(3), dp(4), dp(3), 0) })

        setContentView(scroll)
        refreshGameState()
        checkLicense()
    }

    private fun checkLicense() {
        val base = BuildConfig.LICENSE_API_BASE_URL.trim().trimEnd('/')
        if (base.isEmpty()) {
            licensed = false
            licenseStatus.text = "ТЕСТОВЫЙ РЕЖИМ · сервер лицензий не подключён"
            playButton.isEnabled = false
            return
        }
        if (!base.startsWith("https://", true)) {
            licensed = false
            licenseStatus.text = "Ошибка настройки: разрешён только HTTPS"
            playButton.isEnabled = false
            return
        }
        checkButton.isEnabled = false
        licenseStatus.text = "Связываемся с сервером…"
        playButton.isEnabled = false
        worker.execute {
            var active = false
            var message = "Не удалось проверить лицензию"
            var connection: HttpURLConnection? = null
            try {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val url = URL("$base/v1/license/status?device_id=${Uri.encode(deviceId)}")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/json")
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
                val json = JSONObject(body)
                active = code in 200..299 && json.optBoolean("active", false)
                message = if (active) "ЛИЦЕНЗИЯ АКТИВНА · до ${json.optString("expires_at", "не указано")}"
                    else "Лицензия не найдена или срок истёк"
            } catch (_: Exception) {
                message = "Сервер недоступен · запуск заблокирован"
            } finally {
                connection?.disconnect()
            }
            runOnUiThread {
                licensed = active
                licenseStatus.text = message
                checkButton.isEnabled = true
                playButton.isEnabled = licensed && isGameInstalled()
                refreshGameState()
            }
        }
    }

    private fun launchGame() {
        if (!licensed) {
            licenseStatus.text = "Сначала проверь активную лицензию"
            return
        }
        val intent = packageManager.getLaunchIntentForPackage(gamePackage)
        if (intent == null) {
            gameStatus.text = "Игра не установлена. Установи CityRun-Mobile-Test.apk."
            return
        }
        startActivity(intent)
    }

    private fun refreshGameState() {
        val installed = isGameInstalled()
        gameStatus.text = if (installed) "Игра установлена · готова к запуску"
            else "Игра пока не установлена · нужен отдельный APK"
        if (::playButton.isInitialized) playButton.isEnabled = licensed && installed
    }

    private fun isGameInstalled(): Boolean = try {
        packageManager.getPackageInfo(gamePackage, 0)
        true
    } catch (_: Exception) { false }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(17), dp(17), dp(17), dp(17))
        background = rounded(Color.rgb(25, 29, 38), dp(18), Color.rgb(48, 54, 67))
    }

    private fun pill(text: String): TextView = label(text, 11, Color.rgb(255, 132, 132), true, Gravity.CENTER).apply {
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = rounded(Color.rgb(57, 25, 31), dp(20))
    }

    private fun actionButton(text: String, secondary: Boolean, click: () -> Unit): Button = Button(this).apply {
        this.text = text
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        background = rounded(if (secondary) Color.rgb(170, 35, 45) else Color.rgb(49, 57, 70), dp(13))
        minHeight = dp(52)
        setOnClickListener { click() }
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean, gravity: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            this.gravity = gravity
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun fullWidth() = LinearLayout.LayoutParams(-1, dp(52))
    private fun marginParams(bottom: Int) = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(bottom) }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
