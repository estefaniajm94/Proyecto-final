package com.estef.antiphishingcoach.presentation.common

import androidx.fragment.app.Fragment
import com.estef.antiphishingcoach.app.AntiPhishingCoachApp
import com.estef.antiphishingcoach.app.AppContainer

fun Fragment.appContainer(): AppContainer {
    val app = requireActivity().application as AntiPhishingCoachApp
    return app.appContainer
}
