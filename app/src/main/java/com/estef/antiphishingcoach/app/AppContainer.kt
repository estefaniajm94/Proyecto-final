package com.estef.antiphishingcoach.app

import android.content.Context
import com.estef.antiphishingcoach.data.local.db.AppDatabase
import com.estef.antiphishingcoach.data.local.preferences.SecureSettingsDataSource
import com.estef.antiphishingcoach.data.local.seed.SeedAssetLoader
import com.estef.antiphishingcoach.data.ocr.MlKitOcrRepository
import com.estef.antiphishingcoach.data.repository.CoachRepositoryImpl
import com.estef.antiphishingcoach.data.repository.IncidentRepositoryImpl
import com.estef.antiphishingcoach.data.repository.SettingsRepositoryImpl
import com.estef.antiphishingcoach.data.repository.TrainingRepositoryImpl
import com.estef.antiphishingcoach.domain.repository.CoachRepository
import com.estef.antiphishingcoach.domain.repository.IncidentRepository
import com.estef.antiphishingcoach.domain.repository.OcrRepository
import com.estef.antiphishingcoach.domain.repository.SettingsRepository
import com.estef.antiphishingcoach.domain.repository.TrainingRepository
import com.estef.antiphishingcoach.domain.usecase.AnalyzeAndPersistUseCase
import com.estef.antiphishingcoach.domain.usecase.AnalyzeInputUseCase
import com.estef.antiphishingcoach.domain.usecase.ClearLocalDataUseCase
import com.estef.antiphishingcoach.domain.usecase.ExtractTextFromImageUseCase
import com.estef.antiphishingcoach.domain.usecase.GetCoachScenariosUseCase
import com.estef.antiphishingcoach.domain.usecase.GetTrainingQuestionsUseCase
import com.estef.antiphishingcoach.domain.usecase.IsLocalLockEnabledUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveHistoryUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveIncidentDetailUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ObserveLocalLockUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleExtremePrivacyUseCase
import com.estef.antiphishingcoach.domain.usecase.ToggleLocalLockUseCase

/**
 * Service locator simple para MVP A sin framework DI.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy { AppDatabase.create(appContext) }
    private val secureSettingsDataSource: SecureSettingsDataSource by lazy { SecureSettingsDataSource(appContext) }
    private val seedAssetLoader: SeedAssetLoader by lazy { SeedAssetLoader(appContext) }

    val incidentRepository: IncidentRepository by lazy {
        IncidentRepositoryImpl(
            database = database,
            incidentDao = database.incidentDao(),
            analysisResultDao = database.analysisResultDao(),
            detectedSignalDao = database.detectedSignalDao()
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(secureSettingsDataSource)
    }

    val coachRepository: CoachRepository by lazy {
        CoachRepositoryImpl(seedAssetLoader)
    }

    val trainingRepository: TrainingRepository by lazy {
        TrainingRepositoryImpl(seedAssetLoader)
    }

    val ocrRepository: OcrRepository by lazy {
        MlKitOcrRepository(appContext)
    }

    private val analyzeInputUseCase: AnalyzeInputUseCase by lazy { AnalyzeInputUseCase() }

    val analyzeAndPersistUseCase: AnalyzeAndPersistUseCase by lazy {
        AnalyzeAndPersistUseCase(
            analyzeInputUseCase = analyzeInputUseCase,
            incidentRepository = incidentRepository,
            settingsRepository = settingsRepository
        )
    }

    val observeExtremePrivacyUseCase: ObserveExtremePrivacyUseCase by lazy {
        ObserveExtremePrivacyUseCase(settingsRepository)
    }

    val observeLocalLockUseCase: ObserveLocalLockUseCase by lazy {
        ObserveLocalLockUseCase(settingsRepository)
    }

    val isLocalLockEnabledUseCase: IsLocalLockEnabledUseCase by lazy {
        IsLocalLockEnabledUseCase(settingsRepository)
    }

    val observeHistoryUseCase: ObserveHistoryUseCase by lazy {
        ObserveHistoryUseCase(incidentRepository)
    }

    val observeIncidentDetailUseCase: ObserveIncidentDetailUseCase by lazy {
        ObserveIncidentDetailUseCase(incidentRepository)
    }

    val toggleExtremePrivacyUseCase: ToggleExtremePrivacyUseCase by lazy {
        ToggleExtremePrivacyUseCase(settingsRepository)
    }

    val toggleLocalLockUseCase: ToggleLocalLockUseCase by lazy {
        ToggleLocalLockUseCase(settingsRepository)
    }

    val clearLocalDataUseCase: ClearLocalDataUseCase by lazy {
        ClearLocalDataUseCase(incidentRepository)
    }

    val getCoachScenariosUseCase: GetCoachScenariosUseCase by lazy {
        GetCoachScenariosUseCase(coachRepository)
    }

    val getTrainingQuestionsUseCase: GetTrainingQuestionsUseCase by lazy {
        GetTrainingQuestionsUseCase(trainingRepository)
    }

    val extractTextFromImageUseCase: ExtractTextFromImageUseCase by lazy {
        ExtractTextFromImageUseCase(ocrRepository)
    }
}
