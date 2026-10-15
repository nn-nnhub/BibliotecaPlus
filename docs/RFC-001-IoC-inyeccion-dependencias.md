# RFC-001 · IoC: Inyección de dependencias y abstracciones

## Relación con RFC-001

Este documento registra la corrección de IoC-01 e IoC-02 dentro de la refactorización exhaustiva RFC-001.

## Hallazgo

La revisión del diseño inicial mostró que el flujo de préstamos estaba acoplado a una forma concreta de notificación. Además, la validación de dependencias debía quedar explícitamente bajo el control del contenedor Spring y no ser creada por el servicio.

## Solución aplicada

Se creó la abstracción `Notificador`:

```java
public interface Notificador {
    void notificarRegistro(Usuario usuario);
}
```

`NotificadorPrestamo` implementa la interfaz y Spring la registra como componente. `PrestamoService` ahora depende de la interfaz y la recibe por constructor:

```java
public PrestamoService(..., Notificador notificador) {
    this.notificador = notificador;
}
```

Los repositorios, validadores, calculadores y notificadores se reciben también por constructor. Ninguno se instancia directamente dentro de `PrestamoService`.

## Resultado

- El contenedor Spring controla la creación y conexión de dependencias.
- `PrestamoService` depende de una abstracción, no de una implementación concreta.
- Se puede reemplazar el notificador por correo, consola o una implementación de prueba sin modificar el servicio.
- La prueba `NotificadorPrestamoTest` verifica la implementación de forma independiente.

## Validación automatizada

Las pruebas Maven finalizaron con `BUILD SUCCESS`.
