package ru.cityrun.game

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10, 12, 17)
        window.navigationBarColor = Color.rgb(10, 12, 17)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContentView(RoadGameView(this))
    }
}

private class RoadGameView(context: android.content.Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var carX = 0f
    private var roadOffset = 0f
    private var speed = 0f
    private var distance = 0f
    private var stage = 0
    private var steer = 0f
    private var gas = false
    private var brake = false
    private var lastTime = 0L
    private var finished = false
    private val buildings = ArrayList<RectF>()
    private val traffic = floatArrayOf(-0.55f, 0.32f, -0.18f, 0.62f)
    private val trafficY = floatArrayOf(0.22f, 0.48f, 0.72f, 0.92f)

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val scale = w / 480f
        canvas.save()
        canvas.scale(scale, scale)
        val vh = h / scale
        drawWorld(canvas, vh)
        drawHud(canvas)
        drawControls(canvas, vh)
        canvas.restore()
        val now = System.nanoTime()
        val dt = if (lastTime == 0L) 0.016f else ((now - lastTime) / 1e9).toFloat().coerceIn(0f, 0.05f)
        lastTime = now
        update(dt)
        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        if (finished) return
        val target = if (gas) 1f else if (brake) 0.12f else 0.0f
        speed += (target - speed) * dt * 2.4f
        carX = (carX + steer * dt * 0.72f * (0.35f + speed)).coerceIn(-0.78f, 0.78f)
        distance += speed * dt * 95f
        roadOffset = (roadOffset + speed * dt * 520f) % 100f
        for (i in traffic.indices) {
            trafficY[i] += speed * dt * 0.13f
            if (trafficY[i] > 1.12f) trafficY[i] = -0.15f - i * 0.08f
        }
        if (stage == 0 && distance > 850f) stage = 1
        if (stage == 1 && distance > 1850f) { stage = 2; finished = true }
    }

    private fun drawWorld(c: Canvas, vh: Float) {
        c.drawColor(Color.rgb(20, 34, 30))
        p.color = Color.rgb(30, 44, 39)
        c.drawRect(0f, 0f, 480f, vh, p)
        // city blocks
        for (i in 0..7) {
            val y = ((i * 145f + roadOffset * 0.45f) % (vh + 160f)) - 100f
            p.color = if (i % 2 == 0) Color.rgb(47, 53, 59) else Color.rgb(58, 56, 57)
            c.drawRoundRect(12f, y, 105f, y + 105f, 8f, 8f, p)
            c.drawRoundRect(375f, y + 45f, 468f, y + 150f, 8f, 8f, p)
            p.color = Color.rgb(105, 112, 119)
            for (r in 0..2) for (col in 0..2) {
                c.drawRect(23f + col * 24f, y + 15f + r * 25f, 37f + col * 24f, y + 29f + r * 25f, p)
                c.drawRect(386f + col * 24f, y + 60f + r * 25f, 400f + col * 24f, y + 74f + r * 25f, p)
            }
        }
        // road
        p.color = Color.rgb(38, 41, 48); c.drawRect(112f, 0f, 368f, vh, p)
        p.color = Color.rgb(73, 76, 81); c.drawRect(112f, 0f, 119f, vh, p); c.drawRect(361f, 0f, 368f, vh, p)
        p.color = Color.rgb(232, 191, 95)
        c.drawRect(237f, 0f, 240f, vh, p)
        c.drawRect(241f, 0f, 244f, vh, p)
        p.color = Color.rgb(224, 224, 211)
        var y = -100f + roadOffset
        while (y < vh) { c.drawRoundRect(174f, y, 179f, y + 42f, 3f, 3f, p); c.drawRoundRect(301f, y, 306f, y + 42f, 3f, 3f, p); y += 100f }
        // destination / parcel marker
        if (stage == 0) drawMarker(c, 240f, vh * 0.26f, "ЗАБЕРИ ПОСЫЛКУ")
        else drawMarker(c, 240f, vh * 0.26f, "ГАРАЖ")
        // ambient cars
        for (i in traffic.indices) {
            val x = 240f + traffic[i] * 105f
            val cy = trafficY[i] * vh
            drawCar(c, x, cy, 0.78f, if (i % 2 == 0) Color.rgb(48, 107, 174) else Color.rgb(205, 205, 194), false)
        }
        drawCar(c, 240f + carX * 105f, vh * 0.76f, 1f, Color.rgb(197, 35, 43), true)
    }

    private fun drawMarker(c: Canvas, x: Float, y: Float, text: String) {
        p.color = Color.argb(65, 255, 190, 55); c.drawCircle(x, y, 40f, p)
        p.color = Color.rgb(255, 193, 65); p.style = Paint.Style.STROKE; p.strokeWidth = 5f
        c.drawCircle(x, y, 27f, p); p.style = Paint.Style.FILL
        p.textSize = 11f; p.typeface = Typeface.create("sans-serif", Typeface.BOLD); p.textAlign = Paint.Align.CENTER
        c.drawText(text, x, y - 49f, p)
    }

    private fun drawCar(c: Canvas, x: Float, y: Float, s: Float, color: Int, player: Boolean) {
        c.save(); c.translate(x, y); c.scale(s, s)
        p.color = Color.rgb(8, 9, 12)
        c.drawRoundRect(-28f, -40f, -18f, 39f, 5f, 5f, p); c.drawRoundRect(18f, -40f, 28f, 39f, 5f, 5f, p)
        p.color = color; c.drawRoundRect(-23f, -47f, 23f, 47f, 10f, 10f, p)
        p.color = Color.rgb(29, 47, 61); c.drawRoundRect(-17f, -27f, 17f, 4f, 6f, 6f, p)
        p.color = Color.rgb(17, 27, 34); c.drawRoundRect(-17f, 10f, 17f, 30f, 4f, 4f, p)
        p.color = if (player) Color.rgb(255, 239, 168) else Color.rgb(255, 244, 202)
        c.drawRoundRect(-19f, -45f, -8f, -40f, 2f, 2f, p); c.drawRoundRect(8f, -45f, 19f, -40f, 2f, 2f, p)
        p.color = Color.rgb(255, 55, 45); c.drawRect(-18f, 41f, -8f, 46f, p); c.drawRect(8f, 41f, 18f, 46f, p)
        c.restore()
    }

    private fun drawHud(c: Canvas) {
        p.color = Color.argb(225, 12, 15, 22); c.drawRoundRect(16f, 18f, 464f, 118f, 18f, 18f, p)
        p.textAlign = Paint.Align.LEFT; p.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        p.textSize = 23f; p.color = Color.rgb(255, 70, 75); c.drawText("CITY RUN", 34f, 52f, p)
        p.textSize = 13f; p.color = Color.WHITE
        val task = when(stage) { 0 -> "МИССИЯ 1 · НАЙДИ ПОСЫЛКУ"; 1 -> "МИССИЯ 1 · ДОСТАВЬ В ГАРАЖ"; else -> "МИССИЯ ВЫПОЛНЕНА!" }
        c.drawText(task, 34f, 78f, p)
        p.color = Color.rgb(255, 194, 80); c.drawText("СКОРОСТЬ  ${(speed * 120).toInt()} КМ/Ч", 34f, 101f, p)
        p.color = Color.rgb(53, 61, 73); c.drawRoundRect(280f, 88f, 444f, 101f, 6f, 6f, p)
        p.color = Color.rgb(255, 65, 70); c.drawRoundRect(280f, 88f, 280f + 164f * (distance / 1850f).coerceIn(0f,1f), 101f, 6f, 6f, p)
        if (finished) {
            p.color = Color.argb(220, 9, 12, 18); c.drawRoundRect(36f, 250f, 444f, 520f, 24f, 24f, p)
            p.textAlign = Paint.Align.CENTER; p.textSize = 29f; p.color = Color.WHITE
            c.drawText("МИССИЯ ВЫПОЛНЕНА", 240f, 345f, p)
            p.textSize = 16f; p.color = Color.rgb(255, 190, 80)
            c.drawText("Посылка доставлена!", 240f, 385f, p)
            c.drawText("Перезапусти игру для повтора", 240f, 425f, p)
        }
    }

    private fun drawControls(c: Canvas, vh: Float) {
        val y = vh - 112f
        drawButton(c, 68f, y, 48f, "◀")
        drawButton(c, 172f, y, 48f, "▶")
        drawButton(c, 365f, y - 20f, 62f, "ГАЗ")
        drawButton(c, 365f, y + 72f, 40f, "ТОРМ")
    }

    private fun drawButton(c: Canvas, x: Float, y: Float, r: Float, text: String) {
        p.color = Color.argb(190, 25, 31, 42); c.drawCircle(x, y, r, p)
        p.color = Color.rgb(255, 74, 78); p.style = Paint.Style.STROKE; p.strokeWidth = 3f
        c.drawCircle(x, y, r - 3f, p); p.style = Paint.Style.FILL
        p.textAlign = Paint.Align.CENTER; p.textSize = if (text.length > 2) 13f else 28f
        p.typeface = Typeface.DEFAULT_BOLD; p.color = Color.WHITE
        c.drawText(text, x, y + 5f, p)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x * 480f / width
        val y = event.y * 480f / width
        val vh = height * 480f / width
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                steer = when { x < 120f -> -1f; x < 240f -> 1f; else -> 0f }
                gas = x > 280f && y > vh - 200f && y < vh - 45f && y < vh - 55f
                brake = x > 280f && y >= vh - 55f
                if (x > 280f && y > vh - 210f && y < vh - 115f) gas = true
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                steer = 0f; gas = false; brake = false
                return true
            }
        }
        return true
    }
}
