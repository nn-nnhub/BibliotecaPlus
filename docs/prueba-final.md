# Prueba final del proyecto

## Fecha

15/10/2026

## Ejecución automatizada

Se ejecutó una limpieza completa, compilación y ejecución de pruebas:

```powershell
& "C:\Users\USER\Tools\apache-maven-3.9.9\bin\mvn.cmd" clean test
```

## Resultado

- Pruebas ejecutadas: 10.
- Fallos: 0.
- Errores: 0.
- Omitidas: 0.
- Resultado Maven: `BUILD SUCCESS`.

Las pruebas cubren validación de ISBN, validación de DNI, cálculo de multas, validación de préstamos y notificación desacoplada. Maven genera el detalle en `target/surefire-reports/`.

## Revisión del iniciador de Windows

El archivo `Iniciar-BibliotecaPlus.bat` abre la aplicación automáticamente, pero la versión actual depende de una ruta fija de Maven:

```text
C:\Users\USER\Tools\apache-maven-3.9.9\bin\mvn.cmd
```

Por ello funciona en el equipo de desarrollo, pero no garantiza ejecución en otra computadora que no tenga Java y Maven configurados. Esta limitación se registró como RFC-007.
