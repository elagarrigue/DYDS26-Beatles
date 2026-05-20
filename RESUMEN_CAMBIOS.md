# Resumen general de lo informado sobre la Etapa 3

## Cambio funcional solicitado

Se desea integrar **OMDB** (`https://www.omdbapi.com/`) para enriquecer el detalle de una película.

### Punto importante

* **La lista de películas no cambia**.
* El cambio aplica solo al **detalle** de una película.
* En lugar de buscar el detalle por **id**, se debe buscar por **title**.
* El id actual de la lista corresponde a **TMDB**, pero no tiene correlación directa con OMDB.
* Ambos servicios soportan búsqueda por título.

## Reglas de negocio para construir el `Movie` resultante

1. **Si TMDB y OMDB retornan resultado**

   * Se debe construir un `Movie` combinando propiedades de ambos.
2. **Si solo uno de los servicios retorna resultado**

   * Se devuelve ese resultado.
   * Se debe modificar el `overview` agregando:

     * `TMDB: ` si el resultado vino de TMDB
     * `OMDB: ` si el resultado vino de OMDB
3. **Si ninguno retorna resultado**

   * Se devuelve vacío / nulo, según el contrato del proyecto.

## Tareas funcionales indicadas

### 1\. Reemplazar TMDB get movie details by id por search movie by title

* Cambiar el flujo de detalle desde búsqueda por id a búsqueda por título.
* Actualizar los tests afectados.
* Esta tarea fue acompañada por imágenes referidas como `blobid1` a `blobid6`.

### 2\. Reestructurar la fuente externa de TMDB

* Mover la implementación de `MoviesExternalSource` a `data/external/tmdb`.
* Renombrar `MoviesExternalSourceImpl` a `TMDBMoviesExternalSource`.
* OMDB solo debe usarse para el detalle de una película, no para listar películas.
* Se señaló que `MoviesExternalSource` incumple ISP, por lo que se debe separar la interfaz.
* Esta parte fue referida con `blobid7`.
* También se deben actualizar los tests.

### 3\. Agregar el servicio OMDB

* Implementar la fuente externa para OMDB.
* En este paso incluso se puede reemplazar TMDB por OMDB para obtener la película por título.
* Esta etapa fue referida con `blobid8`, `blobid9`, `blobid10` y `blobid11`.

### 4\. Agregar un Broker

* Crear un Broker que implemente la interfaz de detalle y dependa de:

  * `TMDBMoviesExternalSource`
  * `OMDBMoviesExternalSource`
* El Broker debe resolver la lógica combinada de resultados.
* Esta etapa fue referida con `blobid12`.
* También se deben implementar tests para cubrir los casos del Broker.

## Plan de trabajo propuesto

Se propuso dividir el trabajo en tres personas:

### Rodri

* Refactorizar el contrato del detalle para buscar por `title`.
* Ajustar dominio, repository y tests afectados.

### Agos

* Separar la interfaz de `MoviesExternalSource`.
* Mover y renombrar la implementación de TMDB.
* Actualizar tests de infraestructura.

### Santi

* Implementar OMDB.
* Crear el Broker.
* Agregar tests del Broker.

## Trabajo en paralelo

Sí, se puede trabajar en paralelo, con estas condiciones:

* **Rodri y Agos** pueden avanzar casi al mismo tiempo.
* **Santi** puede preparar diseño y tests preliminares, pero la implementación final depende del nuevo contrato e interfaces.

## Secuencia recomendada

1. Cambiar el contrato del detalle a búsqueda por título.
2. Separar interfaces y reestructurar TMDB.
3. Agregar OMDB.
4. Implementar el Broker.
5. Ajustar y validar tests finales.

## Objetivo final

* Mantener la lista de películas sin cambios.
* Obtener el detalle de una película por título.
* Combinar datos de TMDB y OMDB cuando ambos estén disponibles.
* Dar fallback correcto si solo uno responde.
* Devolver vacío si ninguno responde.

## Observación adicional

Durante la conversación también se creó un resumen previo de la etapa 2 (`PR\_ETAPA\_2\_DETAILED.md`), que documenta el estado anterior del proyecto y sus tests, pero no forma parte directa del cambio de etapa 3.

