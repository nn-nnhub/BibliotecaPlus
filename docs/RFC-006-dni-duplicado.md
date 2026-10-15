# RFC 006 Validación de DNI duplicado

## Solicitud

Evitar que dos usuarios sean registrados con el mismo DNI.

## Motivo

El DNI identifica de manera única a una persona. Permitir duplicados ocasionaría registros ambiguos y dificultaría el control de préstamos.

## Implementación

- `UsuarioRepository.java`: agregó consultas `existsByDni` y `existsByDniAndIdNot`.
- `UsuarioService.java`: valida el DNI antes de guardar o actualizar.
- `app.js`: muestra el mensaje enviado por la API cuando el registro es rechazado.

## Regla

```text
Si el DNI ya existe, no se guarda el usuario.
```

## Prueba funcional

1. Registrar un usuario con un DNI nuevo.
2. Intentar registrar otro usuario usando el mismo DNI.
3. Verificar el mensaje `El DNI ya está registrado en otro usuario`.
4. Confirmar que la tabla conserva un solo registro con ese DNI.

## Capturas requeridas

- `rfc-006-formulario-dni-repetido.png`: formulario antes de guardar el DNI repetido.
- `rfc-006-rechazo-dni-repetido.png`: mensaje de rechazo mostrado por la interfaz.
- `rfc-006-tabla-sin-duplicado.png`: tabla mostrando que no se creó el segundo usuario.
