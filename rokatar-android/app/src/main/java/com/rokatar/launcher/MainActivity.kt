package com.rokatar.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray

class MainActivity : Activity() {
    private lateinit var web: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 12, 14)
        window.navigationBarColor = Color.rgb(12, 12, 14)
        web = WebView(this)
        web.setBackgroundColor(Color.rgb(12,12,14))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "Rokatar")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }
    inner class Bridge {
        @JavascriptInterface fun platform() = "android"
        @JavascriptInterface fun listGames(): String {
            val out = JSONArray()
            val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = packageManager.queryIntentActivities(intent, 0)
            for (item in apps) {
                val appIntent = packageManager.getLaunchIntentForPackage(item.activityInfo.packageName) ?: continue
                val obj = org.json.JSONObject()
                obj.put("name", item.loadLabel(packageManager).toString())
                obj.put("packageName", item.activityInfo.packageName)
                out.put(obj)
            }
            return out.toString()
        }
        @JavascriptInterface fun launchGame(packageName: String): Boolean {
            return try {
                val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return false
                startActivity(launch); true
            } catch (_: Exception) { false }
        }
    }
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
