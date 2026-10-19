const test = require('node:test');
const assert = require('node:assert/strict');
const { cargarApp } = require('./cargar-app');

const respuesta = datos => Promise.resolve({ ok: true, status: 200, json: async () => datos });

const ficha = {
    usuario: { id: 1, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@example.com', telefono: '999111222' },
    resumen: { totalPrestamos: 2, prestamosActivos: 1, prestamosDevueltos: 1, prestamosVencidos: 0, multasAcumuladas: 8 },
    historial: [
        { id: 10, libroId: 2, libroTitulo: 'Libro activo', fechaPrestamo: '2026-10-01', fechaVencimiento: '2026-11-01', fechaDevolucion: null, tipoPrestamo: 'DOMICILIO', devuelto: false, vencido: false, multa: 0 },
        { id: 9, libroId: 3, libroTitulo: 'Libro devuelto', fechaPrestamo: '2026-09-01', fechaVencimiento: '2026-09-10', fechaDevolucion: '2026-09-14', tipoPrestamo: 'ESPECIAL', devuelto: true, vencido: false, multa: 8 }
    ]
};

function servidor(url) {
    if (url === '/api/usuarios/1/ficha') return respuesta(ficha);
    return respuesta([]);
}

test('cada usuario muestra un botón Ver ficha', () => {
    const app = cargarApp();
    app.ejecutar("estado.usuarios = [{ id: 1, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@example.com', telefono: '999' }]; mostrarUsuarios();");

    assert.match(app.nodos['#tablaUsuarios'].innerHTML, /Ver ficha/);
    assert.match(app.nodos['#tablaUsuarios'].innerHTML, /abrirFichaUsuario\(1\)/);
});

test('abre la ficha consultando el endpoint del usuario', async () => {
    const app = cargarApp({ fetch: servidor });
    app.ejecutar("document.querySelector('#fichaLector').hidden = true");
    app.llamadas.length = 0;

    await app.ejecutar('abrirFichaUsuario(1)');

    assert.equal(app.llamadas[0].url, '/api/usuarios/1/ficha');
    assert.equal(app.nodos['#fichaLector'].hidden, false);
    assert.match(app.nodos['#fichaContenido'].innerHTML, /Ana Torres/);
});

test('cierra la ficha sin alterar los datos cargados', () => {
    const app = cargarApp();
    app.ejecutar("document.querySelector('#fichaLector').hidden = false; cerrarFichaUsuario()");

    assert.equal(app.nodos['#fichaLector'].hidden, true);
});

test('muestra resumen e historial de préstamos', () => {
    const app = cargarApp();
    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(ficha)})`);
    const html = app.nodos['#fichaContenido'].innerHTML;

    assert.match(html, /Activos[^0-9]*1/);
    assert.match(html, /Devueltos[^0-9]*1/);
    assert.match(html, /S\/ 8\.00/);
    assert.match(html, /Libro activo/);
    assert.match(html, /Libro devuelto/);
});

test('muestra un estado vacío para un lector sin préstamos', () => {
    const app = cargarApp();
    const vacia = { ...ficha, resumen: { totalPrestamos: 0, prestamosActivos: 0, prestamosDevueltos: 0, prestamosVencidos: 0, multasAcumuladas: 0 }, historial: [] };

    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(vacia)})`);

    assert.match(app.nodos['#fichaContenido'].innerHTML, /no tiene préstamos/i);
});

test('escapa los datos del lector y del historial para evitar XSS', () => {
    const app = cargarApp();
    const peligrosa = {
        ...ficha,
        usuario: { ...ficha.usuario, nombre: '<img src=x onerror=alert(1)>', correo: '<script>alert(2)</script>' },
        historial: [{ ...ficha.historial[0], libroTitulo: '<svg onload=alert(3)>' }]
    };

    app.ejecutar(`mostrarFichaUsuario(${JSON.stringify(peligrosa)})`);
    const html = app.nodos['#fichaContenido'].innerHTML;

    assert.doesNotMatch(html, /<img|<script|<svg/i);
    assert.match(html, /&lt;img/);
    assert.match(html, /&lt;script/);
    assert.match(html, /&lt;svg/);
});
