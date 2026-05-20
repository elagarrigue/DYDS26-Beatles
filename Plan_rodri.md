Plan de Acción para Rodri: Refactorizar Contrato de Detalle por Título
Objetivo: Modificar el flujo de obtención del detalle de una película para que utilice el título en lugar del ID, ajustando las capas de Dominio, Repositorio y Presentación, así como los tests asociados.


Etapa 1: Modificación de Interfaces de Dominio (Contratos)
Descripción: Esta etapa se enfoca en la modificación de las interfaces de la capa de dominio. Es crucial empezar aquí para establecer los nuevos contratos antes de tocar las implementaciones, siguiendo el Principio de Inversión de Dependencias (DIP) y el Principio de Segregación de Interfaces (ISP) si aplica.
Acciones:
1.
Modificar MoviesRepository Interface:
◦
Abrir el archivo de la interfaz MoviesRepository.
◦
Eliminar el método suspend fun getMovieById(id: Int): Movie?.
◦
Agregar el nuevo método suspend fun getMovieByTitle(title: String): Movie?.
2.
Modificar GetMovieDetailsUseCase Interface:
◦
Abrir el archivo de la interfaz GetMovieDetailsUseCase.
◦
Actualizar la firma del método execute para que acepte un String title en lugar de un Int id.


Etapa 2: Adaptación de Implementaciones de Dominio
Descripción: Implementar los cambios definidos en las interfaces de dominio. Esto incluye la lógica del caso de uso y la adaptación del repositorio fake para que cumpla con el nuevo contrato.
Acciones:
1.
Ajustar GetMovieDetailsUseCaseImpl Implementación:
◦
Abrir el archivo GetMovieDetailsUseCaseImpl.
◦
Actualizar la firma del método execute para que coincida con la interfaz (String title).
◦
Modificar la implementación de execute para que delegue la llamada al nuevo método getMovieByTitle(title) del MoviesRepository.
2.
Actualizar MoviesRepositoryFake Implementación:
◦
Abrir el archivo MoviesRepositoryFake.
◦
Implementar el nuevo método override suspend fun getMovieByTitle(title: String): Movie?.
◦
Dentro de este método, ajustar la lógica para que simule la búsqueda por título. Por ejemplo, podría verificar si el movieDetailResult configurado tiene un título que coincide (o contiene) el title pasado, o simplemente devolver movieDetailResult si no se necesita una lógica de búsqueda compleja en el fake.
◦
Asegurarse de que el capturedMovieId (si aún se usa para otros fines) se adapte o se elimine si ya no es relevante para la búsqueda por título.


Etapa 3: Adaptación de la Capa de Presentación (ViewModel y Navegación)
Descripción: Ajustar el ViewModel y la configuración de navegación para que utilicen el nuevo contrato de búsqueda por título.
Acciones:
1.
Ajustar DetailViewModel:
◦
Abrir el archivo DetailViewModel.
◦
Modificar la función de carga de detalles (ej. getMovieDetail) para que acepte un String title en lugar de un Int id.
◦
Actualizar la llamada al GetMovieDetailsUseCase.execute() dentro del ViewModel para pasar el title recibido.
2.
Actualizar la Navegación y la UI:
◦
Abrir el archivo donde se define la navegación (ej. NavGraphBuilder en App.kt o similar).
◦
Modificar la ruta del destino de detalle (composable) para que espere un MOVIE_TITLE como argumento (ej: route = "$DETAIL/{$MOVIE_TITLE}", arguments = listOf(navArgument(MOVIE_TITLE) { type = NavType.StringType })).
◦
En la pantalla de origen (ej. HomeScreen o donde se llama a la navegación), ajustar la función onGoodMovieClick (o similar) para que navegue utilizando it.title en lugar de it.id al construir la ruta de navegación.
◦
En la DetailScreen, obtener el movieTitle de los argumentos del backstackEntry.


Etapa 4: Actualización y Verificación de Tests
Descripción: Modificar los tests existentes para que reflejen los cambios en la lógica de búsqueda por título y asegurar que todo el sistema sigue funcionando correctamente.
Acciones:
1.
Actualizar GetMovieDetailsUseCaseImplTest:
◦
Abrir el archivo GetMovieDetailsUseCaseImplTest.
◦
Modificar los tests existentes para que pasen String title al useCase.execute() en lugar de Int id.
◦
Ajustar las configuraciones del MoviesRepositoryFake en los tests para que respondan a la búsqueda por título.
◦
Asegurarse de que los tests verifiquen el comportamiento correcto del caso de uso al recibir un título.
2.
Actualizar DetailViewModelTest:
◦
Abrir el archivo DetailViewModelTest.
◦
Modificar los tests existentes para que pasen String title a la función de carga de detalles del ViewModel (ej. viewModel.getMovieDetail(title)).
◦
Ajustar las configuraciones del FakeGetMovieDetailsUseCase en los tests para que respondan a la búsqueda por título.
◦
Asegurarse de que los tests verifiquen que el ViewModel llama correctamente al caso de uso con el título y actualiza el estado de la UI.