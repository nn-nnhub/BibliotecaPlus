# Pruebas automatizadas

## Herramientas

- **Java:** JUnit 5, Mockito y Spring Boot Test a través de `spring-boot-starter-test`. Se ejecutan con Maven (`mvn test`).
- **JavaScript:** el módulo `node:test` de Node.js, sin dependencias adicionales. **No forman parte de Maven**: se ejecutan con un comando aparte.

Las pruebas de integración usan una base H2 **en memoria** con un nombre propio por clase (`jdbc:h2:mem:...`) y `spring.jpa.show-sql=false`, por lo que no leen ni modifican la base persistente `./data`.

## Pruebas Java (`src/test/java/`)

| Clase | Casos | Qué verifica |
|---|---|---|
| `CalculadorMultaTest` | 6 | Tarifas por tipo, orden de tarifas, retraso 0 y ausencia de tarifa (RFC-009) |
| `PrestamoServiceTest` | 7 | Tipo normalizado, vencimiento pasado, multa por tipo guardado, histórico y doble devolución (RFC-009) |
| `ValidadorPrestamoTest` | 5 | Libro y usuario obligatorios; vencimiento nulo, pasado, de hoy y futuro (RFC-009) |
| `MultaPorTipoPrestamoTest` | 5 | Multa de Domicilio, Especial y Sala por HTTP, sin depender del tipo enviado al devolver (RFC-009) |
| `LibroServiceTest` | 10 | ISBN duplicado, título y autor, disponibilidad y eliminación (RFC-010) |
| `UsuarioServiceTest` | 8 | DNI duplicado, formato de DNI y correo, y eliminación (RFC-010) |
| `ValidadorDatosTest` | 4 | Formatos de DNI y correo (RFC-010) |
| `ManejadorErroresTest` | 3 | Mapeo de violaciones de integridad (RFC-010) |
| `ManejoErroresTest` | 7 | Respuestas 400 y 404 con mensaje JSON (RFC-010) |
| `ValidacionLibrosHttpTest` | 5 | Validación y disponibilidad de libros por HTTP (RFC-010) |
| `ValidacionUsuariosHttpTest` | 5 | DNI y correo de usuarios por HTTP (RFC-010) |
| `EliminacionConPrestamosTest` | 3 | Bloqueo de la eliminación con préstamos y 404 (RFC-010) |
| `ExportadorCsvTest` | 5 | Neutralización de fórmulas y escape del CSV (RFC-011) |
| `ExportacionCsvTest` | 1 | CSV exportado por HTTP con fórmula en el título (RFC-011) |
| `ReporteControllerTest` | 2 | Contrato del resumen y de la descarga CSV (RFC-011) |
| `ReporteServiceTest` | 4 | Equivalencia de los reportes y número de consultas SQL (RFC-011) |
| `NotificadorPrestamoTest` | 1 | Registro de la notificación de préstamo (RFC-001) |

Total: **81 casos** en la última ejecución.

## Pruebas JavaScript (`src/test/js/`)

`app.js` se ejecuta en un contexto `vm` de Node con un `document` simulado (`cargar-app.js`); no se usa un navegador.

| Archivo | Casos | Qué verifica |
|---|---|---|
| `xss.test.js` | 5 | Los datos con HTML se muestran como texto y el formato normal no cambia (RFC-011) |
| `escape.test.js` | 3 | La función `escaparHtml` (RFC-011) |
| `prestamos.test.js` | 2 | La devolución no envía el tipo y el campo de fecha tiene como mínimo hoy (RFC-009) |

Total: **10 casos** en la última ejecución.

## Ejecución

Con el JDK 17 y el Maven portables del proyecto, desde su carpeta:

```powershell
$env:JAVA_HOME="$PWD\.tools\jdk-17"
& "$PWD\.tools\apache-maven-3.9.9\bin\mvn.cmd" -B test
```

Con un Maven instalado en el sistema basta `mvn test`. Para ejecutar una sola clase o método:

```powershell
mvn -B test "-Dtest=MultaPorTipoPrestamoTest"
mvn -B test "-Dtest=ManejoErroresTest#vencimientoPasadoResponde400ConMensaje"
```

Las pruebas JavaScript requieren Node.js; se ejecutaron con la versión 24, donde el patrón debe ir entre comillas:

```powershell
node --test "src/test/js/*.test.js"
```

## Resultado

Maven informa el resultado en consola y genera los reportes técnicos en `target/surefire-reports/`; Node.js imprime un resumen con las pruebas aprobadas y fallidas. La salida literal de ejecuciones anteriores y posteriores a las correcciones de RFC-009, RFC-010 y RFC-011 se conserva en `docs/evidencias/`.
