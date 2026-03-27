package com.estef.antiphishingcoach.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.estef.antiphishingcoach.core.avatar.AvatarCatalog

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long,
    val displayName: String,
    val email: String,
    val passwordHash: String,
    @ColumnInfo(defaultValue = "'avatar_default'")
    val avatarId: String = AvatarCatalog.DEFAULT_AVATAR_ID
)
