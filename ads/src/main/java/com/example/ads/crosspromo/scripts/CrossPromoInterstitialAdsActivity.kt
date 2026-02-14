package com.example.ads.crosspromo.scripts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.ads.Constants.crossPromoAdsList
import com.example.ads.Constants.placement
import com.example.ads.crosspromo.helper.showInterstitialCrossPromo
import com.example.ads.databinding.ActivityCrossPromoAdsBinding
import com.example.analytics.Constants.firebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrossPromoInterstitialAdsActivity : AppCompatActivity() {

    private var _binding: ActivityCrossPromoAdsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCrossPromoAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        try {
            binding.adImageLayout.post {
                showInterstitialCrossPromo(
                    firebaseAnalytics,
                    binding.adImageLayout,
                    placement,
                    crossPromoAdsList
                )
            }
            binding.closeAdBtn.setOnClickListener {
                val intent = Intent()
                intent.putExtra("response_key", "Ad Closed")
                setResult(RESULT_OK, intent)
                finish()
            }
        } catch (_: Exception) {
        }
    }
}