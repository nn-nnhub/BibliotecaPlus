# Registro de commits propios · RFC-012 a RFC-014

Ethan confirmó, según lo comunicado por el usuario, una meta de **30 commits en total** durante el desarrollo de los tres RFC. La distribución inicial es aproximadamente 10 por RFC y se ajustará según avances reales y verificables. No se crearán commits vacíos ni divisiones artificiales para alcanzar la cifra.

## Criterio de conteo

- Base anterior al trabajo: `0557ad36d161cfd7947313f56a9a0b28aed217a1`.
- Contar únicamente commits propios de desarrollo de estos RFC efectivamente creados y registrados con hash; excluir commits de Dave, merges y duplicados entre ramas.
- Un commit preparado o autorizado todavía no cuenta hasta que Git confirme su creación.
- Antes de cada commit: revisar el contenido del índice, comunicar archivos, validación y mensaje propuesto, y solicitar autorización explícita. Push y PR requieren su propia autorización.
- Actualizar este registro al preparar los siguientes avances, contrastándolo con el historial real. No crear commits dedicados únicamente a incrementar el contador.

## Acumulado confirmado

| RFC | Commits propios creados | Orientación inicial |
|---|---|---|
| RFC-012 | 2 | Aproximadamente 10 |
| RFC-013 | 0 | Aproximadamente 10; aprobación y alcance por verificar |
| RFC-014 | 0 | Aproximadamente 10; aprobación y alcance por verificar |
| Total | **2 de 30** | Distribución ajustable según cambios reales |

El historial confirma `1480992` (pruebas ANTES) y `11fc483` (corrección transaccional) como los dos commits propios después de la base. RFC-013 y RFC-014 no se han iniciado en esta sesión.

## Avances registrados

| Número propuesto | RFC | Avance verificable | Estado |
|---|---|---|---|
| 1 | RFC-012 | Pruebas ANTES, documento, registro de ejecución, captura JPG auténtica y planificación del seguimiento | Creado: `1480992` |
| 2 | RFC-012 | Corrección transaccional de registrar y devolver en PrestamoService | Creado: `11fc483` |
| 3 | RFC-012 | Documentación, evidencia DESPUÉS, captura JPG auténtica y actualización de este registro | Preparado para revisión; pendiente de autorización y creación |

Mensaje del primer commit: `test(rfc-012): documentar inconsistencias antes de la corrección` (`Refs #12`).

Validación DESPUÉS de la corrección del commit `11fc483`: prueba dirigida con 3 pruebas y suite completa con 84 pruebas, ambas con 0 fallos, 0 errores y salida 0. Las aserciones originales permanecen intactas. El commit 2 ya incrementa el contador; el commit documental 3 aún no. La captura DESPUÉS está verificada en `docs/capturas/rfc-012-despues.jpg`. Tras crear el commit 3, el total real será 3. No anticipar commits como realizados.

Validación existente: `IntegridadPrestamosTest`, H2 exclusivamente en memoria, 3 pruebas, 2 fallos de consistencia, 0 errores y salida 1. Ambos fallos quedan documentados como evidencia ANTES; el flujo normal pasa. Sin cambios de producción. Referencia: [RFC-012](RFC-012.md), Issue #12.

## Organización del trabajo restante

Para RFC-012, la solución mínima y su validación DESPUÉS ya están completas. Separar la corrección de producción de la documentación y evidencia en los commits 2 y 3, con autorización individual. Evaluar cualquier trabajo adicional según necesidades reales del Issue, sin introducir cambios para completar una cuota.

Para RFC-013 y RFC-014, revisar primero los Issues y sus aprobaciones. Cada RFC seguirá el ciclo de prueba ANTES, evidencia, implementación, pruebas DESPUÉS y documentación. No se reservan diez cambios ficticios: si los avances coherentes no justifican la distribución inicial, se comunicará la diferencia antes de reorganizarla.
