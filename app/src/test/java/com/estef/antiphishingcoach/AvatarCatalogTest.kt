package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.core.avatar.AvatarCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarCatalogTest {

    @Test
    fun `resolveAvatarId returns default when id is null`() {
        assertEquals(AvatarCatalog.DEFAULT_AVATAR_ID, AvatarCatalog.resolveAvatarId(null))
    }

    @Test
    fun `resolveAvatarId returns default when id is unknown`() {
        assertEquals(AvatarCatalog.DEFAULT_AVATAR_ID, AvatarCatalog.resolveAvatarId("missing"))
    }
}
