# Configurar JAVA_HOME manualmente

No se encontró Java en las rutas típicas de Windows. Como estás usando **IntelliJ IDEA**, sigue estos pasos:

## Opción 1: Desde IntelliJ IDEA (Recomendado)

1. Abre IntelliJ IDEA
2. Ve a **File** → **Project Structure** (o presiona Ctrl+Alt+Shift+S)
3. En la izquierda, selecciona **Project**
4. Busca **SDK** o **Project SDK**
5. Si hay un JDK/SDK configurado, anota la ruta que aparece
6. Copia esa ruta (ejemplo: `C:\Users\sanhe\.jdks\jdk-21.0.1`)

## Opción 2: Configurar manualmente en Windows

Si encontraste la ruta del JDK:

### En PowerShell (sesión actual):
```powershell
# Reemplaza con tu ruta real
$env:JAVA_HOME = "C:\ruta\a\tu\jdk"

# Verifica que funcione
java -version

# Luego prueba Gradle
cd "C:\Users\sanhe\OneDrive\Documentos\Kotlin\DYDS26-Beatles"
./gradlew.bat --version
```

### Para hacerlo permanente (todo el sistema):

1. **Busca "Variables de entorno"** en Windows
2. Haz clic en **"Editar variables de entorno del sistema"**
3. Haz clic en **"Variables de entorno..."** (abajo a la derecha)
4. Haz clic en **"Nueva..."** en la sección "Variables de usuario"
5. **Nombre de variable**: `JAVA_HOME`
6. **Valor de la variable**: Tu ruta JDK (ejemplo: `C:\Users\sanhe\.jdks\jdk-21.0.1`)
7. Haz clic en **"Aceptar"** dos veces
8. **Reinicia la terminal PowerShell**

## Opción 3: Configurar en gradle.properties

Agrega esta línea al archivo `gradle.properties`:

```properties
org.gradle.java.home=C:\ruta\a\tu\jdk
```

## ¿Dónde está mi JDK?

Típicamente está en:
- `C:\Users\[TuUsuario]\.jdks\jdk-*` (descargado por IntelliJ)
- `C:\Program Files\Java\jdk-*` (instalación manual)
- `C:\Program Files\JetBrains\IntelliJ IDEA\jbr` (JetBrains Runtime de IntelliJ)

Intenta explorar estas carpetas en el Explorador de archivos.

## Verificar después de configurar

```powershell
# Verifica que Java funciona
java -version

# Intenta ejecutar Gradle
cd "C:\Users\sanhe\OneDrive\Documentos\Kotlin\DYDS26-Beatles"
./gradlew.bat --version
```

¿Necesitas ayuda para encontrar tu JDK? Revisa el proyecto en IntelliJ IDEA.

