# Plan de Refactorización a Clean Architecture por Etapas

Este documento detalla el plan para reestructurar el proyecto aplicando Clean Architecture y principios SOLID. Se divide en tres etapas, cada una asignada a un integrante del equipo.

## Estructura Final Esperada
```text
desktopMain/
└── kotlin/
    └── edu.dyds.movies/
        ├── data/
        │   ├── external/
        │   ├── local/
        │   └── MoviesRepositoryImpl.kt
        ├── di/
        │   └── MoviesDependencyInjector.kt
        ├── domain/
        │   ├── entity/
        │   │   └── Movie.kt
        │   ├── repository/
        │   │   └── MoviesRepository.kt
        │   └── usecase/
        │       ├── GetMovieDetailsUseCase.kt
        │       └── GetPopularMoviesUseCase.kt
        ├── presentation/
        │   ├── detail/
        │   ├── home/
        │   ├── utils/
        │   ├── App.kt
        │   └── Navigation.kt
        └── main.kt
```

---

## Etapa 1: Base de la Arquitectura y Capa Domain
**Asignado a:** Santiago

**Objetivo:** Crear la estructura de paquetes principal y aislar la lógica de negocio pura (entidades y casos de uso) de cualquier dependencia externa.

**Tareas:**
1. Crear los paquetes base dentro de `edu.dyds.movies/`: `presentation`, `domain`, `data`, y `di`.
2. Crear los subpaquetes de `domain/`: `entity`, `repository`, y `usecase`.
3. Extraer `Movie` y `QualifiedMovie` del actual `Movie.kt` y moverlos a `domain/entity/Movie.kt`.
4. Definir la interfaz `MoviesRepository` en `domain/repository/` con los métodos necesarios (`getPopularMovies` y `getMovieDetails`).
5. Crear `GetPopularMoviesUseCase.kt` en `domain/usecase/`. Mover a este caso de uso la lógica de negocio que actualmente está en el ViewModel: ordenar las películas por `voteAverage` de forma descendente y mapear los resultados a `QualifiedMovie` (determinando si es "buena película").
6. Crear `GetMovieDetailsUseCase.kt` en `domain/usecase/` para obtener los detalles de una película.

---

## Etapa 2: Capa Data e Inyección de Dependencias
**Asignado a:** Agostina

**Objetivo:** Implementar la obtención y almacenamiento de datos abstraída para el dominio, y configurar la inyección de dependencias.

**Tareas:**
1. Crear los subpaquetes de `data/`: `external` y `local`.
2. Mover los modelos de red (`RemoteMovie`, `RemoteResult`) y la lógica de llamadas Ktor a la carpeta `data/external/` (por ejemplo, creando un `RemoteDataSource`).
3. Mover la lógica de la caché en memoria (la lista `cacheMovies` que actualmente vive en el ViewModel) a la carpeta `data/local/` (por ejemplo, creando un `LocalDataSource`).
4. Crear la clase `MoviesRepositoryImpl.kt` directamente bajo la carpeta `data/`. Esta clase debe implementar la interfaz `MoviesRepository` definida en la Etapa 1, coordinando la obtención de datos desde local (caché) y external (API de TMDB).
5. Mover `MoviesDependencyInjector.kt` a la carpeta `di/`.
6. Actualizar `MoviesDependencyInjector.kt` para que además del `HttpClient`, se encargue de instanciar los *Data Sources*, el `MoviesRepositoryImpl` y los *Use Cases* definidos en la Etapa 1, proveyéndolos a quien los necesite.

---

## Etapa 3: Capa Presentation y Limpieza
**Asignado a:** Rodrigo

**Objetivo:** Refactorizar la interfaz de usuario, adaptarla a la nueva estructura de carpetas y conectar el `ViewModel` con los casos de uso (eliminando cualquier rastro de la capa de datos).

**Tareas:**
1. Crear los subpaquetes de `presentation/`: `home`, `detail`, y `utils`.
2. Mover `HomeScreen.kt` y `MoviesViewModel.kt` a `presentation/home/`.
3. Mover `DetailScreen.kt` a `presentation/detail/`.
4. Mover `CommonComposables.kt` a `presentation/utils/`.
5. Mover `App.kt` y `Navigation.kt` a la raíz de la carpeta `presentation/`.
6. **Refactorización del `MoviesViewModel`**:
   - Eliminar el uso de `HttpClient` (Ktor) y la caché (`cacheMovies`).
   - Inyectar los casos de uso (`GetPopularMoviesUseCase` y `GetMovieDetailsUseCase`) provenientes de DI.
   - Consumir los casos de uso para actualizar los estados `MoviesUiState` y `MovieDetailUiState`.
7. Actualizar los *imports* en todas las pantallas y verificar que el proyecto compila y funciona correctamente sin romper el flujo original.