# RFC-001 · DRY-02: Separación del cálculo de multas

## Relación con RFC-001

Este documento registra la segunda corrección de la refactorización exhaustiva RFC-001. Se separa el cálculo de multas para que no permanezca mezclado con el flujo de devolución de préstamos.

## Hallazgo

`PrestamoService` gestionaba la devolución, buscaba entidades, actualizaba la disponibilidad y además calculaba las tarifas por tipo de préstamo. Esto concentraba reglas que pueden reutilizarse y probarse de forma independiente.

## Código anterior

```java
private double calcularMulta(String tipoPrestamo, long diasRetraso) {
    double tarifaDiaria;

    if ("ESPECIAL".equalsIgnoreCase(tipoPrestamo)) {
        tarifaDiaria = 2.0;
    } else if ("DOMICILIO".equalsIgnoreCase(tipoPrestamo)) {
        tarifaDiaria = 1.0;
    } else {
        tarifaDiaria = 0.5;
    }

    return diasRetraso * tarifaDiaria;
}
```

## Solución aplicada

Se creó el componente `CalculadorMulta`:

```java
@Component
public class CalculadorMulta {
    public double calcular(String tipoPrestamo, long diasRetraso) {
        double tarifaDiaria = obtenerTarifa(tipoPrestamo);
        return diasRetraso * tarifaDiaria;
    }
}
```

`PrestamoService` recibe el componente por constructor y delega el cálculo:

```java
prestamo.setMulta(calculadorMulta.calcular(tipoPrestamo, diasRetraso));
```

## Resultado

- El flujo de devolución permanece en `PrestamoService`.
- Las tarifas y el cálculo se concentran en `CalculadorMulta`.
- El componente puede reutilizarse en reportes u otras operaciones.
- Las reglas pueden probarse sin iniciar toda la aplicación.

## Validación automatizada

`CalculadorMultaTest` verifica las tarifas de domicilio, especial y general. La ejecución de `mvn test` finalizó con `BUILD SUCCESS`.

## Archivos modificados

- `src/main/java/com/bibliotecaplus/service/CalculadorMulta.java`
- `src/main/java/com/bibliotecaplus/service/PrestamoService.java`
- `src/test/java/com/bibliotecaplus/service/CalculadorMultaTest.java`
