package com.estef.antiphishingcoach.presentation.common

import android.content.Context
import androidx.annotation.StringRes

interface StringResolver {
    fun get(@StringRes resId: Int, vararg formatArgs: Any): String
}

class AndroidStringResolver(private val context: Context) : StringResolver {
    override fun get(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
