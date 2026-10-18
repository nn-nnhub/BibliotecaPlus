// Ejecuta app.js en un contexto aislado de Node con un `document` simulado.
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const rutaApp = path.join(__dirname, '..', '..', 'main', 'resources', 'static', 'app.js');

function cargarApp({ fetch, confirm } = {}) {
    const nodos = {};
    const llamadas = [];
    const confirmaciones = [];
    const nodo = () => ({
        value: '', textContent: '', innerHTML: '', className: '', min: '',
        listeners: {},
        addEventListener(tipo, manejador) { this.listeners[tipo] = manejador; },
        reset() { this.value = ''; }
    });
    const documento = { querySelector: selector => (nodos[selector] ??= nodo()) };
    const fetchSimulado = fetch ?? (() => Promise.reject(new Error('sin red en las pruebas')));
    const confirmSimulado = confirm ?? (() => false);
    const contexto = vm.createContext({
        document: documento,
        fetch: (url, opciones) => { llamadas.push({ url, metodo: opciones?.method ?? 'GET' }); return fetchSimulado(url, opciones); },
        confirm: mensaje => { confirmaciones.push(mensaje); return confirmSimulado(mensaje); },
        alert() {},
        console
    });
    vm.runInContext(fs.readFileSync(rutaApp, 'utf8'), contexto);
    return {
        nodos,
        llamadas,
        confirmaciones,
        ejecutar: codigo => vm.runInContext(codigo, contexto),
        disparar(selector, tipo, evento) {
            const eventoSimulado = evento ?? { target: nodos[selector], preventDefault() {} };
            return nodos[selector].listeners[tipo](eventoSimulado);
        }
    };
}

module.exports = { cargarApp };
