# RFC-007: Instalación portable de Java y Maven

## Solicitud de cambio (RFC)

Título del cambio: preparar el iniciador para computadoras Windows sin Java ni Maven configurados.

## Descripción del cambio

Modificar el proceso de inicio para detectar Java y Maven, utilizar herramientas locales cuando existan y ofrecer instalación automática mediante un administrador de paquetes cuando sea necesario.

## Justificación

El `.bat` actual utiliza una ruta fija de Maven del equipo de desarrollo. En otra computadora, la aplicación no iniciará si Java o Maven no están instalados y configurados.

## Impacto en el sistema

`Iniciar-BibliotecaPlus.bat`, documentación de instalación y configuración del entorno Windows.

## Tipo de mantenimiento

Adaptativo / Perfectivo

## Riesgo

Medio, porque requiere Internet, permisos de instalación y compatibilidad con la política del equipo Windows.

## Prioridad

Media

## Costo estimado

Bajo

## Responsable

nn-nnhub

## Fecha propuesta

15/10/2026

## Consideración técnica

La instalación automática no se incorporó todavía al `.bat` porque no debe instalar software sin informar al usuario ni asumir que todas las computadoras permiten `winget`. Primero debe definirse si la entrega permitirá conexión a Internet y permisos de administrador.
