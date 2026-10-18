// Pruebas de la interfaz de préstamos (RFC-009). Ejecutar con: node --test "src/test/js/*.test.js"
const test = require('node:test');
const assert = require('node:assert');
const { cargarApp } = require('./cargar-app');

const respuestaVacia = () => Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve([]) });

test('devolver un préstamo no envía el tipo de préstamo al servidor', async () => {
    const { ejecutar, llamadas } = cargarApp({ fetch: respuestaVacia });
    llamadas.length = 0;

    await ejecutar('devolverPrestamo(5)');

    const devolucion = llamadas.find(llamada => llamada.metodo === 'PUT');
    assert.strictEqual(devolucion.url, '/api/prestamos/5/devolver');
    assert.ok(!llamadas.some(llamada => llamada.url.includes('tipoPrestamo')));
});

test('el campo de vencimiento no admite fechas anteriores a hoy', () => {
    const { nodos } = cargarApp();
    const hoy = new Date().toLocaleDateString('en-CA');

    assert.strictEqual(nodos['#fechaVencimiento'].min, hoy);
    assert.match(nodos['#fechaVencimiento'].min, /^\d{4}-\d{2}-\d{2}$/);
    assert.ok(nodos['#fechaVencimiento'].value > nodos['#fechaVencimiento'].min,
        'la fecha propuesta debe ser posterior al mínimo');
});
