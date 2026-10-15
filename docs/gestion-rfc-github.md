# Gestión de RFC en GitHub

## Propósito

El repositorio utiliza GitHub Issues para registrar, revisar y aprobar solicitudes de cambio. Cada RFC debe utilizar la plantilla `Solicitud de cambio RFC` ubicada en `.github/ISSUE_TEMPLATE/rfc.md`.

## Labels del repositorio

Las siguientes etiquetas ya fueron configuradas en GitHub:

| Label | Uso |
|---|---|
| `RFC` | Identifica una solicitud formal de cambio |
| `Bug` | Error o comportamiento incorrecto |
| `Mejora` | Cambio perfectivo o funcional |
| `Refactor` | Cambio estructural sin alterar el objetivo funcional |
| `Alta` | Prioridad alta |
| `Media` | Prioridad media |
| `Baja` | Prioridad baja |

## Reglas de etiquetado

Cada RFC debe tener:

1. La etiqueta `RFC`.
2. Una etiqueta de prioridad: `Alta`, `Media` o `Baja`.
3. Una etiqueta de tipo: `Mejora` o `Refactor`.

Si además se detecta un defecto, puede agregarse `Bug`, sin reemplazar las etiquetas obligatorias.

## Flujo de aprobación

1. Crear un Issue utilizando la plantilla RFC.
2. Completar todos los campos solicitados.
3. Agregar las etiquetas obligatorias.
4. Revisar impacto, riesgo y costo.
5. Aprobar o solicitar ajustes mediante comentarios.
6. Implementar el cambio en commits descriptivos.
7. Vincular los commits con el Issue usando `Refs #n` o `Closes #n`.
8. Cerrar el Issue cuando la prueba y la documentación estén completas.

## RFC existentes del proyecto

- RFC-001: [Refactorización exhaustiva](https://github.com/nn-nnhub/BibliotecaPlus/issues/1).
- RFC-002: [Validación de ISBN duplicado](https://github.com/nn-nnhub/BibliotecaPlus/issues/2).
- RFC-003: [Reportes y exportación compatible con Excel](https://github.com/nn-nnhub/BibliotecaPlus/issues/3).
- RFC-004: [Vencimientos y multas](https://github.com/nn-nnhub/BibliotecaPlus/issues/4).
- RFC-005: [Información ampliada de usuarios](https://github.com/nn-nnhub/BibliotecaPlus/issues/5).
- RFC-006: [Validación de DNI duplicado](https://github.com/nn-nnhub/BibliotecaPlus/issues/6).
