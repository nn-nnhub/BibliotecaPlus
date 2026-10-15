# RFC-001 · DRY-01: Centralización de validaciones repetidas

## Relación con el RFC-001

Este documento detalla la primera corrección aplicada dentro de la refactorización exhaustiva RFC-001. El objetivo es evitar que varios servicios implementen de manera independiente la misma regla básica para determinar si un texto está vacío.

## Hallazgo

Durante el análisis se observó el mismo patrón repetido en diferentes servicios:

```java
valor == null || valor.isBlank()
```

Esto dificultaba mantener la regla y podía producir comportamientos diferentes si un servicio se modificaba y otro no.

## Código anterior

En la línea funcional inicial, `UsuarioService` validaba directamente sus campos:

```java
if (usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()
        || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
    throw new IllegalArgumentException("El nombre y el correo son obligatorios");
}
```

`PrestamoService` repetía el mismo criterio para libro y usuario:

```java
if (libro == null || libro.getTitulo() == null || libro.getTitulo().isBlank()
        || usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()) {
    throw new IllegalArgumentException("El libro y el usuario son obligatorios");
}
```

## Solución aplicada

Se creó el componente reutilizable `ValidadorDatos`:

```java
@Component
public class ValidadorDatos {
    public boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public void exigirTexto(String valor, String mensaje) {
        if (estaVacio(valor)) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
```

Los servicios reciben este componente mediante inyección por constructor. Por ejemplo, `PrestamoService` ahora utiliza:

```java
if (libro == null || validadorDatos.estaVacio(libro.getTitulo())
        || usuario == null || validadorDatos.estaVacio(usuario.getNombre())) {
    throw new IllegalArgumentException("El libro y el usuario son obligatorios");
}
```

En `LibroService`, la misma abstracción se utiliza para validar el ISBN:

```java
validadorDatos.exigirTexto(isbn, "El ISBN es obligatorio");
```

## Resultado

- Se eliminó la repetición de la regla `null` o texto vacío.
- Los servicios conservan sus mensajes y reglas específicas.
- La validación común puede evolucionar desde un único componente.
- Se mantuvo la separación entre servicio y validación.
- No se modificó el comportamiento esperado de la aplicación.

## Validación automatizada

Se ejecutó:

```powershell
mvn test
```

Resultado: `BUILD SUCCESS`.

Las pruebas de `UsuarioServiceTest` y `LibroServiceTest` verifican los casos de DNI e ISBN duplicados y disponibles.

## Archivos modificados

- `src/main/java/com/bibliotecaplus/validation/ValidadorDatos.java`
- `src/main/java/com/bibliotecaplus/service/UsuarioService.java`
- `src/main/java/com/bibliotecaplus/service/LibroService.java`
- `src/main/java/com/bibliotecaplus/service/PrestamoService.java`

## Registro Git

Commit de implementación:

`ee0e585 - Se aplicó la corrección DRY-01 mediante ValidadorDatos`
