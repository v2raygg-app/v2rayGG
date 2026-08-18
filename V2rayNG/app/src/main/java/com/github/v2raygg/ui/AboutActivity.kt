package com.github.v2raygg.ui

import android.os.Bundle
import com.github.v2raygg.AppConfig
import com.github.v2raygg.BuildConfig
import com.github.v2raygg.R
import com.github.v2raygg.core.CoreNativeManager
import com.github.v2raygg.databinding.ActivityAboutBinding
import com.github.v2raygg.util.Utils

class AboutActivity : BaseActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(binding.root)
        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(R.string.title_about))

        binding.layoutSoureCcode.setOnClickListener {
            Utils.openUri(this, AppConfig.APP_URL)
        }

        binding.layoutFeedback.setOnClickListener {
            Utils.openUri(this, AppConfig.APP_ISSUES_URL)
        }

        binding.layoutOssLicenses.setOnClickListener {
            val webView = android.webkit.WebView(this)
            webView.loadUrl("file:///android_asset/open_source_licenses.html")
            android.app.AlertDialog.Builder(this)
                .setTitle("Open source licenses")
                .setView(webView)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.layoutTgChannel.setOnClickListener {
            Utils.openUri(this, AppConfig.TG_CHANNEL_URL)
        }

        binding.layoutPrivacyPolicy.setOnClickListener {
            Utils.openUri(this, AppConfig.APP_PRIVACY_POLICY)
        }

        "v${BuildConfig.VERSION_NAME} (${CoreNativeManager.getLibVersion()})".also {
            binding.tvVersion.text = it
        }
        BuildConfig.APPLICATION_ID.also {
            binding.tvAppId.text = it
        }
    }
}