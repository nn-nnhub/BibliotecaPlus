# RFC-001 · SRP: Separación de responsabilidades del préstamo

## Relación con RFC-001

Este documento registra la corrección de los hallazgos SRP-01 y SRP-02 dentro de la refactorización exhaustiva RFC-001.

## Hallazgos

`PrestamoService` concentraba el flujo de préstamos, la validación de libro y usuario y la notificación del registro. Esto hacía que una modificación en una responsabilidad pudiera afectar las demás.

## Código anterior

La validación estaba dentro del servicio principal:

```java
private void validarPrestamo(Libro libro, Usuario usuario) {
    if (libro == null || libro.getTitulo() == null || libro.getTitulo().isBlank()
            || usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()) {
        throw new IllegalArgumentException("El libro y el usuario son obligatorios");
    }
}
```

La notificación también estaba acoplada al flujo de persistencia:

```java
System.out.println("Notificación: préstamo registrado para " + usuario.getCorreo());
```

## Solución aplicada

- `ValidadorPrestamo`: concentra la validación de las entidades necesarias.
- `NotificadorPrestamo`: concentra la notificación y utiliza un logger.
- `PrestamoService`: coordina el caso de uso e inyecta ambas dependencias por constructor.

```java
validadorPrestamo.validar(libro, usuario);
notificadorPrestamo.notificarRegistro(usuario);
```

## Resultado

- `PrestamoService` tiene una responsabilidad de coordinación más clara.
- La validación puede probarse sin persistencia.
- La notificación puede cambiarse sin modificar el flujo del préstamo.
- Se eliminó el uso directo de `System.out`.

## Validación automatizada

`ValidadorPrestamoTest` verifica la aceptación de datos válidos y el rechazo de un libro sin título. Las pruebas Maven finalizaron con `BUILD SUCCESS`.
