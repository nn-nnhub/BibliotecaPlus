# RFC 004 Control de vencimientos y multas

## Solicitud

Incorporar fechas de vencimiento en los préstamos y calcular automáticamente una multa cuando un libro se devuelve después de la fecha límite.

## Motivo

La versión anterior registraba únicamente la fecha del préstamo. El usuario no podía conocer la fecha límite ni observar una multa desde la interfaz.

## Alcance

- Agregar fecha de vencimiento al préstamo.
- Mostrar fecha de vencimiento en el historial.
- Calcular días de retraso al devolver.
- Calcular la multa según el tipo de préstamo.
- Mostrar la multa en la interfaz.

## Reglas de negocio

| Tipo | Tarifa por día de retraso |
|---|---:|
| DOMICILIO | S/ 1.00 |
| ESPECIAL | S/ 2.00 |
| Otro | S/ 0.50 |

La multa se calcula así:

```text
multa = días de retraso × tarifa diaria
```

## Archivos modificados

- `Prestamo.java`: agregó `fechaVencimiento`.
- `PrestamoService.java`: calculó los días de retraso y la multa.
- `PrestamoController.java`: recibió la fecha desde la API.
- `index.html`: agregó el campo de fecha y columnas de vencimiento y multa.
- `app.js`: conectó el formulario y mostró los resultados.

## Desarrollo y control de calidad

1. Se implementó el campo de vencimiento en el modelo.
2. Se conectó la fecha al servicio y al controlador.
3. Se incorporó la fecha en el formulario web.
4. Se realizó una primera prueba con un préstamo vencido.
5. La primera prueba detectó que la multa resultaba `S/ 0.00` por aplicar dos veces la resta de días permitidos.
6. Se corrigió el método de cálculo para recibir directamente los días de retraso.
7. Se repitió la prueba y se obtuvo una multa correcta de `S/ 5.00`.

## Prueba final ejecutada

Se creó un préstamo con fecha de vencimiento cinco días antes de la fecha de devolución:

```text
Fecha de vencimiento: 2026-10-09
Fecha de devolución: 2026-10-14
Tipo: DOMICILIO
Días de retraso: 5
Multa esperada: S/ 5.00
Multa obtenida: S/ 5.00
```

## Capturas que deben tomarse

1. **Formulario de préstamo:** antes de presionar Registrar préstamo, mostrando libro, usuario, tipo y fecha de vencimiento.
2. **Préstamo activo:** inmediatamente después de registrarlo, mostrando la fecha de vencimiento y multa `S/ 0.00`.
3. **Devolución:** después de presionar Devolver.
4. **Resultado final:** historial mostrando `Sí` en Devuelto y la multa calculada.

Guardar las capturas en `docs/capturas/` con estos nombres sugeridos:

```text
rfc-004-formulario.png
rfc-004-prestamo-activo.png
rfc-004-devolucion.png
rfc-004-multa-calculada.png
```
