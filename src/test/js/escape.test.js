// Pruebas unitarias de escaparHtml. Ejecutar con: node --test "src/test/js/*.test.js"
const test = require('node:test');
const assert = require('node:assert');
const { cargarApp } = require('./cargar-app');

test('escaparHtml convierte los cinco caracteres especiales de HTML', () => {
    const { ejecutar } = cargarApp();
    const entrada = `<a href="x" title='y'>&</a>`;
    assert.strictEqual(ejecutar(`escaparHtml(${JSON.stringify(entrada)})`),
        '&lt;a href=&quot;x&quot; title=&#39;y&#39;&gt;&amp;&lt;/a&gt;');
});

test('escaparHtml no vuelve a escapar el resultado como texto plano', () => {
    const { ejecutar } = cargarApp();
    assert.strictEqual(ejecutar(`escaparHtml('Clean Code')`), 'Clean Code');
    assert.strictEqual(ejecutar(`escaparHtml('Ingeniería de Software')`), 'Ingeniería de Software');
});

test('escaparHtml convierte nulos y números en texto', () => {
    const { ejecutar } = cargarApp();
    assert.strictEqual(ejecutar('escaparHtml(null)'), '');
    assert.strictEqual(ejecutar('escaparHtml(undefined)'), '');
    assert.strictEqual(ejecutar('escaparHtml(42)'), '42');
    assert.strictEqual(ejecutar('escaparHtml(0)'), '0');
});
