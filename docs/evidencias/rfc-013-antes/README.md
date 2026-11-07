# Fuente histórico de la prueba ANTES del RFC-013

`ConcurrenciaPrestamosTest.java` conserva exactamente los bytes del arnés ejecutado antes de la corrección. SHA-256:

```text
B08514CE0B6C970270F18493BDC4892AEF3E4EC9A020FF9BB4A5A747D17B708C
```

La regla local `.gitattributes` evita normalizar sus finales de línea al guardar o recuperar el archivo. Es un artefacto documental fuera de `src/test/java`: no se compila ni se descubre como prueba activa. No hay una exclusión de esta clase en Surefire.

## Reproducción contra el código ANTES

El commit `5e64e17` ya contiene el arnés en su ubicación ejecutable original y el código anterior al bloqueo pesimista. Usar un checkout independiente de ese commit, nunca copiar este arnés sobre la solución actual: su barrera espera que dos transacciones lean el libro antes de escribir, situación que el bloqueo nuevo impide.

Ejemplo PowerShell para una futura reproducción autorizada, desde la raíz del repositorio (no se ejecutó al preparar este archivo):

```powershell
# Seleccionar el JDK 17 instalado; ajustar esta ruta al equipo.
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version

# Debe ser una carpeta nueva; no sobrescribir un checkout existente.
$copiaAntesRfc013 = Join-Path $env:TEMP 'BibliotecaPlus-rfc013-antes'
git worktree add --detach $copiaAntesRfc013 5e64e17
Push-Location $copiaAntesRfc013
try {
    mvn -B clean '-Dtest=ConcurrenciaPrestamosTest' test
    $salidaAntesRfc013 = $LASTEXITCODE
    "Codigo de salida: $salidaAntesRfc013"
} finally {
    Pop-Location
}
```

Este comando selecciona únicamente el arnés, cuya configuración exige `jdbc:h2:mem:rfc013`. No iniciar la aplicación con su configuración por defecto ni abrir `./data/bibliotecadb`. Registrar commit, JDK, comando, informes y resultados reales de cada nueva reproducción por separado.

La ejecución histórica obtuvo tres repeticiones con dos préstamos activos por libro y un control correcto de libros diferentes: 4 ejecuciones individuales, 3 fallos; resumen Maven agrupado de 2 pruebas con 1 fallo y salida 1. Es evidencia de aquella ejecución, no un resultado garantizado sin volver a ejecutar.

## Cobertura vigente

En la rama actual, `ConcurrenciaPrestamosDespuesTest` contiene seis ejecuciones: tres de exclusión sobre el mismo libro, una de libros diferentes, una de timeout HTTP 409 y una de rollback/liberación del bloqueo. También se ejecutan las tres pruebas originales de integridad del RFC-012. No se han debilitado ni eliminado esas pruebas activas.

Registro histórico: [rfc-013-antes-despues.txt](../rfc-013-antes-despues.txt). Las capturas y ese registro no se reescriben al trasladar el fuente.
