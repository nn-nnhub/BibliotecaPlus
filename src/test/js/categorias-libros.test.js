const test = require('node:test');
const assert = require('node:assert');
const { cargarApp } = require('./cargar-app');

test('filtra el catálogo por categoría', () => {
    const app = cargarApp();
    app.ejecutar("document.querySelector('#buscarLibro').value = ''; document.querySelector('#filtroCategoriaLibro').value = 'Tecnología';");

    app.ejecutar(`estado.libros = [
        { id: 1, titulo: 'Clean Code', autor: 'Robert C. Martin', isbn: '1', categoria: 'Tecnología', disponible: true },
        { id: 2, titulo: 'Fundación', autor: 'Isaac Asimov', isbn: '2', categoria: 'Ciencia ficción', disponible: true }
    ]; mostrarLibros();`);

    assert.match(app.nodos['#tablaLibros'].innerHTML, /Clean Code/);
    assert.doesNotMatch(app.nodos['#tablaLibros'].innerHTML, /Fundación/);
});

test('muestra Sin categoría para libros antiguos', () => {
    const app = cargarApp();
    app.ejecutar("document.querySelector('#buscarLibro').value = ''; document.querySelector('#filtroCategoriaLibro').value = '';");

    app.ejecutar(`estado.libros = [
        { id: 1, titulo: 'Libro heredado', autor: 'Autor', isbn: '1', disponible: true }
    ]; mostrarLibros();`);

    assert.match(app.nodos['#tablaLibros'].innerHTML, /Sin categoría/);
});
