package com.fahad.newtruelovebyfahad.ui.fragments.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.crosspromo.adapter.CrossPromoAppsRV
import com.example.ads.crosspromo.api.retrofit.helper.Response
import com.example.ads.crosspromo.helper.PanelConstants.SETTING_PANEL
import com.example.ads.crosspromo.helper.openUrl
import com.example.ads.crosspromo.viewModel.CrossPromoViewModel
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.MainScreenNavigationDirections
import com.fahad.newtruelovebyfahad.databinding.FragmentSettingsBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.shareApp
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.getProScreen
import com.project.common.utils.privacyPolicy
import com.project.common.utils.termOfUse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private val crossPromoViewModel by viewModels<CrossPromoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding?.initView()
        initObserver()


    }

    private fun FragmentSettingsBinding.initView() {

        runCatching {
            if (!isProVersion()) {
                this.initCrossPromoAds()
                moreAppsTv.visible()
                adsTv.visible()
            } else {
                moreAppsTv.gone()
                adsTv.gone()
                if (recommendedAppsRv.isVisible)
                    recommendedAppsRv.gone()
            }
        }
        cancelSubsContainer.setSingleClickListener {
            openPlayStoreAccount()
        }

        backBtn.setSingleClickListener {
            closeFragment()
        }

        languageContainer.setSingleClickListener {
            firebaseAnalytics?.logEvent(Events.Screens.MAIN, Bundle().apply {
                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU)
                putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.MainScreen.LANGUAGE)
            })
            kotlin.runCatching {
                activity?.let { mActivity ->
                    if (mActivity is MainActivity) {
                        mActivity.changeLanguage()
                    }
                }
            }
        }

        shareContainer.setSingleClickListener {
            firebaseAnalytics?.logEvent(Events.Screens.SETTING, Bundle().apply {
                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU)
                putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.MainScreen.SHARE)
            })
            activity?.shareApp(getString(com.project.common.R.string.app_name_new))
        }
        rateUsContainer.setSingleClickListener {
            try {
                firebaseAnalytics?.logEvent(Events.Screens.SETTING, Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU)
                    putString(
                        Events.ParamsKeys.BUTTON, Events.ParamsValues.MainScreen.RATE_US
                    )
                })
                navController?.navigate(MainScreenNavigationDirections.actionGlobalNavRating())
            } catch (_: Exception) {
            }
        }

        privacyPolicyContainer.setSingleClickListener {
            firebaseAnalytics?.logEvent(Events.Screens.SETTING, Bundle().apply {
                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU)
                putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.PRIVACY_POLICY)
            })
            activity?.privacyPolicy()
        }

        termsContainer.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.SETTING,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            activity?.termOfUse()
        }

        topCard.setSingleClickListener {
            runCatching {

                firebaseAnalytics?.logEvent(Events.Screens.SETTING, Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.PRO)
                })
                activity?.let {
                    startActivity(Intent().apply {
                        setClassName(
                            it.applicationContext,
                            getProScreen()
                        )
                        putExtra("from_frames", false)
                    })
                }

            }
        }

    }

    private fun closeFragment() {
        kotlin.runCatching {
            navController?.navigateUp()
        }
    }

    private fun initObserver() {

        if (isProVersion()) {
            _binding?.cancelSubsContainer?.isVisible = true
            _binding?.topCard?.isVisible = false
            _binding?.txt1?.isVisible = false
            _binding?.txt2?.isVisible = false
            _binding?.arrowBtn?.isVisible = false
        }
    }

    private fun openPlayStoreAccount() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions")
                )
            )
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun FragmentSettingsBinding.initCrossPromoAds() {
        if (!isProVersion() && activity?.isNetworkAvailable() == true) {
            crossPromoViewModel.crossPromoAds.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is Response.Success -> {
                        Log.d("Fahad", "CrossPromoSuccess: ${it.data}")
                        it.data?.let { crossPromo ->
                            crossPromo.forEach {
                                if (it.placement?.lowercase() == SETTING_PANEL.lowercase()) it.ads?.icon?.let {
                                    recommendedAppsRv.visible()
                                    moreAppsTv.visible()
                                    adsTv.visible()
                                    val placement = SETTING_PANEL.lowercase()
                                    val crossPromoAppsRV = CrossPromoAppsRV(it, onImpression = {
                                        firebaseAnalytics?.logEvent(
                                            Events.Screens.SETTING,
                                            Bundle().apply {
                                                putString(
                                                    Events.ParamsKeys.SUB_SCREEN,
                                                    Events.SubScreens.SLIDER_MENU
                                                )
                                                putString(
                                                    Events.ParamsKeys.ACTION,
                                                    Events.ParamsValues.DISPLAYED
                                                )
                                                putString(
                                                    Events.ParamsKeys.CROSS_PROMO_AD_TITLE,
                                                    "${it.title}".replace(".", "_").lowercase()
                                                )
                                                putString(
                                                    Events.ParamsKeys.CROSS_PROMO_AD_PLACEMENT,
                                                    placement.replace(".", "_").lowercase()
                                                )
                                                putString(
                                                    Events.ParamsKeys.CROSS_PROMO_AD_TYPE,
                                                    "${it.adType}".replace(".", "_").lowercase()
                                                )
                                            })
                                    }, onCLick = {

                                        it.link?.let { currentLink ->
                                            if (currentLink.isNotBlank()) {
                                                firebaseAnalytics?.logEvent(
                                                    Events.Screens.SETTING,
                                                    Bundle().apply {
                                                        putString(
                                                            Events.ParamsKeys.SUB_SCREEN,
                                                            Events.SubScreens.SLIDER_MENU
                                                        )
                                                        putString(
                                                            Events.ParamsKeys.ACTION,
                                                            Events.ParamsValues.CLICKED
                                                        )
                                                        putString(
                                                            Events.ParamsKeys.CROSS_PROMO_AD_TITLE,
                                                            "${it.title}".replace(".", "_")
                                                                .lowercase()
                                                        )
                                                        putString(
                                                            Events.ParamsKeys.CROSS_PROMO_AD_PLACEMENT,
                                                            placement.replace(".", "_").lowercase()
                                                        )
                                                        putString(
                                                            Events.ParamsKeys.CROSS_PROMO_AD_TYPE,
                                                            "${it.adType}".replace(".", "_")
                                                                .lowercase()
                                                        )
                                                    })
                                                activity?.openUrl(Uri.parse(currentLink))
                                            }
                                        }
                                    })
                                    recommendedAppsRv.adapter = crossPromoAppsRV
                                }
                            }
                        }
                    }

                    is Response.Error -> {
                        recommendedAppsRv.invisible()
                        moreAppsTv.invisible()
                        adsTv.invisible()

                        /*binding.crossPromoLayout.gone()
                    binding.adTv.gone()*/
                    }
                }
            }
        } else {
            recommendedAppsRv.invisible()
            moreAppsTv.invisible()
            adsTv.invisible()
        }
    }

}