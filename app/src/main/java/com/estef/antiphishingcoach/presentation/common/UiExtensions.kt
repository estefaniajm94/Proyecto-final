package com.estef.antiphishingcoach.presentation.common

import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.showShortMessage(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}
