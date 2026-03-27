package com.estef.antiphishingcoach.core.avatar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.estef.antiphishingcoach.R

data class AvatarOption(
    val id: String,
    @DrawableRes val drawableRes: Int,
    @StringRes val labelRes: Int
)

object AvatarCatalog {
    const val DEFAULT_AVATAR_ID = "avatar_default"

    private val options = listOf(
        AvatarOption(
            id = DEFAULT_AVATAR_ID,
            drawableRes = R.drawable.avatar_default,
            labelRes = R.string.avatar_default_label
        ),
        AvatarOption(
            id = "avatar_01",
            drawableRes = R.drawable.avatar_01,
            labelRes = R.string.avatar_01_label
        ),
        AvatarOption(
            id = "avatar_02",
            drawableRes = R.drawable.avatar_02,
            labelRes = R.string.avatar_02_label
        ),
        AvatarOption(
            id = "avatar_03",
            drawableRes = R.drawable.avatar_03,
            labelRes = R.string.avatar_03_label
        ),
        AvatarOption(
            id = "avatar_04",
            drawableRes = R.drawable.avatar_04,
            labelRes = R.string.avatar_04_label
        ),
        AvatarOption(
            id = "avatar_05",
            drawableRes = R.drawable.avatar_05,
            labelRes = R.string.avatar_05_label
        ),
        AvatarOption(
            id = "avatar_06",
            drawableRes = R.drawable.avatar_06,
            labelRes = R.string.avatar_06_label
        )
    )

    fun all(): List<AvatarOption> = options

    fun resolve(avatarId: String?): AvatarOption {
        return options.firstOrNull { option -> option.id == avatarId } ?: default()
    }

    fun resolveAvatarId(avatarId: String?): String = resolve(avatarId).id

    fun default(): AvatarOption = options.first()
}
