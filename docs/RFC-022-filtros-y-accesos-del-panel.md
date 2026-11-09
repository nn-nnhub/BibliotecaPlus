# RFC-022 · Alineación de filtros y accesos rápidos del panel

## Identificación y trazabilidad

- Solicitud [#35](https://github.com/nn-nnhub/BibliotecaPlus/issues/35).
- Propuesta del 08/11/2026. Mantenimiento perfectivo, prioridad baja y riesgo bajo.
- Estado en revisión. Consultar el PR en el [índice de RFC](indice-rfc.md).

## Objetivo y justificación

El selector de categoría y los buscadores se mostraban con tamaños y alineaciones diferentes. La tarjeta Disponibilidad compartía destino con Libros y la cabecera fija podía cubrir la sección al seguir un enlace.

## Implementación e impacto

Los buscadores y el selector comparten estilos de ancho, altura mínima y espaciado. En pantallas estrechas ocupan el ancho del panel. Usuarios abre Usuarios registrados, Libros abre Libros registrados, Préstamos abre Registrar préstamo y Disponibilidad lleva directamente a la tabla de inventario que muestra el estado de cada libro. Las anclas dejan espacio bajo la cabecera y permiten enfocar el destino.

## Pruebas y evidencias

- `node --test "src/test/js/*.test.js"` pasó 90 pruebas.
- Se comprobó en el HTML que cada tarjeta tiene un destino único y enfocable y que ambos paneles usan el grupo de filtros compartido.
- Comprobación visual pendiente. En escritorio y móvil, comparar el tamaño de los controles, probar cada tarjeta y comprobar que la cabecera deja visible el destino.

### Capturas del estado anterior

![Catálogo antes del cambio](evidencias/rfc-022/antes-catalogo.png)

![Diferencias de alineación entre catálogo y usuarios](evidencias/rfc-022/antes-alineacion.png)

![Accesos rápidos y catálogo](evidencias/rfc-022/antes-accesos.png)
