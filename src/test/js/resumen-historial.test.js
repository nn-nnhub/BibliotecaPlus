const test = require('node:test');
const assert = require('node:assert/strict');
const { cargarApp } = require('./cargar-app');

const respuesta = datos => ({ ok: true, json: async () => datos });
const resumen = { totalPrestamos: 1, prestamosActivos: 0, prestamosDevueltos: 1, totalMultas: 0, librosMasPrestados: [] };

test('el botón muestra y oculta el resumen sin pedirlo al cerrar', async () => {
    const app = cargarApp({ fetch: async url => respuesta(url === '/api/reportes/resumen' ? resumen : []) });
    const boton = app.nodos['#btnResumen'];
    await boton.listeners.click();
    assert.match(app.nodos['#reporteResumen'].textContent, /Préstamos: 1/);
    assert.equal(boton.attributes['aria-expanded'], 'true');
    await boton.listeners.click();
    assert.equal(app.nodos['#reporteResumen'].hidden, true);
    assert.equal(boton.textContent, 'Ver resumen');
    assert.equal(boton.attributes['aria-expanded'], 'false');
    assert.equal(app.llamadas.filter(llamada => llamada.url === '/api/reportes/resumen').length, 1);
});

test('una respuesta tardía no vuelve a mostrar el resumen cerrado', async () => {
    let resolver;
    const app = cargarApp({ fetch: url => url === '/api/reportes/resumen'
        ? new Promise(resolve => { resolver = resolve; }) : Promise.resolve(respuesta([])) });
    const boton = app.nodos['#btnResumen'];
    const consulta = boton.listeners.click();
    await boton.listeners.click();
    resolver(respuesta(resumen));
    await consulta;
    assert.equal(app.nodos['#reporteResumen'].hidden, true);
    assert.equal(app.nodos['#reporteResumen'].textContent, '');
});
