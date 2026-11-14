package ru.leningrad.rp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val bg = Color.rgb(12, 14, 20)
    private val panel = Color.rgb(25, 29, 39)
    private val red = Color.rgb(205, 48, 55)
    private lateinit var status: TextView
    private lateinit var bridgeInput: EditText
    private lateinit var activateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showLogin()
    }

    private fun showLogin() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(28, 30, 28, 30)
            setBackgroundColor(bg)
        }
        root.addView(label("LENINGRAD RP", 29, Color.WHITE, true))
        root.addView(label("BOT CONTROL", 15, red, true))
        root.addView(space(28))
        root.addView(label("ВХОД В ПРИЛОЖЕНИЕ", 17, Color.WHITE, true))
        root.addView(space(12))
        val login = edit("Логин")
        val password = edit("Пароль").apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(login, matchWrap())
        root.addView(space(8))
        root.addView(password, matchWrap())
        root.addView(space(18))
        root.addView(actionButton("ВОЙТИ", red) {
            val userOk = login.text.toString().trim() == "NewBot"
            val passOk = sha256(password.text.toString()) == PASSWORD_SHA256
            if (userOk && passOk) showDashboard()
            else {
                password.text.clear()
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(space(18))
        root.addView(label("Доступ только после авторизации", 12, Color.LTGRAY))
        setContentView(root)
    }

    private fun showDashboard() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 24)
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(label("LENINGRAD RP", 27, Color.WHITE, true))
        content.addView(label("ПАНЕЛЬ УПРАВЛЕНИЯ DISCORD-БОТОМ", 13, red, true))
        content.addView(space(22))
        content.addView(label("СЕТЬ И ПОДКЛЮЧЕНИЕ", 17, Color.WHITE, true))
        status = label("Нажмите «Активировать бота», чтобы проверить сеть телефона.", 14, Color.LTGRAY)
        content.addView(status)
        content.addView(space(14))
        content.addView(label("HTTPS-адрес моста бота (если настроен)", 13, Color.LTGRAY))
        bridgeInput = edit("https://ваш-домен.example")
        bridgeInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
        content.addView(bridgeInput, matchWrap())
        content.addView(space(10))
        activateButton = actionButton("АКТИВИРОВАТЬ БОТА", red) { activateBot() }
        content.addView(activateButton)
        content.addView(space(18))
        content.addView(label("После проверки приложение покажет тип сети, локальный IP и публичный IP. Для реального соединения Discord-боту нужен HTTPS API-мост с маршрутом /activate.", 12, Color.rgb(155, 164, 180)))
        content.addView(space(18))
        content.addView(actionButton("ВЫЙТИ ИЗ АККАУНТА", Color.rgb(65, 70, 82)) { showLogin() })
        setContentView(root)
    }

    private fun activateBot() {
        activateButton.isEnabled = false
        status.text = "Проверяем подключение к сети…"
        val bridge = bridgeInput.text.toString().trim().trimEnd('/')
        worker.execute {
            val network = networkInfo()
            val publicIp = fetchPublicIp()
            val summary = "Тип сети: ${network.first}\nЛокальный IP: ${network.second}\nПубличный IP: ${publicIp ?: "не определён"}"
            var message = summary
            if (bridge.isEmpty()) {
                message += "\n\nСеть обнаружена. Адрес HTTPS-моста не задан, поэтому бот пока не подключён."
            } else if (!bridge.startsWith("https://")) {
                message += "\n\nДля безопасности укажите HTTPS-адрес моста."
            } else {
                val response = postActivation(bridge, network.first, network.second, publicIp)
                message += if (response == null) "\n\nНе удалось связаться с мостом бота. Проверьте адрес и доступность API."
                else "\n\nОтвет моста: " + response.optString("message", response.toString())
            }
            runOnUiThread {
                status.text = message
                activateButton.isEnabled = true
            }
        }
    }

    private fun networkInfo(): Pair<String, String> {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return Pair("Нет подключения", "—")
        val caps = cm.getNetworkCapabilities(net)
        val type = when {
            caps == null -> "Неизвестная сеть"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Мобильный интернет"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Другая сеть"
        }
        val localIp = cm.getLinkProperties(net)?.linkAddresses
            ?.mapNotNull { it.address.hostAddress }
            ?.firstOrNull { it.contains('.') && !it.startsWith("127.") } ?: "не определён"
        return Pair(type, localIp)
    }

    private fun fetchPublicIp(): String? = try {
        val conn = (URL("https://api.ipify.org").openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
        }
        val value = conn.inputStream.bufferedReader().use { it.readText().trim() }
        conn.disconnect()
        value.takeIf { it.length <= 64 }
    } catch (_: Exception) { null }

    private fun postActivation(base: String, type: String, localIp: String, publicIp: String?): JSONObject? = try {
        val url = URL(base + "/activate")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 12000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        val body = JSONObject()
            .put("network_type", type)
            .put("local_ip", localIp)
            .put("public_ip", publicIp ?: JSONObject.NULL)
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        conn.disconnect()
        JSONObject(text)
    } catch (_: Exception) { null }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun edit(hintText: String) = EditText(this).apply {
        hint = hintText
        setSingleLine(true)
        setTextColor(Color.WHITE)
        setHintTextColor(Color.GRAY)
        setPadding(16, 14, 16, 14)
        setBackgroundColor(panel)
    }

    private fun actionButton(title: String, color: Int, click: () -> Unit) = Button(this).apply {
        text = title
        setTextColor(Color.WHITE)
        setBackgroundColor(color)
        isAllCaps = false
        textSize = 14f
        setOnClickListener { click() }
    }

    private fun label(value: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(0, 5, 0, 5)
    }

    private fun space(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, height)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2).apply {
        bottomMargin = 6
    }

    companion object {
        // SHA-256 of the temporary test password "NewBot".
        private const val PASSWORD_SHA256 = "3ad1c02a5bea15fd2371ad1e62ec0b8c1c718fc346db630872a4fe117b3bdd76"
    }
}
