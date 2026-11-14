package ru.leningrad.virusprank

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var seconds = 60
    private var running = false
    private var overlayView: Button? = null
    private var windowManager: WindowManager? = null
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var timer: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(18, 0, 0)
        window.navigationBarColor = Color.rgb(18, 0, 0)
        showAgreement()
    }

    private fun showAgreement() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(15, 10, 12))
        }
        root.addView(text("VIRUS 666", 32, true), match())
        root.addView(text("Это шуточная имитация вируса. Приложение не заражает телефон, не блокирует выход, не удаляет данные и не работает скрытно.", 17, false), match())
        root.addView(text("Во время 60-секундного эффекта будут медленно меняться цвета и появляться вымышленные предупреждения. В любой момент можно нажать «СТОП» или выйти кнопкой Android «Назад». Эффект не предназначен для людей с фоточувствительностью.", 14, false), match())
        val agree = Button(this).apply {
            text = "СОГЛАСЕН — ПРОДОЛЖИТЬ"
            isAllCaps = false
            setOnClickListener { requestOverlayOrStart() }
        }
        root.addView(agree, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(24) })
        setContentView(root)
    }

    private fun requestOverlayOrStart() {
        if (!Settings.canDrawOverlays(this)) {
            val info = android.app.AlertDialog.Builder(this)
                .setTitle("Плавающая кнопка СТОП")
                .setMessage("Можно разрешить небольшую кнопку «СТОП» поверх окон, чтобы быстро выключить эффект даже после переключения приложения. Это необязательно.")
                .setNegativeButton("Без разрешения") { _, _ -> showReady() }
                .setPositiveButton("Открыть настройки") { _, _ ->
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), 77)
                }.create()
            info.show()
        } else showReady()
    }

    @Deprecated("Uses Android settings activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 77) showReady()
    }

    private fun showReady() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(22), dp(22), dp(22), dp(22))
            setBackgroundColor(Color.rgb(20, 0, 0))
        }
        root.addView(text("⚠ VIRUS 666 ⚠", 29, true), match())
        status = text("СИСТЕМА ГОТОВА К СИМУЛЯЦИИ", 16, true)
        root.addView(status, match())
        timer = text("01:00", 42, true)
        root.addView(timer, match())
        startButton = Button(this).apply {
            text = "ЗАПУСТИТЬ ПРАНК"
            isAllCaps = false
            setOnClickListener { startPrank() }
        }
        root.addView(startButton, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(20) })
        val stop = Button(this).apply {
            text = "СТОП / ВЫХОД"
            isAllCaps = false
            setOnClickListener { stopPrank("Эффект остановлен.") }
        }
        root.addView(stop, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(10) })
        setContentView(root)
    }

    private fun startPrank() {
        if (running) return
        running = true
        seconds = 60
        startButton.isEnabled = false
        showOverlayStopButton()
        tick()
    }

    private fun tick() {
        if (!running) return
        val dark = seconds % 2 == 0
        root.setBackgroundColor(if (dark) Color.rgb(20, 0, 0) else Color.rgb(55, 0, 0))
        status.text = when (seconds % 4) {
            0 -> "ОБНАРУЖЕНА УГРОЗА… (ИМИТАЦИЯ)"
            1 -> "СКАНИРОВАНИЕ ФАЙЛОВ…"
            2 -> "ЗАРАЖЕНИЕ: ${100 - seconds}% (ФЕЙК)"
            else -> "НЕ ПАНИКУЙ — ЭТО ТОЛЬКО ПРАНК"
        }
        timer.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
        if (seconds <= 0) {
            finishPrank()
            return
        }
        seconds--
        handler.postDelayed({ tick() }, 1200L)
    }

    private fun finishPrank() {
        running = false
        handler.removeCallbacksAndMessages(null)
        removeOverlayStopButton()
        root.setBackgroundColor(Color.rgb(8, 35, 18))
        status.text = "ЭТО ПРАНК 😂"
        timer.text = "ТВОЙ ТЕЛЕФОН В ПОРЯДКЕ"
        startButton.text = "УДАЛИТЬ ПРИЛОЖЕНИЕ"
        startButton.isEnabled = true
        startButton.setOnClickListener { requestUninstall() }
    }

    private fun stopPrank(message: String) {
        running = false
        handler.removeCallbacksAndMessages(null)
        removeOverlayStopButton()
        if (::status.isInitialized) status.text = message
        if (::timer.isInitialized) timer.text = "СТОП"
        if (::startButton.isInitialized) startButton.isEnabled = true
        root.setBackgroundColor(Color.rgb(15, 10, 12))
    }

    private fun showOverlayStopButton() {
        if (!Settings.canDrawOverlays(this)) return
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val button = Button(this).apply {
                text = "СТОП ✕"
                isAllCaps = false
                setOnClickListener { stopPrank("Эффект остановлен плавающей кнопкой.") }
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = dp(12); y = dp(90)
            }
            windowManager?.addView(button, params)
            overlayView = button
        } catch (_: Exception) {
            Toast.makeText(this, "Плавающая кнопка недоступна — используй СТОП в приложении.", Toast.LENGTH_LONG).show()
        }
    }

    private fun removeOverlayStopButton() {
        try { overlayView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        overlayView = null
    }

    private fun requestUninstall() {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
        try { startActivity(intent) } catch (_: Exception) {
            Toast.makeText(this, "Удалить можно в настройках Android → Приложения.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (running) stopPrank("Ты остановил пранк кнопкой «Назад».")
        super.onBackPressed()
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        removeOverlayStopButton()
        super.onDestroy()
    }

    private fun text(value: String, size: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        if (bold) typeface = Typeface.DEFAULT_BOLD
        setPadding(0, dp(12), 0, dp(12))
    }

    private fun match() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
