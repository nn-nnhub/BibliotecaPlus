// Ejecuta app.js en un contexto aislado de Node con un `document` simulado.
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const rutaApp = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'app.js');

function cargarApp({ fetch } = {}) {
    const nodos = {};
    const llamadas = [];
    const nodo = () => ({
        value: '', textContent: '', innerHTML: '', className: '', min: '',
        listeners: {},
        addEventListener(tipo, accion) { this.listeners[tipo] = accion; }, reset() {}
    });
    const documento = { querySelector: selector => (nodos[selector] ??= nodo()) };
    const fetchSimulado = fetch ?? (() => Promise.reject(new Error('sin red en las pruebas')));
    const contexto = vm.createContext({
        document: documento,
        fetch: (url, opciones) => { llamadas.push({ url, metodo: opciones?.method ?? 'GET' }); return fetchSimulado(url, opciones); },
        alert() {},
        console
    });
    vm.runInContext(fs.readFileSync(rutaApp, 'utf8'), contexto);
    return { nodos, llamadas, ejecutar: codigo => vm.runInContext(codigo, contexto) };
}

module.exports = { cargarApp };
