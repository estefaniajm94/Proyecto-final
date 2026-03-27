package com.estef.antiphishingcoach.presentation.common

import android.widget.ImageView
import com.estef.antiphishingcoach.core.avatar.AvatarCatalog

fun ImageView.renderAvatar(avatarId: String?) {
    setImageResource(AvatarCatalog.resolve(avatarId).drawableRes)
}
