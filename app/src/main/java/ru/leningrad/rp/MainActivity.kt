package ru.leningrad.rp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.SafeBrowsingResponse
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = false
        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.setSupportMultipleWindows(false)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                val host = uri.host?.lowercase() ?: return true

                if (uri.scheme == "file" && uri.path?.startsWith("/android_asset/") == true) {
                    return false
                }

                if (uri.scheme == "https" && host == "play.roblox.com") {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    return true
                }

                val allowedHost = host == "yandex.ru" ||
                    host.endsWith(".yandex.ru") ||
                    host == "proverka29.ru" ||
                    host.endsWith(".proverka29.ru")

                if (uri.scheme == "https" && allowedHost) {
                    return false
                }

                if (uri.scheme == "https") {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                return true
            }
        }

        setContentView(webView)
        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }
}