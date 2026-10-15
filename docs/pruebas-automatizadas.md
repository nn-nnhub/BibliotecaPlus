# Pruebas automatizadas

## Herramientas

El proyecto utiliza JUnit 5 y Mockito a través de `spring-boot-starter-test`.

## Pruebas implementadas

- `UsuarioServiceTest`: verifica el rechazo de DNI duplicado y la aceptación de un DNI disponible.
- `LibroServiceTest`: verifica el rechazo de ISBN duplicado y la aceptación de un ISBN disponible.

## Ejecución

Desde la carpeta del proyecto:

```powershell
mvn test
```

También puede ejecutarse con la ruta local de Maven:

```powershell
& "C:\Users\USER\Tools\apache-maven-3.9.9\bin\mvn.cmd" test
```

## Resultado automático

Maven informa el resultado en consola y genera los reportes técnicos en `target/surefire-reports/`. En la ejecución de RFC-001 se validaron cuatro casos automatizados y todos finalizaron correctamente (`BUILD SUCCESS`). Estas pruebas no requieren capturas de pantalla.
