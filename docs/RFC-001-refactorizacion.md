# RFC-001: Refactorización del sistema BibliotecaPlus

## Línea base

La versión inicial funcional se identifica con el commit `58543d5`:

> Se conectaron los formularios de registro con la API mediante el archivo app.js

Esta versión ya contaba con interfaz, formularios, API REST, persistencia H2 y operaciones de libros, usuarios y préstamos.

## Objetivo

Refactorizar el código inicial aplicando DRY, Responsabilidad Única, Abierto/Cerrado e Inversión de Control, conservando el comportamiento funcional del sistema.

## Problemas intencionales identificados

| Código | Principio | Problema inicial | Corrección prevista |
|---|---|---|---|
| DRY-01 | DRY | Validaciones repetidas en servicios | Centralizar validaciones reutilizables |
| DRY-02 | DRY | Cálculo de multas mediante reglas repetidas | Extraer el cálculo a un componente |
| SRP-01 | Responsabilidad Única | `PrestamoService` concentra préstamo, devolución y multa | Separar las responsabilidades |
| SRP-02 | Responsabilidad Única | `PrestamoService` realiza notificaciones | Extraer la notificación |
| OCP-01 | Abierto/Cerrado | Condicionales por tipo de préstamo | Usar estrategias de tarifa |
| OCP-02 | Abierto/Cerrado | Agregar una regla exige modificar la clase principal | Permitir extensiones mediante interfaces |
| IOC-01 | Inversión de Control | Dependencias creadas directamente en el diseño inicial | Recibir dependencias por constructor |
| IOC-02 | Inversión de Control | Notificador acoplado al servicio | Inyectar un componente de notificación |

El detalle de control interno se conserva localmente en `control-interno/errores-intencionales.md` y no se sube a GitHub.

## Plan de corrección

1. Registrar la línea base y los problemas.
2. Extraer la validación y las reglas reutilizables.
3. Separar notificaciones y responsabilidades del préstamo.
4. Aplicar estrategias para las tarifas.
5. Revisar y demostrar la inyección de dependencias.
6. Ejecutar pruebas y documentar el comportamiento antes/después.

## Evidencia de Git

Cada corrección se registrará en un commit independiente, con una descripción de qué archivo se modificó, qué problema se corrigió y qué principio se aplicó.
