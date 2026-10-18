# RFC-010: Integridad y validación de datos

## Solicitud de cambio (RFC)

Título del cambio: RFC-010 — Integridad y validación de datos.

Issue: #10.

## Descripción del cambio

- Centralizar el manejo de errores HTTP en un `@RestControllerAdvice`: 404 para recursos inexistentes, 400 para validación y mensajes de integridad según la restricción violada.
- Validar título y autor de los libros (al guardar y actualizar) y el ISBN al actualizar; la disponibilidad deja de ser editable desde el cliente.
- Validar el formato del DNI (8 dígitos) y del correo de los usuarios.
- Impedir eliminar libros o usuarios con préstamos asociados y responder 404 al eliminar un recurso inexistente.

## Justificación

Defectos verificados con pruebas en un entorno aislado (H2 en memoria, sin tocar `./data`):

| ID | Evidencia antes de la corrección |
|---|---|
| H8 | 5 de 7 casos fallaban: libro o usuario inexistente respondía 400 en lugar de 404, y `POST /api/prestamos` con libro inexistente, `PUT /devolver` inexistente y vencimiento pasado lanzaban una excepción sin manejar (500). Los handlers estaban duplicados en dos controladores y `PrestamoController` no tenía ninguno. |
| H4a | Se guardaba un libro sin título ni autor o con `disponible=false`; `actualizar` aceptaba ISBN, título y autor vacíos y un `PUT` sin el campo `disponible` marcaba como disponible un libro prestado. 5 de 7 pruebas fallaban. |
| H4b | Se aceptaban DNI como `abc`, `7000000` o `700000012` y correos como `no-es-correo` o `carlos@`. |
| H3 | `DELETE` de un libro o usuario con préstamo respondía 204 y dejaba el préstamo huérfano (los préstamos guardan solo `libroId` y `usuarioId`, sin clave foránea); `DELETE` de un id inexistente también respondía 204. |

## Impacto en el sistema

`ManejadorErrores` y `RecursoNoEncontradoException` (nuevos), `LibroController`, `UsuarioController`, `LibroService`, `UsuarioService`, `PrestamoService`, `ValidadorDatos`, `PrestamoRepository` y las pruebas asociadas.

## Tipo de mantenimiento

Correctivo / Preventivo

## Riesgo

Medio (cambian códigos de respuesta HTTP y se agregan rechazos)

## Prioridad

Alta

## Costo estimado

Medio

## Responsable

Pendiente de asignación

## Fecha propuesta

17/10/2026

## Etiquetas

RFC · Alta · Mejora · bug

## Implementación

- `ManejadorErrores.java`: `RecursoNoEncontradoException` → 404; `IllegalArgumentException` → 400; `DataIntegrityViolationException` → 400 con mensaje de ISBN, de DNI o genérico. Se eliminaron los handlers de `LibroController` y `UsuarioController`.
- `RecursoNoEncontradoException.java`: extiende `IllegalArgumentException` para no romper el código existente.
- `LibroService.java`: `validarTituloYAutor`, ISBN obligatorio al actualizar, `guardar` registra siempre el libro como disponible y `actualizar` ya no modifica `disponible`.
- `ValidadorDatos.java` y `UsuarioService.java`: `esDniValido` (exactamente 8 dígitos) y `esCorreoValido` (`algo@dominio.ext` sin espacios), aplicados en `guardar` y `actualizar`. El teléfono no se valida porque no existe una regla documentada.
- `PrestamoRepository.java`: `existsByLibroId` y `existsByUsuarioId`.
- `LibroService.eliminar` y `UsuarioService.eliminar`: buscan primero (404) y rechazan si existen préstamos, activos o devueltos, para conservar el historial.

## Pruebas

- `ManejoErroresTest` (integración, 7 casos) y `ManejadorErroresTest` (mapeo de restricciones).
- `LibroServiceTest`, `UsuarioServiceTest` y `ValidadorDatosTest`: validaciones y eliminación.
- `EliminacionConPrestamosTest` (integración): bloqueo, borrado permitido sin préstamos y 404.

Resultado tras cada cambio: 34, 39, 46 y 55 pruebas, 0 fallos (`mvn test`, JDK 17 portable).

## Decisiones pendientes

- **HTTP 400 frente a 409:** el rechazo de eliminación con préstamos responde 400 por coherencia con las demás validaciones. Semánticamente sería 409 (Conflict); queda por decidir.
- **404 para préstamos con referencias inexistentes:** `POST /api/prestamos` con un `libroId` o `usuarioId` inexistente ahora responde 404 (antes era una excepción sin manejar, 500). Es un cambio de contrato; queda por confirmar que es el comportamiento deseado.
- **Compatibilidad y migración de `./data`:** no se modifica el esquema. Los registros existentes con DNI o correo inválidos no se alteran, pero fallarán al actualizarlos. La validación no se ejecutó sobre la base real, que permanecía bloqueada por la aplicación en ejecución.
- **Eliminación en la interfaz:** la interfaz no ofrece botón para eliminar; el bloqueo solo se ejerce vía API.

## Evidencia

- `docs/capturas/rfc-010-libros-pruebas.png`: 39 pruebas aprobadas tras validar libros (H4a).
- `docs/capturas/rfc-010-usuarios-pruebas.png`: 46 pruebas aprobadas tras validar DNI y correo (H4b).
- `docs/capturas/rfc-010-eliminacion-pruebas.png`: 55 pruebas aprobadas tras bloquear la eliminación (H3).
