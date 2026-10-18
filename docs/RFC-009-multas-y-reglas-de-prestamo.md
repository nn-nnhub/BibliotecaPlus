# RFC-009: Corrección de multas y reglas de préstamo

## Solicitud de cambio (RFC)

Título del cambio: RFC-009 — Corrección de multas y reglas de préstamo.

Issue: #9.

## Descripción del cambio

- Guardar el tipo de préstamo (`DOMICILIO`, `ESPECIAL`, `SALA`) en `Prestamo` al registrarlo y usarlo al devolver, en lugar del parámetro enviado por el cliente.
- Rechazar fechas de vencimiento nulas o anteriores a hoy.
- Seleccionar la tarifa de multa sin depender del orden de la lista inyectada.
- Retirar el parámetro `tipoPrestamo` de la devolución en la interfaz y limitar el campo de fecha al día actual.

## Justificación

Defectos verificados con pruebas en un entorno aislado (H2 en memoria, sin tocar `./data`):

| ID | Evidencia antes de la corrección |
|---|---|
| H1 | Un préstamo ESPECIAL con 10 días de retraso, devuelto como DOMICILIO (lo que enviaba la interfaz), generaba S/ 10.00 en lugar de S/ 20.00. |
| H5 | Se aceptaba un vencimiento 10 días en el pasado. |
| H6 | Con la lista `[General, Domicilio, Especial]`, `calcular("ESPECIAL")` devolvía 0.5 en lugar de 2.0. En ejecución normal funcionaba solo por el orden alfabético de los beans. |

## Impacto en el sistema

`Prestamo`, `PrestamoService`, `PrestamoController`, `CalculadorMulta`, `TarifaMulta`, `TarifaGeneral`, `ValidadorPrestamo`, `app.js` y las pruebas asociadas.

## Tipo de mantenimiento

Correctivo

## Riesgo

Medio

## Prioridad

Alta

## Costo estimado

Medio

## Responsable

Pendiente de asignación

## Fecha propuesta

17/10/2026

## Etiquetas

RFC · Alta · Refactor · bug

## Implementación

- `Prestamo.java`: nuevo campo `tipoPrestamo`; `getTipoPrestamo()` devuelve `DOMICILIO` cuando el valor es nulo.
- `PrestamoService.java`: `registrar` guarda el tipo normalizado (mayúsculas, sin espacios; vacío equivale a `DOMICILIO`) y `devolver(id)` calcula la multa con el tipo guardado.
- `PrestamoController.java`: `PUT /api/prestamos/{id}/devolver` ya no recibe `tipoPrestamo`.
- `TarifaMulta.java` y `TarifaGeneral.java`: método `esPorDefecto()`; la tarifa general solo se usa si ninguna específica aplica.
- `CalculadorMulta.java`: elige primero una tarifa específica y recurre a la general.
- `ValidadorPrestamo.java`: `validarVencimiento` rechaza fechas nulas o anteriores a hoy (hoy sí es válido).
- `app.js`: la devolución no envía el tipo y `#fechaVencimiento` usa `min` con la fecha local (no UTC, que en Perú adelantaría el día después de las 19:00).

## Compatibilidad con préstamos históricos

La columna `tipoPrestamo` es opcional, por lo que `ddl-auto=update` la agrega sin alterar los datos. Los préstamos anteriores quedan con valor nulo y se tratan como `DOMICILIO`, que era el valor que enviaba la interfaz. Se comprobó con una base creada con el código anterior y abierta con el nuevo: el préstamo histórico se leyó como `DOMICILIO` y se devolvió con la multa esperada. La prueba no se ejecutó sobre la base real `./data`.

## Pruebas

- `MultaPorTipoPrestamoTest` (integración): reproduce H1 de extremo a extremo.
- `CalculadorMultaTest`: las 6 permutaciones de orden, retraso 0 y ausencia de tarifa.
- `ValidadorPrestamoTest`: vencimiento nulo, pasado, de hoy y futuro.
- `PrestamoServiceTest`: tipo normalizado, tipo por defecto, vencimiento pasado, tarifa por tipo, préstamo histórico, devolución a tiempo y doble devolución.

Resultado: 24 pruebas, 0 fallos (`mvn test`, JDK 17 portable).

## Decisiones pendientes

- **Tipos desconocidos:** un tipo como `XYZ` se registra tal cual y se multa con la tarifa general (S/ 0.50 por día). No existe una regla documentada que lo rechace; queda pendiente decidir si debe rechazarse o mantenerse.
- **Contrato HTTP:** el parámetro `tipoPrestamo` de la devolución se descarta. Los clientes externos que lo enviaban no reciben error, pero tampoco lo usan.
- **Base real:** falta comprobar la migración sobre `./data` con la aplicación cerrada.

## Evidencia

- `docs/capturas/rfc-009-estado-inicial.png`: cambios locales antes del primer commit.
- `docs/capturas/rfc-009-commit.png`: primer commit del RFC.
