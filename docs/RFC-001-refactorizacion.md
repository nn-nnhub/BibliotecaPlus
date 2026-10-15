# RFC-001: Refactorización del sistema BibliotecaPlus

## Objetivo

Después del diagnóstico del software inicial, se solicita refactorizar la arquitectura interna aplicando DRY, Responsabilidad Única, Abierto/Cerrado e Inversión de Control, conservando el comportamiento funcional del sistema.

## Diagnóstico del software inicial

| Código | Principio | Hallazgo del diagnóstico | Corrección prevista |
|---|---|---|---|
| DRY-01 | DRY | Validaciones repetidas en servicios | Centralizar validaciones reutilizables |
| DRY-02 | DRY | Cálculo de multas mediante reglas repetidas | Extraer el cálculo a un componente |
| SRP-01 | Responsabilidad Única | `PrestamoService` concentra préstamo, devolución y multa | Separar las responsabilidades |
| SRP-02 | Responsabilidad Única | `PrestamoService` realiza notificaciones | Extraer la notificación |
| OCP-01 | Abierto/Cerrado | Condicionales por tipo de préstamo | Usar estrategias de tarifa |
| OCP-02 | Abierto/Cerrado | Agregar una regla exige modificar la clase principal | Permitir extensiones mediante interfaces |
| IOC-01 | Inversión de Control | Dependencias creadas directamente en el diseño inicial | Recibir dependencias por constructor |
| IOC-02 | Inversión de Control | Notificador acoplado al servicio | Inyectar un componente de notificación |

El detalle técnico de revisión se conserva localmente en `control-interno/errores-intencionales.md` y no se sube a GitHub.

## Plan de corrección

1. Registrar el diagnóstico y los hallazgos.
2. Extraer la validación y las reglas reutilizables.
3. Separar notificaciones y responsabilidades del préstamo.
4. Aplicar estrategias para las tarifas.
5. Revisar y demostrar la inyección de dependencias.
6. Ejecutar pruebas y documentar el comportamiento antes/después.

## Evidencia de Git

Cada corrección se registrará en un commit independiente, con una descripción de qué archivo se modificó, qué problema se corrigió y qué principio se aplicó.
