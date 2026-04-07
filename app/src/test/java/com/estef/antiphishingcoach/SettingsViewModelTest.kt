package com.estef.antiphishingcoach

import com.estef.antiphishingcoach.domain.model.AuthActionResult
import com.estef.antiphishingcoach.domain.model.AuthUser
import com.estef.antiphishingcoach.domain.repository.AuthRepository
import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.LogoutCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveCurrentUserUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.UpdateCurrentUserAvatarUseCase
import com.estef.antiphishingcoach.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val currentUserFlow = MutableStateFlow<AuthUser?>(
        AuthUser(
            id = 7L,
            displayName = "Estef",
            email = "estef@example.com",
            avatarId = "avatar_01"
        )
    )
    private val extremePrivacyFlow = MutableStateFlow(false)
    private val localLockFlow = MutableStateFlow(false)

    private var clearLocalDataCalls = 0
    private var logoutCalls = 0

    private val settingsRepository = object : SettingsRepository {
        override fun observeExtremePrivacy(): Flow<Boolean> = extremePrivacyFlow
        override fun observeLocalLockEnabled(): Flow<Boolean> = localLockFlow
        override suspend fun isExtremePrivacyEnabled(): Boolean = extremePrivacyFlow.value
        override suspend fun isLocalLockEnabled(): Boolean = localLockFlow.value

        override suspend fun setExtremePrivacy(enabled: Boolean) {
            extremePrivacyFlow.value = enabled
        }

        override suspend fun setLocalLockEnabled(enabled: Boolean) {
            localLockFlow.value = enabled
        }
    }

    private val authRepository = object : AuthRepository {
        override fun observeCurrentUser(): Flow<AuthUser?> = currentUserFlow
        override fun hasActiveSession(): Boolean = currentUserFlow.value != null
        override suspend fun register(
            displayName: String,
            email: String,
            password: String,
            avatarId: String
        ): AuthActionResult = throw NotImplementedError()

        override suspend fun login(email: String, password: String): AuthActionResult {
            throw NotImplementedError()
        }

        override suspend fun findUserByEmail(email: String): AuthUser? = null
        override suspend fun updateCurrentUserAvatar(avatarId: String): Boolean = true

        override suspend fun logout() {
            logoutCalls++
            currentUserFlow.value = null
        }
    }

    private val incidentRepository = FakeIncidentRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clearLocalData delega en caso de uso y publica mensaje`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.clearLocalData()
        advanceUntilIdle()

        assertEquals(1, clearLocalDataCalls)
        assertEquals("Datos locales eliminados.", viewModel.uiState.value.statusMessage)
    }

    @Test
    fun `onLocalLockChanged actualiza estado observado y mensaje`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onLocalLockChanged(true)
        advanceUntilIdle()

        assertTrue(localLockFlow.value)
        assertTrue(viewModel.uiState.value.localLockEnabled)
        assertEquals(
            "Bloqueo local activado para Historial y Ajustes.",
            viewModel.uiState.value.statusMessage
        )
    }

    @Test
    fun `onLocalLockNotAvailable publica aviso de biometria no disponible`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onLocalLockNotAvailable()

        assertEquals(
            "No hay biometria o credencial del dispositivo disponible para activar el bloqueo local.",
            viewModel.uiState.value.statusMessage
        )
        assertFalse(viewModel.uiState.value.logoutCompleted)
    }

    @Test
    fun `onAccessBlockedByAuthError publica mensaje de acceso bloqueado`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAccessBlockedByAuthError()

        assertEquals(
            "No se pudo autenticar para abrir Ajustes protegidos.",
            viewModel.uiState.value.statusMessage
        )
        assertFalse(viewModel.uiState.value.logoutCompleted)
    }

    @Test
    fun `logout marca cierre de sesion y limpia usuario observado`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(1, logoutCalls)
        assertTrue(viewModel.uiState.value.logoutCompleted)
        assertEquals("Sesion cerrada.", viewModel.uiState.value.statusMessage)
        assertEquals(null, viewModel.uiState.value.currentUserName)
    }

    private fun buildViewModel(): SettingsViewModel {
        return SettingsViewModel(
            observeCurrentUserUseCase = ObserveCurrentUserUseCase(authRepository),
            observeExtremePrivacyUseCase = ObserveExtremePrivacyUseCase(settingsRepository),
            observeLocalLockUseCase = ObserveLocalLockUseCase(settingsRepository),
            toggleExtremePrivacyUseCase = ToggleExtremePrivacyUseCase(settingsRepository),
            toggleLocalLockUseCase = ToggleLocalLockUseCase(settingsRepository),
            clearLocalDataUseCase = ClearLocalDataUseCase(incidentRepository),
            updateCurrentUserAvatarUseCase = UpdateCurrentUserAvatarUseCase(authRepository),
            logoutCurrentUserUseCase = LogoutCurrentUserUseCase(authRepository),
            stringResolver = TestStringResolver()
        )
    }

    private inner class FakeIncidentRepository : com.estef.antiphishingcoach.domain.repository.IncidentRepository {
        override suspend fun saveIncident(record: com.estef.antiphishingcoach.domain.model.IncidentRecord): Long {
            return 1L
        }

        override fun observeHistory(): Flow<List<com.estef.antiphishingcoach.domain.model.IncidentRecord>> {
            return flowOf(emptyList())
        }

        override fun observeLatestIncidentSummary(): Flow<com.estef.antiphishingcoach.domain.model.IncidentSummary?> {
            return flowOf(null)
        }

        override fun observeIncidentDetail(incidentId: Long): Flow<com.estef.antiphishingcoach.domain.model.IncidentRecord?> {
            return flowOf(null)
        }

        override suspend fun clearAll() {
            clearLocalDataCalls++
        }
    }
}
