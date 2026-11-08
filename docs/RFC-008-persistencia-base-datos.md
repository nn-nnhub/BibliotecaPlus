# RFC-008 · Persistencia local de la base de datos

## Identificación y trazabilidad

| Campo | Referencia |
|---|---|
| Issue | [#8](https://github.com/nn-nnhub/BibliotecaPlus/issues/8) |
| Pull request | Sin PR asociado en el historial consultado |
| Estado | Integrado en `main`; issue cerrado |

## Objetivo y justificación

Conservar datos al reiniciar BibliotecaPlus.

## Implementación e impacto

H2 en archivo local excluido de Git.

## Pruebas y evidencias

Verificación de persistencia y procedimiento de limpieza documentado. Los resultados y capturas originales se conservan en el registro detallado.

## Registro detallado

### Solicitud de cambio (RFC)

Título del cambio: conservar los datos de BibliotecaPlus entre reinicios.

### Descripción del cambio

Cambiar H2 de memoria a una base de datos basada en archivo dentro de `data/`.

### Justificación

La configuración anterior usaba `jdbc:h2:mem`, por lo que los libros, usuarios y préstamos desaparecían al cerrar la aplicación.

### Impacto en el sistema

`application.properties`, `DataInitializer.java`, configuración de Git y carpeta local `data/`.

### Tipo de mantenimiento

Perfectivo / Adaptativo

### Riesgo

Medio

### Prioridad

Alta

### Costo estimado

Bajo

### Implementación

La URL pasó a ser:

```properties
spring.datasource.url=jdbc:h2:file:./data/bibliotecadb
spring.jpa.hibernate.ddl-auto=update
```

El inicializador solo carga los datos de ejemplo cuando la base está vacía. Así, los registros creados desde la interfaz no se duplican al reiniciar.

La carpeta `data/` se excluye de Git porque contiene datos locales de ejecución.

### Limpieza inicial

Se eliminó la base local de prueba antes de activar la persistencia, por lo que la primera ejecución comienza con los datos iniciales configurados. A partir de ese momento, los nuevos registros quedan guardados entre reinicios.
