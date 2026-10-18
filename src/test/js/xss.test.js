// Pruebas de regresión del frontend (H2). Ejecutar con: node --test src/test/js
const test = require('node:test');
const assert = require('node:assert');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const rutaApp = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'app.js');

function cargarApp() {
    const nodos = {};
    const nodo = () => ({
        value: '', textContent: '', innerHTML: '', className: '', min: '',
        addEventListener() {}, reset() {}
    });
    const documento = { querySelector: selector => (nodos[selector] ??= nodo()) };
    const contexto = vm.createContext({
        document: documento,
        fetch: () => Promise.reject(new Error('sin red en las pruebas')),
        alert() {},
        console
    });
    vm.runInContext(fs.readFileSync(rutaApp, 'utf8'), contexto);
    return { nodos, ejecutar: codigo => vm.runInContext(codigo, contexto) };
}

const PAYLOAD_IMG = '<img src=x onerror=alert(1)>';
const PAYLOAD_SCRIPT = '<script>alert(2)</script>';

test('los libros con HTML en sus datos se muestran como texto', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: '${PAYLOAD_IMG}', autor: '${PAYLOAD_SCRIPT}', isbn: '"><svg onload=alert(3)>', disponible: true }]; mostrarLibros();`);
    for (const html of [nodos['#tablaLibros'].innerHTML, nodos['#libroId'].innerHTML]) {
        assert.ok(!/<img|<script|<svg/i.test(html), `HTML sin escapar: ${html}`);
    }
    assert.ok(nodos['#tablaLibros'].innerHTML.includes('&lt;img src=x onerror=alert(1)&gt;'));
});

test('los usuarios con HTML en sus datos se muestran como texto', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.usuarios = [{ id: 1, nombre: '${PAYLOAD_IMG}', apellidos: '${PAYLOAD_SCRIPT}', dni: '1', correo: 'a"><svg onload=alert(3)>', telefono: '2' }]; mostrarUsuarios();`);
    for (const html of [nodos['#tablaUsuarios'].innerHTML, nodos['#usuarioId'].innerHTML]) {
        assert.ok(!/<img|<script|<svg/i.test(html), `HTML sin escapar: ${html}`);
    }
});

test('el historial de préstamos escapa los nombres de libro y usuario', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: '${PAYLOAD_IMG}', autor: 'A', isbn: 'I', disponible: false }];
              estado.usuarios = [{ id: 1, nombre: '${PAYLOAD_SCRIPT}', apellidos: 'B', dni: '1', correo: 'c', telefono: '2' }];
              estado.prestamos = [{ id: 7, libroId: 1, usuarioId: 1, fechaVencimiento: '2099-01-01', devuelto: false, multa: 0 }];
              mostrarPrestamos();`);
    assert.ok(!/<img|<script/i.test(nodos['#tablaPrestamos'].innerHTML), nodos['#tablaPrestamos'].innerHTML);
});

test('los datos normales conservan el mismo formato visible', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: 'Clean Code', autor: 'Robert C. Martin', isbn: '9780132350884', disponible: true }];
              estado.usuarios = [{ id: 1, nombre: 'Ana', apellidos: 'Torres', dni: '70000001', correo: 'ana@x.com', telefono: '999' }];
              estado.prestamos = [{ id: 7, libroId: 1, usuarioId: 1, fechaVencimiento: '2099-01-01', devuelto: false, multa: 2.5 }];
              mostrarLibros(); mostrarUsuarios(); mostrarPrestamos();`);
    assert.ok(nodos['#tablaLibros'].innerHTML.includes('<td>Clean Code</td><td>Robert C. Martin</td><td>9780132350884</td>'));
    assert.ok(nodos['#tablaLibros'].innerHTML.includes('Disponible'));
    assert.ok(nodos['#libroId'].innerHTML.includes('<option value="1">Clean Code</option>'));
    assert.ok(nodos['#tablaUsuarios'].innerHTML.includes('<td>Ana</td><td>Torres</td><td>70000001</td><td>ana@x.com</td><td>999</td>'));
    assert.ok(nodos['#tablaPrestamos'].innerHTML.includes('Ana Torres'));
    assert.ok(nodos['#tablaPrestamos'].innerHTML.includes('S/ 2.50'));
    assert.ok(nodos['#tablaPrestamos'].innerHTML.includes('onclick="devolverPrestamo(7)"'));
});

test('un libro con campos nulos no rompe la búsqueda', () => {
    const { nodos, ejecutar } = cargarApp();
    ejecutar(`estado.libros = [{ id: 1, titulo: null, autor: null, isbn: '1', disponible: true }]; mostrarLibros();`);
    assert.ok(nodos['#tablaLibros'].innerHTML.includes('<tr>'));
});
