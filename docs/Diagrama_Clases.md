# Diagrama de Clases (vista simplificada)

## Diagrama (Mermaid)
```mermaid
classDiagram
    class AnalyzeFragment
    class AnalyzeViewModel
    class AnalyzeAndPersistUseCase
    class AnalyzeInputUseCase
    class RuleEngine
    class IncidentRepository
    class IncidentRepositoryImpl
    class IncidentDao
    class AppDatabase
    class SettingsRepository
    class SettingsRepositoryImpl
    class SecureSettingsDataSource
    class CoachFragment
    class CoachViewModel
    class GetCoachScenariosUseCase
    class CoachRepository
    class CoachRepositoryImpl
    class SeedAssetLoader
    class SeedJsonParser
    class TrainingViewModel
    class GetTrainingQuestionsUseCase
    class TrainingRepository
    class TrainingRepositoryImpl
    class QuizEngine
    class AnalysisDetailFragment
    class IncidentDetailViewModel
    class ObserveIncidentDetailUseCase
    class HistoryFragment
    class HistoryViewModel
    class ObserveHistoryUseCase

    AnalyzeFragment --> AnalyzeViewModel
    AnalyzeViewModel --> AnalyzeAndPersistUseCase
    AnalyzeAndPersistUseCase --> AnalyzeInputUseCase
    AnalyzeInputUseCase --> RuleEngine
    AnalyzeAndPersistUseCase --> IncidentRepository
    AnalyzeAndPersistUseCase --> SettingsRepository
    IncidentRepository <|.. IncidentRepositoryImpl
    IncidentRepositoryImpl --> IncidentDao
    IncidentRepositoryImpl --> AppDatabase
    SettingsRepository <|.. SettingsRepositoryImpl
    SettingsRepositoryImpl --> SecureSettingsDataSource

    CoachFragment --> CoachViewModel
    CoachViewModel --> GetCoachScenariosUseCase
    GetCoachScenariosUseCase --> CoachRepository
    CoachRepository <|.. CoachRepositoryImpl
    CoachRepositoryImpl --> SeedAssetLoader
    SeedAssetLoader --> SeedJsonParser

    TrainingViewModel --> GetTrainingQuestionsUseCase
    GetTrainingQuestionsUseCase --> TrainingRepository
    TrainingRepository <|.. TrainingRepositoryImpl
    TrainingRepositoryImpl --> SeedAssetLoader
    TrainingViewModel --> QuizEngine

    AnalysisDetailFragment --> IncidentDetailViewModel
    IncidentDetailViewModel --> ObserveIncidentDetailUseCase
    ObserveIncidentDetailUseCase --> IncidentRepository

    HistoryFragment --> HistoryViewModel
    HistoryViewModel --> ObserveHistoryUseCase
    ObserveHistoryUseCase --> IncidentRepository
```

## Notas de arquitectura
- `presentation`: Fragments + ViewModels (`StateFlow`) para estado UI.
- `domain`: use cases y motor heuristico/quiz (logica testeable).
- `data`: repositorios, Room y seed local desde `assets`.
- Separacion MVVM mantenida: `UI -> ViewModel -> UseCase -> Repository -> Fuente de datos`.
