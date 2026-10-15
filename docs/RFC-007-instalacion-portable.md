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

## Implementación aprobada

La solución se implementó de forma portable:

- `Iniciar-BibliotecaPlus.bat` busca Java y Maven dentro de `.tools`.
- `bootstrap-portable.ps1` descarga Java 17 y Maven 3.9.9 si no existen.
- Los archivos se descomprimen dentro de la carpeta del proyecto.
- No se modifica el `PATH` de Windows ni se requiere permisos de administrador.
- `.tools/` está excluida de Git porque contiene herramientas descargadas localmente.

La primera ejecución requiere conexión a Internet. Las siguientes ejecuciones reutilizan las herramientas locales.

## Archivos implementados

- `Iniciar-BibliotecaPlus.bat`
- `bootstrap-portable.ps1`
- `.gitignore`
