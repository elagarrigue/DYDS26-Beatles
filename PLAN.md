# Plan: Refactorización Arquitectónica Completa - SOLID y Clean Code

## Resumen Ejecutivo
Resolver violaciones críticas de arquitectura mediante creación de interfaces para capas de datos, corrección de retornos de repository, división de ViewModels por responsabilidad, y eliminación de dependencias cruzadas.

---

## Problemas a Resolver

### CRÍTICOS
| ID  | Problema | Impacto |
|-----|----------|--------|
| **C1** | `MoviesRepository` retorna `RemoteMovie` (data) en lugar de `Movie` (dominio) | Domain depende de Data, viola Arquitectura de Capas |
| **C2** | `LocalDataSource` importa y usa `RemoteMovie` para caché | Acoplamiento entre subsistemas data/external y data/local |

### GRAVES
| ID  | Problema | Impacto |
|-----|----------|--------|
| **G1** | `MoviesViewModel` maneja dos pantallas (Home + Detail) violando SRP | Sin separación de responsabilidades |
| **G2** | `DetailScreen` importa `MoviesViewModel` desde `presentation.home` | Dependencia cruzada entre paquetes |
| **G3** | Propiedades públicas `*UseCaseProvider` no referenciadas | Código muerto |
| **G4** | Clases sin interfaz: `RemoteDataSource`, `LocalDataSource`, `GetPopularMoviesUseCase`, `GetMovieDetailsUseCase` | Impide DI y testing unitario, acoplamiento a implementaciones |

---

## Plan Detallado - 10 Pasos

### **Paso 1: Crear Modelo Local Independiente**
**Archivo**: `data/local/LocalModels.kt` (NUEVO)
- Definir `data class LocalMovie(...)` con estructura similar a `Movie` pero sin URLs procesadas
- Propósito: Desacoplar `LocalDataSource` de modelos externos
- **Principio SOLID**: Interface Segregation (cada subsistema su propio modelo)

**Beneficio**: `LocalDataSource` ya no importará `RemoteMovie`

---

### **Paso 2: Crear Interfaz para RemoteDataSource**
**Archivos**:
- `data/external/IRemoteDataSource.kt` (NUEVO)
  ```
  interface IRemoteDataSource {
      suspend fun getPopularMovies(): List<RemoteMovie>
      suspend fun getMovieDetails(id: Int): RemoteMovie
  }
  ```
- Actualizar `data/external/RemoteDataSource.kt`: implementar `IRemoteDataSource`

**Principios SOLID**: Dependency Inversion (inyectar interfaz, no clase concreta)

**Beneficio**: Facilita testing con mocks, permite múltiples implementaciones

---

### **Paso 3: Crear Interfaz para LocalDataSource**
**Archivos**:
- `data/local/ILocalDataSource.kt` (NUEVO)
  ```
  interface ILocalDataSource {
      fun getPopularMovies(): List<LocalMovie>?
      fun savePopularMovies(movies: List<LocalMovie>)
      fun getMovieDetails(id: Int): LocalMovie?
  }
  ```
- Actualizar `data/local/LocalDataSource.kt`: implementar `ILocalDataSource`, cambiar tipo interno de `RemoteMovie` a `LocalMovie`

**Principios SOLID**: Single Responsibility, Dependency Inversion

**Beneficio**: Separación clara de responsabilidades, desacoplamiento

---

### **Paso 4: Crear Métodos de Conversión de Modelos**
**Archivo**: `data/external/RemoteModels.kt`
- Agregar método: `RemoteMovie.toLocalMovie(): LocalMovie`
- Mantener método existente: `RemoteMovie.toDomainMovie(): Movie`

**Archivo**: `data/local/LocalModels.kt`
- Agregar método: `LocalMovie.toDomainMovie(): Movie`

**Archivo**: `data/DataMappers.kt` (NUEVO - Opcional pero recomendado)
- Centralizar conversiones de modelos
- Facilita cambios futuros, evita duplicación

**Principios SOLID**: Single Responsibility (separar lógica de conversión)

---

### **Paso 5: Corregir MoviesRepository**
**Archivo**: `domain/repository/MoviesRepository.kt`
- **CAMBIAR** retorno:
  ```
  // ANTES
  suspend fun getPopularMovies(): List<RemoteMovie>
  suspend fun getMovieDetails(id: Int): RemoteMovie?
  
  // DESPUÉS
  suspend fun getPopularMovies(): List<Movie>
  suspend fun getMovieDetails(id: Int): Movie?
  ```

**Principios SOLID**: Dependency Inversion (domain nunca importa data)

---

### **Paso 6: Actualizar MoviesRepositoryImpl**
**Archivo**: `data/MoviesRepositoryImpl.kt`
- Cambiar parámetros inyectados a interfaces: `IRemoteDataSource`, `ILocalDataSource`
- Implementar conversiones en el repositorio:
  ```
  // getPopularMovies
  localDataSource.getPopularMovies()?.map { it.toDomainMovie() }?.let { return it }
  return remoteDataSource.getPopularMovies().map { remote ->
      remote.toLocalMovie().also { local ->
          localDataSource.savePopularMovies(listOf(it))
      }.toDomainMovie()
  }
  
  // getMovieDetails
  return localDataSource.getMovieDetails(id)?.toDomainMovie() 
      ?: remoteDataSource.getMovieDetails(id)?.toDomainMovie()
  ```

**Principios SOLID**: Liskov Substitution (usar interfaces), Single Responsibility

---

### **Paso 7: Crear Interfaces para Casos de Uso**
**Archivos**:
- `domain/usecase/IGetPopularMoviesUseCase.kt` (NUEVO)
  ```
  interface IGetPopularMoviesUseCase {
      suspend fun execute(): List<QualifiedMovie>
  }
  ```
- `domain/usecase/IGetMovieDetailsUseCase.kt` (NUEVO)
  ```
  interface IGetMovieDetailsUseCase {
      suspend fun execute(id: Int): Movie?
  }
  ```
- Actualizar clases concretas para implementar interfaces

**Principios SOLID**: Dependency Inversion (inyectar interfaz en ViewModel)

---

### **Paso 8: Dividir ViewModels por Responsabilidad**
**Archivos**:

#### 8a. Crear HomeViewModel
- **Nuevo**: `presentation/home/HomeViewModel.kt`
- Responsabilidades:
  - Gestionar estado de listado de películas
  - Llamar a `IGetPopularMoviesUseCase`
  - Exponer `moviesStateFlow: Flow<MoviesUiState>`
  - Método: `getAllMovies()`

#### 8b. Crear DetailViewModel
- **Nuevo**: `presentation/detail/DetailViewModel.kt`
- Responsabilidades:
  - Gestionar estado de detalle de película
  - Llamar a `IGetMovieDetailsUseCase`
  - Exponer `movieDetailStateFlow: Flow<MovieDetailUiState>`
  - Método: `getMovieDetail(id: Int)`

#### 8c. Eliminar MoviesViewModel antiguo
- **Obsoleto**: `presentation/home/MoviesViewModel.kt` (ELIMINAR)

**Principios SOLID**: Single Responsibility (cada ViewModel = una pantalla)

**Beneficio**: Testeable, desacoplado, responsabilidades claras

---

### **Paso 9: Corregir Dependencias Cruzadas en Presentación**
**Archivo**: `presentation/detail/DetailScreen.kt`
- **CAMBIAR** parámetro de `MoviesViewModel` → `DetailViewModel`
- **CAMBIAR** importación:
  ```
  // ANTES
  import edu.dyds.movies.presentation.home.MoviesViewModel
  
  // DESPUÉS
  import edu.dyds.movies.presentation.detail.DetailViewModel
  ```
- Actualizar referencias: `viewModel.movieDetailStateFlow` → `viewModel.movieDetailStateFlow` (idéntico, pero ViewModel diferente)

**Principios SOLID**: Dependency Inversion (cada screen = su ViewModel)

---

### **Paso 10: Refactorizar Inyección de Dependencias**
**Archivo**: `di/MoviesDependencyInjector.kt`
- Cambiar parámetros de inyección a interfaces:
  ```
  IRemoteDataSource en lugar de RemoteDataSource
  ILocalDataSource en lugar de LocalDataSource
  IGetPopularMoviesUseCase en lugar de GetPopularMoviesUseCase
  IGetMovieDetailsUseCase en lugar de GetMovieDetailsUseCase
  ```
- Crear factory para cada ViewModel:
  ```
  @Composable
  fun getHomeViewModel(): HomeViewModel = viewModel {
      HomeViewModel(getPopularMoviesUseCase)
  }
  
  @Composable
  fun getDetailViewModel(): DetailViewModel = viewModel {
      DetailViewModel(getMovieDetailsUseCase)
  }
  ```
- **ELIMINAR** propiedades públicas no usadas:
  ```
  val getPopularMoviesUseCaseProvider = { getPopularMoviesUseCase }
  val getMovieDetailsUseCaseProvider = { getMovieDetailsUseCase }
  ```

**Principios SOLID**: Dependency Inversion (inyectar interfaces)

**Beneficio**: Eliminación de código muerto, DI consistente

---

### **Paso 11: Actualizar Presentación (Navigation, App, Screens)**
**Archivo**: `presentation/Navigation.kt`
- Cambiar para usar `getHomeViewModel()` y `getDetailViewModel()` (nuevos)
- Eliminar referencias a `MoviesViewModel` antiguo

**Archivo**: `presentation/App.kt`
- Asegurar consistencia en cómo se pasan ViewModels a screens

**Archivo**: `presentation/home/HomeScreen.kt`
- Cambiar parámetro: `viewModel: MoviesViewModel` → `viewModel: HomeViewModel`
- Actualizar llamadas al ViewModel

---

## Verificaciones Finales

### Checklist de Validación
- [ ] Compilación sin errores
- [ ] `MoviesRepository` retorna `Movie` (no `RemoteMovie`)
- [ ] `LocalDataSource` usa `LocalMovie` (no `RemoteMovie`)
- [ ] `HomeViewModel` y `DetailViewModel` separados
- [ ] `DetailScreen` importa `DetailViewModel` (no `MoviesViewModel` desde home)
- [ ] Todas las clases de datos tienen interfaz correspondiente
- [ ] `MoviesDependencyInjector` usa interfaces
- [ ] Código muerto eliminado (propiedades `*Provider`)
- [ ] Tests unitarios posibles (DI con mocks)

---

## Matriz de Cambios Resumida

| Aspecto | Antes | Después | Principio SOLID |
|--------|-------|--------|-----------------|
| Retorno de Repository | `List<RemoteMovie>` | `List<Movie>` | Dependency Inversion |
| Caché Local | `RemoteMovie` | `LocalMovie` | Interface Segregation |
| DataSources | Clases concretas | Interfaces | Dependency Inversion |
| UseCases | Clases concretas | Interfaces | Dependency Inversion |
| ViewModels | 1 (MoviesViewModel) | 2 (Home, Detail) | Single Responsibility |
| Dependencias de Screen | Home importa home.MoviesViewModel + detail importa home.MoviesViewModel | Home → HomeViewModel + Detail → DetailViewModel | Dependency Inversion |
| DI | Singleton, propiedades públicas | Factory methods, interfaces | Dependency Inversion |

---

## Diagrama de Flujo Post-Refactorización

```
presentation/
├── home/
│   ├── HomeScreen (usa HomeViewModel)
│   └── HomeViewModel (usa IGetPopularMoviesUseCase)
├── detail/
│   ├── DetailScreen (usa DetailViewModel) ✓ SIN dependencia cruzada
│   └── DetailViewModel (usa IGetMovieDetailsUseCase)
└── Navigation.kt (orquesta ambos ViewModels)

domain/
├── repository/
│   └── MoviesRepository (retorna Movie, no RemoteMovie) ✓ CRÍTICO C1
└── usecase/
    ├── IGetPopularMoviesUseCase ✓ INTERFACE
    └── IGetMovieDetailsUseCase ✓ INTERFACE

data/
├── MoviesRepositoryImpl (mapea RemoteMovie → Movie, LocalMovie → Movie) ✓ CRÍTICO C1
├── external/
│   ├── IRemoteDataSource ✓ INTERFACE
│   ├── RemoteDataSource (implementa interfaz)
│   └── RemoteMovie (con conversiones)
└── local/
    ├── ILocalDataSource ✓ INTERFACE
    ├── LocalDataSource (usa LocalMovie, implementa interfaz) ✓ CRÍTICO C2
    └── LocalMovie (modelo independiente) ✓ CRÍTICO C2

di/
└── MoviesDependencyInjector (inyecta interfaces, factories por ViewModel) ✓ G3, G4
```

---

## Impacto por Problema

| Problema | Pasos de Solución | Resultado |
|----------|-------------------|-----------|
| **C1** | 4, 5, 6 | Repository retorna Movie (domain nunca importa data) |
| **C2** | 1, 3, 4, 6 | LocalDataSource desacoplado de RemoteMovie |
| **G1** | 8 | HomeViewModel y DetailViewModel separados |
| **G2** | 8, 9 | DetailScreen importa DetailViewModel (sin dependencia cruzada) |
| **G3** | 10 | Propiedades *Provider eliminadas |
| **G4** | 2, 3, 7, 10 | Todas las clases tienen interfaz, DI con mocks posible |

---

## Consideraciones Futuras

1. **Migración a BD**: `LocalMovie` está listo para serialización con Room/SQLite
2. **Testing**: Con interfaces, crear mocks es trivial
3. **Escalabilidad**: Estructura permite agregar más casos de uso sin modificar existentes
4. **Error Handling**: Considerar Result<T> wrapper para mejor manejo de errores

