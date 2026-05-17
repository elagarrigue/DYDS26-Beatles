# Plan de Acción para la Corrección de Errores

Este plan describe las etapas para abordar los errores GT1, GT2 y GT4a identificados en el proyecto.

## Etapa 1: Refactorización de la Inicialización de Tests (GT4a)

**Objetivo:** Eliminar la duplicación de código en la inicialización de `MoviesRepositoryFake` y las instancias de los casos de uso en los tests.

**Archivos afectados:**
*   `composeApp/src/desktopTest/kotlin/edu/dyds/movies/domain/usecase/GetPopularMoviesUseCaseImplTest.kt`
*   `composeApp/src/desktopTest/kotlin/edu/dyds/movies/domain/usecase/GetMovieDetailsUseCaseImplTest.kt`

**Acciones:**
1.  En ambos archivos de test, declarar `repositoryFake` y `useCase` como propiedades `private lateinit var` de la clase.
2.  Crear un método `setUp()` anotado con `@Before` en cada clase de test.
3.  Dentro del método `setUp()`, inicializar `repositoryFake` con `MoviesRepositoryFake()` y `useCase` con la implementación correspondiente (`GetPopularMoviesUseCaseImpl(repositoryFake)` o `GetMovieDetailsUseCaseImpl(repositoryFake)`).

**Ejemplo (para GetPopularMoviesUseCaseImplTest.kt):**
```kotlin
import org.junit.Before // Asegúrate de importar Before

class GetPopularMoviesUseCaseImplTest {

    private lateinit var repositoryFake: MoviesRepositoryFake
    private lateinit var useCase: GetPopularMoviesUseCaseImpl

    @Before
    fun setUp() {
        repositoryFake = MoviesRepositoryFake()
        useCase = GetPopularMoviesUseCaseImpl(repositoryFake)
    }

    // ... el resto de los tests
}
```

## Etapa 2: Corrección de Tests en `GetPopularMoviesUseCaseImplTest.kt` (GT1)

**Objetivo:** Mejorar la claridad y eliminar la redundancia en los tests de `GetPopularMoviesUseCaseImplTest.kt`.

**Archivo afectado:**
*   `composeApp/src/desktopTest/kotlin/edu/dyds/movies/domain/usecase/GetPopularMoviesUseCaseImplTest.kt`

**Acciones:**
1.  **Renombrar test ambiguo:** Cambiar el nombre del test `execute returns empty list by default` a algo más descriptivo, como `given repository with no movies, when execute, then returns empty list`.
2.  **Eliminar test redundante:** El test `execute should return empty list when repository returns empty list` es redundante con el test renombrado en el paso anterior. Se debe eliminar este test.

## Etapa 3: Corrección de Tests en `GetMovieDetailsUseCaseImplTest.kt` (GT2)

**Objetivo:** Asegurar que los tests en `GetMovieDetailsUseCaseImplTest.kt` verifiquen el comportamiento observable del caso de uso en lugar de detalles de implementación del fake.

**Archivo afectado:**
*   `composeApp/src/desktopTest/kotlin/edu/dyds/movies/domain/usecase/GetMovieDetailsUseCaseImplTest.kt`

**Acciones:**
1.  **Revisar `execute should delegate to repository with correct id and return null by default`:**
    *   Eliminar la aserción `assertEquals(movieId, repositoryFake.capturedMovieId, "Should pass correct movie id to repository")`. Esta aserción verifica un detalle interno del `MoviesRepositoryFake`.
    *   Asegurarse de que el test se centre en el valor de retorno del `useCase.execute(movieId)` (que debería ser `null` en este escenario). La aserción `assertNull(result, "Result should be null by default when no movieDetailResult is set")` es correcta.
2.  **Revisar `execute should pass different ids correctly to repository`:**
    *   Este test es un ejemplo claro de testing de detalle de implementación. La forma correcta de probar que el `useCase` pasa diferentes IDs es verificar que, si el repositorio *realmente* devolviera diferentes películas para diferentes IDs, el caso de uso las devolvería correctamente.
    *   **Opción recomendada:** Eliminar este test, ya que su propósito es verificar el comportamiento interno del fake, no el comportamiento observable del caso de uso.
    *   **Alternativa (si se considera crucial):** Si se desea mantener un test que de alguna manera verifique el paso de IDs, se debería modificar el `MoviesRepositoryFake` para que devuelva diferentes resultados basados en el ID pasado, y luego el test debería verificar el *resultado* del `useCase.execute()` para diferentes IDs, no el `capturedMovieId`. Sin embargo, la opción de eliminarlo es más limpia y se alinea mejor con el principio de testing de comportamiento observable.

---
**Nota:** Después de realizar los cambios, es crucial ejecutar todos los tests para asegurar que las modificaciones no introdujeron nuevas regresiones y que los tests siguen pasando.
