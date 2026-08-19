package com.github.v2raygg.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.github.v2raygg.AppConfig
import com.github.v2raygg.R
import com.github.v2raygg.databinding.ActivityNoneBinding
import com.github.v2raygg.extension.toast
import com.github.v2raygg.handler.MmkvManager
import com.github.v2raygg.util.LogUtil
import com.github.v2raygg.util.QRCodeDecoder
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig

class ScannerActivity : HelperBaseActivity() {
    private val binding by lazy { ActivityNoneBinding.inflate(layoutInflater) }

    private val scanQrCode = registerForActivityResult(ScanCustomCode(), ::handleResult)

    // PickVisualMedia uses the Android Photo Picker when available and a system
    // picker fallback on older Android versions. It does not require storage
    // permissions.
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(::decodeQRCodeFromUri)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentViewWithToolbar(binding.root, showHomeAsUp = true, title = getString(R.string.menu_item_import_config_qrcode))

        if (MmkvManager.decodeSettingsBool(AppConfig.PREF_START_SCAN_IMMEDIATE)) {
            launchScan()
        }
    }

    private fun launchScan() {
        scanQrCode.launch(
            ScannerConfig.build {
                setHapticSuccessFeedback(true) // enable (default) or disable haptic feedback when a barcode was detected
                setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
                setShowCloseButton(true) // show or hide (default) close button
                setBarcodeFormats(listOf(BarcodeFormat.QR_CODE))
            }
        )
    }

    private fun handleResult(result: QRResult) {
        if (result is QRResult.QRSuccess) {
            finished(result.content.rawValue.orEmpty())
        } else {
            finish()
        }
    }

    private fun finished(text: String) {
        val intent = Intent()
        intent.putExtra("SCAN_RESULT", text)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scanner, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.scan_code -> {
            launchScan()
            true
        }

        R.id.select_photo -> {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun decodeQRCodeFromUri(uri: Uri) {
        try {
            val bitmap = contentResolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input)
            }
            val text = bitmap?.let(QRCodeDecoder::syncDecodeQRCode)
            if (text.isNullOrEmpty()) {
                toast(R.string.toast_decoding_failed)
            } else {
                finished(text)
            }
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to decode QR code from file", e)
            toast(R.string.toast_decoding_failed)
        }
    }
}
