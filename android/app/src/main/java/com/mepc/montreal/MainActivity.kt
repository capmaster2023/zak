package com.mepc.montreal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsetsController

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    private val BASE_URL = "https://mepcmontreal.com"

    // Routes mapped to nav items
    private val navRoutes = mapOf(
        R.id.nav_accueil    to "/",
        R.id.nav_sermons    to "/sermons",
        R.id.nav_evenements to "/evenements",
        R.id.nav_contact    to "/contact"
    )

    // Titles mapped to routes (prefix match)
    private val routeTitles = listOf(
        "/sermons"    to "Sermons",
        "/evenements" to "Événements",
        "/contact"    to "Contact",
        "/"           to "MEPC Montréal"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        toolbar    = findViewById(R.id.toolbar)
        webView    = findViewById(R.id.webview)
        progressBar= findViewById(R.id.progress_bar)
        bottomNav  = findViewById(R.id.bottom_nav)

        setSupportActionBar(toolbar)
        setupEdgeToEdge()
        setupWebView()
        setupBottomNav()

        val startUrl = savedInstanceState?.getString("current_url") ?: BASE_URL
        webView.loadUrl(startUrl)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            toolbar.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(webView.settings) {
            javaScriptEnabled       = true
            domStorageEnabled       = true
            loadWithOverviewMode    = true
            useWideViewPort         = true
            setSupportZoom(false)
            builtInZoomControls     = false
            mixedContentMode        = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode               = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            userAgentString = userAgentString + " MEPCApp/1.0 Android"
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title ?: "MEPC Montréal"
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return if (url.startsWith(BASE_URL) || url.startsWith("https://mepcmontreal.com")) {
                    false // let WebView handle internal URLs
                } else {
                    // Open external URLs in browser
                    startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                    )
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { syncBottomNav(it) }
                injectAppCSS()
            }
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            val route = navRoutes[item.itemId]
            if (route != null) {
                val targetUrl = BASE_URL + route
                val currentUrl = webView.url ?: ""
                if (!currentUrl.endsWith(route) && currentUrl != targetUrl) {
                    webView.loadUrl(targetUrl)
                }
            }
            true
        }
    }

    private fun syncBottomNav(url: String) {
        val path = url.removePrefix(BASE_URL).removePrefix("https://mepcmontreal.com")
        val matchedItem = navRoutes.entries.firstOrNull { (_, route) ->
            if (route == "/") path == "/" || path.isEmpty()
            else path.startsWith(route)
        }
        matchedItem?.let { (itemId, _) ->
            bottomNav.selectedItemId = itemId
        }

        // Update toolbar title
        val title = routeTitles.firstOrNull { (route, _) ->
            if (route == "/") path == "/" || path.isEmpty()
            else path.startsWith(route)
        }?.second ?: "MEPC Montréal"
        supportActionBar?.title = title
    }

    /** Inject CSS to hide site header/footer (replaced by native UI) */
    private fun injectAppCSS() {
        val css = """
            #headerMain, header.site-header, nav.main-nav { display: none !important; }
            footer, .site-footer { display: none !important; }
            body { padding-top: 0 !important; margin-top: 0 !important; }
            .mobile-nav, .mobile-overlay { display: none !important; }
        """.trimIndent().replace("\n", " ")

        val js = """
            (function() {
                var style = document.createElement('style');
                style.textContent = '$css';
                document.head.appendChild(style);
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_url", webView.url)
    }
}
