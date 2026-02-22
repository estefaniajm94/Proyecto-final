package com.estef.antiphishingcoach.presentation.resources

data class OfficialResourceItem(
    val title: String,
    val description: String,
    val phone: String? = null,
    val url: String? = null
)
