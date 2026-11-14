package ru.leningrad.aistudio

import android.app.Activity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.content.Intent
import android.view.Gravity
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {
    private lateinit var prompt: EditText
    private lateinit var apiKey: EditText
    private lateinit var model: Spinner
    private lateinit var preview: ImageView
    private lateinit var status: TextView
    private lateinit var generate: Button
    private var image: Bitmap? = null
    private val models = listOf("black-forest-labs/flux.1-schnell", "google/gemini-2.5-flash-image", "openai/gpt-image-1-mini")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13,15,23)
        window.navigationBarColor = Color.rgb(13,15,23)
        val root = LinearLayout(this).apply { orientation=1; setPadding(dp(20),dp(18),dp(20),dp(18)); setBackgroundColor(Color.rgb(13,15,23)) }
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation=1 }
        body.addView(label("✦ LENINGRAD AI STUDIO",22,true), match())
        body.addView(label("Превращай идеи в изображения",14,false), match())
        body.addView(label("ОПИШИ ИЗОБРАЖЕНИЕ",12,true), match())
        prompt = EditText(this).apply {
            hint="Например: уютный домик в заснеженном лесу, кинематографичный свет..."
            setTextColor(Color.WHITE); setHintTextColor(0xFF9DA8BE.toInt()); textSize=15f
            minLines=4; maxLines=6; gravity=Gravity.TOP; setPadding(dp(13),dp(12),dp(13),dp(12))
            setBackgroundColor(0xFF191D2A.toInt())
        }
        body.addView(prompt, LinearLayout.LayoutParams(-1,dp(125)).apply { topMargin=dp(8); bottomMargin=dp(16) })
        body.addView(label("МОДЕЛЬ",12,true),match())
        model = Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,models) }
        body.addView(model,LinearLayout.LayoutParams(-1,dp(50)).apply { bottomMargin=dp(16) })
        body.addView(label("API-КЛЮЧ POLLINATIONS",12,true),match())
        apiKey = EditText(this).apply {
            hint="Вставь свой ключ sk_..."; setTextColor(Color.WHITE); setHintTextColor(0xFF9DA8BE.toInt())
            inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true); setPadding(dp(13),dp(10),dp(13),dp(10)); setBackgroundColor(0xFF191D2A.toInt())
        }
        body.addView(apiKey,LinearLayout.LayoutParams(-1,dp(50)).apply { topMargin=dp(8) })
        body.addView(label("Ключ не вшит в APK и не сохраняется приложением.",11,false),match())
        generate=Button(this).apply {
            text="✦  СГЕНЕРИРОВАТЬ"; isAllCaps=false; setTextColor(Color.WHITE)
            setBackgroundColor(0xFF7549D8.toInt()); setOnClickListener { generateImage() }
        }
        body.addView(generate,LinearLayout.LayoutParams(-1,dp(54)).apply { topMargin=dp(14) })
        status=label("Готов к созданию изображения",13,false).apply { gravity=Gravity.CENTER }
        body.addView(status,match())
        preview=ImageView(this).apply { setBackgroundColor(0xFF191D2A.toInt()); scaleType=ImageView.ScaleType.FIT_CENTER; contentDescription="Созданное изображение" }
        body.addView(preview,LinearLayout.LayoutParams(-1,dp(320)).apply { topMargin=dp(8); bottomMargin=dp(10) })
        body.addView(Button(this).apply {
            text="СОХРАНИТЬ ИЗОБРАЖЕНИЕ"; isAllCaps=false; setTextColor(Color.WHITE); setBackgroundColor(0xFF303A50.toInt())
            setOnClickListener { saveImage() }
        },LinearLayout.LayoutParams(-1,dp(50)))
        scroll.addView(body); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f)); setContentView(root)
    }

    private fun generateImage() {
        val description=prompt.text.toString().trim()
        val key=apiKey.text.toString().trim()
        if(description.isEmpty()){prompt.error="Напиши, что нарисовать";return}
        if(key.isEmpty()){apiKey.error="Введи API-ключ";return}
        generate.isEnabled=false; status.text="Генерируем изображение..."
        val selected=models[model.selectedItemPosition]
        Thread {
            var conn:HttpURLConnection?=null
            try {
                val encoded=URLEncoder.encode(description,"UTF-8").replace("+","%20")
                val modelArg=URLEncoder.encode(selected,"UTF-8")
                conn=(URL("https://gen.pollinations.ai/image/$encoded?model=$modelArg&width=1024&height=1024").openConnection() as HttpURLConnection).apply {
                    connectTimeout=30000; readTimeout=180000; requestMethod="GET"
                    setRequestProperty("Authorization","Bearer $key"); setRequestProperty("Accept","image/*")
                }
                val code=conn.responseCode
                if(code !in 200..299) {
                    val detail=try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch(_:Exception){null}
                    throw Exception("HTTP $code. " + (detail?.take(180) ?: "Проверь ключ и баланс API."))
                }
                val result=conn.inputStream.use { BitmapFactory.decodeStream(it) } ?: throw Exception("Сервис не вернул изображение.")
                runOnUiThread { image=result; preview.setImageBitmap(result); status.text="Готово! Картинка создана."; generate.isEnabled=true }
            } catch(e:Exception) {
                runOnUiThread { status.text="Ошибка: " + (e.message ?: "сбой соединения"); generate.isEnabled=true }
            } finally { conn?.disconnect() }
        }.start()
    }

    private fun saveImage() {
        if(image==null){status.text="Сначала создай изображение.";return}
        val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type="image/png"
            putExtra(Intent.EXTRA_TITLE,"Leningrad-AI-image.png")
        }
        startActivityForResult(intent,41)
    }

    @Deprecated("Uses Android system document picker")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==41 && resultCode==RESULT_OK) {
            val uri=data?.data ?: return
            try {
                contentResolver.openOutputStream(uri)?.use { image?.compress(Bitmap.CompressFormat.PNG,100,it) }
                status.text="Изображение сохранено."
            } catch(e:Exception) { status.text="Не удалось сохранить: " + e.message }
        }
    }

    private fun label(value:String,size:Int,bold:Boolean)=TextView(this).apply {
        text=value; textSize=size.toFloat(); setTextColor(if(size<=13) 0xFFB2BCD0.toInt() else Color.WHITE)
        if(bold) typeface=Typeface.DEFAULT_BOLD
        setPadding(0,dp(8),0,dp(8))
    }
    private fun match()=LinearLayout.LayoutParams(-1,-2)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}