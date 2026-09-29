package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

class CinemaWebInterface(
    private val onTitleDetected: (String) -> Unit,
    private val onVideoPlaying: () -> Unit
) {
    @JavascriptInterface
    fun postDramaTitle(title: String) {
        if (title.isNotBlank()) {
            onTitleDetected(title)
        }
    }

    @JavascriptInterface
    fun notifyVideoStarted() {
        onVideoPlaying()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CinemaWebView(
    url: String,
    modifier: Modifier = Modifier,
    onProgressUpdate: (Int) -> Unit = {},
    onPageTitleReceived: (String) -> Unit = {},
    onAdBlocked: () -> Unit = {},
    webViewInstanceHolder: (WebView) -> Unit = {}
) {
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Intercept back button when in fullscreen custom view or when webview can go back
    BackHandler(enabled = customView != null || (webViewRef?.canGoBack() == true)) {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
        } else if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 ReelAI/1.0"
                    }

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    val jsInterface = CinemaWebInterface(
                        onTitleDetected = onPageTitleReceived,
                        onVideoPlaying = { }
                    )
                    addJavascriptInterface(jsInterface, "AndroidCinema")

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            onProgressUpdate(newProgress)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            if (!title.isNullOrBlank() && !title.contains("Just a moment") && !title.contains("Cloudflare")) {
                                onPageTitleReceived(title)
                            }
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            customView = view
                            customViewCallback = callback
                        }

                        override fun onHideCustomView() {
                            customView = null
                            customViewCallback = null
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val destUrl = request?.url?.toString() ?: return false
                            // Block obvious malicious popunder / redirect schemes
                            if (destUrl.startsWith("intent:") ||
                                destUrl.startsWith("market:") ||
                                destUrl.startsWith("tg:") ||
                                destUrl.contains("syndication") ||
                                destUrl.contains("onclick") ||
                                destUrl.contains("popcash") ||
                                destUrl.contains("bet") ||
                                destUrl.contains("adsterra")
                            ) {
                                onAdBlocked()
                                return true
                            }
                            return false
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString()?.lowercase() ?: ""
                            // Filter known ad networks to keep the drama viewer pristine
                            if (reqUrl.contains("googlesyndication") ||
                                reqUrl.contains("adservice.google") ||
                                reqUrl.contains("adnxs") ||
                                reqUrl.contains("adkeeper") ||
                                reqUrl.contains("popads") ||
                                reqUrl.contains("propellerads") ||
                                reqUrl.contains("exoclick")
                            ) {
                                onAdBlocked()
                                return WebResourceResponse("text/plain", "UTF-8", null)
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            onProgressUpdate(15)
                        }

                        override fun onPageFinished(view: WebView?, finishUrl: String?) {
                            super.onPageFinished(view, finishUrl)
                            onProgressUpdate(100)

                            // Inject clean cinema CSS and title extractor
                            val cleanScript = """
                                (function() {
                                    // Remove annoying ad elements & banners
                                    const adSelectors = [
                                        '.adsbygoogle', 'iframe[src*="ad"]', '#ad-container',
                                        '.floating-ad', '.popunder', '.ad-box', 'div[class*="banner"]',
                                        'div[id*="advert"]', 'div[class*="popup"]', '#cookie-notice'
                                    ];
                                    adSelectors.forEach(sel => {
                                        document.querySelectorAll(sel).forEach(el => el.remove());
                                    });

                                    // Extract title if possible
                                    const h1 = document.querySelector('h1, .drama-title, .title');
                                    if (h1 && window.AndroidCinema) {
                                        window.AndroidCinema.postDramaTitle(h1.innerText.trim());
                                    }

                                    // Notify video start
                                    const videos = document.querySelectorAll('video');
                                    videos.forEach(v => {
                                        v.addEventListener('play', () => {
                                            if (window.AndroidCinema) {
                                                window.AndroidCinema.notifyVideoStarted();
                                            }
                                        });
                                    });
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(cleanScript, null)
                        }
                    }

                    loadUrl(url)
                    webViewRef = this
                    webViewInstanceHolder(this)
                }
            },
            update = { webView ->
                if (webView.url != url && !url.isBlank()) {
                    webView.loadUrl(url)
                }
            }
        )

        // If HTML5 video enters fullscreen, render custom view above
        customView?.let { cView ->
            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                factory = {
                    FrameLayout(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(
                            cView,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                }
            )
        }
    }
}
