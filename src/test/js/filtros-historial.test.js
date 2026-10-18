const test = require('node:test');
const assert = require('node:assert/strict');
const { cargarApp } = require('./cargar-app');

const prestamos = [
    { id: 1, libroId: 1, usuarioId: 1, devuelto: false, fechaVencimiento: '2099-01-01', multa: 0 },
    { id: 2, libroId: 2, usuarioId: 2, devuelto: true, fechaVencimiento: '2099-01-01', multa: 0 }
];
const respuesta = datos => Promise.resolve({ ok: true, status: 200, json: async () => datos });

function servidor(url, opciones) {
    if (opciones?.method === 'POST' || opciones?.method === 'PUT') return respuesta({ id: 3 });
    const consulta = new URL(url, 'http://prueba.local');
    if (consulta.pathname === '/api/libros') return respuesta([
        { id: 1, titulo: '<img src=x onerror=alert(1)>', disponible: true },
        { id: 2, titulo: 'Libro B', disponible: true }
    ]);
    if (consulta.pathname === '/api/usuarios') return respuesta([
        { id: 1, nombre: '<script>alert(1)</script>' }, { id: 2, nombre: 'Luis' }
    ]);
    return respuesta(prestamos.filter(p => [...consulta.searchParams].every(([campo, valor]) => String(p[campo]) === valor)));
}

async function iniciar(fetch = servidor) {
    const app = cargarApp({ fetch });
    app.ejecutar("['#filtroUsuarioId', '#filtroLibroId', '#filtroDevuelto', '#tipoPrestamo'].forEach(selector => document.querySelector(selector))");
    await app.ejecutar('cargarDatos()');
    app.llamadas.length = 0;
    return app;
}

async function aplicar(app, valores) {
    for (const [selector, valor] of Object.entries(valores)) app.nodos[selector].value = valor;
    await app.nodos['#formFiltrosHistorial'].listeners.submit({ preventDefault() {} });
}

test('aplica cada filtro y conserva false como parámetro', async () => {
    for (const [selector, parametro, valor] of [
        ['#filtroUsuarioId', 'usuarioId', '1'], ['#filtroLibroId', 'libroId', '2'],
        ['#filtroDevuelto', 'devuelto', 'true'], ['#filtroDevuelto', 'devuelto', 'false']
    ]) {
        const app = await iniciar();
        await aplicar(app, { [selector]: valor });
        assert.equal(app.llamadas.at(-1).url, `/api/prestamos?${parametro}=${valor}`);
        assert.equal(app.nodos['#totalPrestamos'].textContent, 2);
    }
});

test('combina los tres filtros y mantiene el total global', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '1', '#filtroLibroId': '1', '#filtroDevuelto': 'false' });
    assert.equal(app.llamadas.at(-1).url, '/api/prestamos?usuarioId=1&libroId=1&devuelto=false');
    assert.equal((app.nodos['#tablaPrestamos'].innerHTML.match(/<tr>/g) || []).length, 1);
    assert.equal(app.nodos['#totalPrestamos'].textContent, 2);
});

test('limpiar vacía los controles y recupera todo el historial', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '1', '#filtroDevuelto': 'false' });
    await app.nodos['#btnLimpiarFiltros'].listeners.click();
    for (const selector of ['#filtroUsuarioId', '#filtroLibroId', '#filtroDevuelto']) assert.equal(app.nodos[selector].value, '');
    assert.equal((app.nodos['#tablaPrestamos'].innerHTML.match(/<tr>/g) || []).length, 2);
    await app.ejecutar('cargarDatos()');
    assert.ok(!app.llamadas.at(-1).url.includes('?'));
});

test('devolver conserva los filtros aplicados al recargar', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '1', '#filtroDevuelto': 'false' });
    app.llamadas.length = 0;
    await app.ejecutar('devolverPrestamo(1)');
    assert.ok(app.llamadas.some(l => l.metodo === 'PUT' && l.url === '/api/prestamos/1/devolver'));
    assert.equal(app.llamadas.at(-1).url, '/api/prestamos?usuarioId=1&devuelto=false');
    assert.equal(app.nodos['#filtroDevuelto'].value, 'false');
});

test('registrar conserva los filtros del historial al recargar', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '1' });
    app.nodos['#libroId'].value = '2';
    app.nodos['#usuarioId'].value = '2';
    app.nodos['#tipoPrestamo'].value = 'DOMICILIO';
    await app.nodos['#formPrestamo'].listeners.submit({ preventDefault() {} });
    assert.ok(app.llamadas.some(l => l.metodo === 'POST' && l.url.startsWith('/api/prestamos?libroId=2&usuarioId=2')));
    assert.equal(app.llamadas.at(-1).url, '/api/prestamos?usuarioId=1');
    assert.equal(app.nodos['#filtroUsuarioId'].value, '1');
});

test('sin coincidencias muestra un mensaje y mantiene el total global', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '999' });
    assert.equal(app.nodos['#tablaPrestamos'].innerHTML, '');
    assert.match(app.nodos['#historialEstado'].textContent, /No hay préstamos que coincidan/);
    assert.equal(app.nodos['#totalPrestamos'].textContent, 2);
});

test('las filas filtradas conservan el escape HTML', async () => {
    const app = await iniciar();
    await aplicar(app, { '#filtroUsuarioId': '1' });
    const html = app.nodos['#tablaPrestamos'].innerHTML;
    assert.ok(!/<img|<script/i.test(html));
    assert.ok(html.includes('&lt;img'));
    assert.ok(html.includes('&lt;script'));
});

test('una respuesta tardía no restaura filtros después de limpiarlos', async () => {
    let completar;
    const app = await iniciar((url, opciones) => url === '/api/prestamos?usuarioId=1'
        ? new Promise(resolve => { completar = resolve; }) : servidor(url, opciones));
    app.nodos['#filtroUsuarioId'].value = '1';
    const pendiente = app.nodos['#formFiltrosHistorial'].listeners.submit({ preventDefault() {} });
    await app.nodos['#btnLimpiarFiltros'].listeners.click();
    completar(await respuesta([prestamos[0]]));
    await pendiente;
    assert.equal((app.nodos['#tablaPrestamos'].innerHTML.match(/<tr>/g) || []).length, 2);
    assert.equal(app.nodos['#filtroUsuarioId'].value, '');
});

test('los errores de filtrado se muestran como texto', async () => {
    const app = await iniciar((url, opciones) => url.includes('usuarioId=0')
        ? Promise.resolve({ ok: false, status: 400, json: async () => ({ message: '<script>error</script>' }) })
        : servidor(url, opciones));
    await aplicar(app, { '#filtroUsuarioId': '0' });
    assert.equal(app.nodos['#historialEstado'].textContent, '<script>error</script>');
    assert.equal(app.nodos['#historialEstado'].innerHTML, '');
    assert.equal(app.nodos['#historialEstado'].className, 'message error');
});
