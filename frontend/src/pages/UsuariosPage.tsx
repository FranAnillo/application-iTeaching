import { useEffect, useState } from 'react';
import { usuariosApi } from '../api/endpoints';
import type { Usuario } from '../types';

export default function UsuariosPage() {
  var [items, setItems] = useState<Usuario[]>([]);
  var [search, setSearch] = useState('');
  var [loading, setLoading] = useState(true);
  var [showCsvModal, setShowCsvModal] = useState(false);
  var [csvFile, setCsvFile] = useState<File | null>(null);
  var [importing, setImporting] = useState(false);
  var [importResult, setImportResult] = useState<{ importados: Usuario[]; errores: string[] } | null>(null);
  var [userRole, setUserRole] = useState('');

  var load = function () {
    setLoading(true);
    usuariosApi.getAll().then(function (res) {
      setItems(res.data);
    }).catch(function () {}).finally(function () {
      setLoading(false);
    });
  };

  useEffect(function () {
    load();
    try {
      var stored = localStorage.getItem('user');
      if (stored) {
        var parsed = JSON.parse(stored);
        setUserRole(parsed.role || '');
      }
    } catch (e) {}
  }, []);

  var handleSearch = function () {
    setLoading(true);
    var promise = search.trim()
      ? usuariosApi.search(search)
      : usuariosApi.getAll();
    promise.then(function (res) {
      setItems(res.data);
    }).catch(function () {}).finally(function () {
      setLoading(false);
    });
  };

  var handleImportCsv = function () {
    if (!csvFile) return;
    setImporting(true);
    setImportResult(null);
    usuariosApi.importCsv(csvFile).then(function (res) {
      setImportResult(res.data);
      load();
    }).catch(function (err) {
      var msg = err.response?.data?.message || err.message || 'Error al importar';
      setImportResult({ importados: [], errores: [msg] });
    }).finally(function () {
      setImporting(false);
    });
  };

  var isAdmin = userRole === 'ROLE_ADMIN';

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Usuarios</h2>

      <div className="mb-4 flex gap-2">
        <input
          type="text"
          value={search}
          onChange={function (e) { setSearch(e.target.value); }}
          onKeyDown={function (e) { if (e.key === 'Enter') handleSearch(); }}
          placeholder="Buscar usuarios..."
          className="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white"
        />
        <button
          onClick={handleSearch}
          className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium hover:bg-gray-200 transition dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600"
        >
          Buscar
        </button>
        {isAdmin && (
          <button
            onClick={function () { setShowCsvModal(true); setImportResult(null); setCsvFile(null); }}
            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" /></svg>
            Importar CSV
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : items.length === 0 ? (
        <p className="py-12 text-center text-gray-500 dark:text-gray-400">No se encontraron usuarios</p>
      ) : (
        <div className="overflow-hidden rounded-xl bg-white shadow-sm dark:bg-gray-800">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Nombre</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Usuario</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Grado</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Rol</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">Puntuacion</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {items.map(function (u) {
                var roleColor = u.role === 'ROLE_ADMIN'
                  ? 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300'
                  : u.role === 'ROLE_PROFESOR'
                    ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300'
                    : 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300';
                return (
                  <tr key={u.id} className="hover:bg-gray-50 transition dark:hover:bg-gray-700">
                    <td className="whitespace-nowrap px-6 py-4 font-medium text-gray-900 dark:text-white">
                      {u.nombre} {u.apellido}
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{u.username}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{u.email}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{u.gradoNombre || '-'}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm">
                      <span className={'rounded-full px-2.5 py-0.5 text-xs font-medium ' + roleColor}>
                        {u.role?.replace('ROLE_', '')}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">
                      <span className="inline-flex items-center gap-1">
                        {String.fromCharCode(11088)} {u.puntuacion?.toFixed(1) ?? '0.0'}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* ===== CSV IMPORT MODAL ===== */}
      {showCsvModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" onClick={function () { setShowCsvModal(false); }}>
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl dark:bg-gray-800" onClick={function (e) { e.stopPropagation(); }}>
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Importar usuarios desde CSV</h3>
              <button onClick={function () { setShowCsvModal(false); }} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
              </button>
            </div>

            <div className="mb-4 rounded-lg bg-indigo-50 p-4 text-sm text-indigo-800 dark:bg-indigo-900/30 dark:text-indigo-300">
              <p className="mb-2 font-semibold">Formato del CSV (separado por ;)</p>
              <code className="block rounded bg-white p-2 text-xs dark:bg-gray-900 dark:text-gray-300">
                username;password;nombre;apellido;email;telefono;rol;grado
              </code>
              <p className="mt-2 text-xs">
                <strong>Campos obligatorios:</strong> username, password, nombre, apellido, email<br />
                <strong>Campos opcionales:</strong> telefono, rol<br />
                <strong>Roles validos:</strong> ESTUDIANTE (por defecto), PROFESOR, ADMIN
              </p>
            </div>

            <div className="mb-4">
              <label className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">Seleccionar archivo CSV</label>
              <input
                type="file"
                accept=".csv,text/csv"
                onChange={function (e) {
                  var files = e.target.files;
                  setCsvFile(files && files.length > 0 ? files[0] : null);
                }}
                className="block w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-indigo-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-indigo-700 dark:text-gray-300"
              />
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleImportCsv}
                disabled={!csvFile || importing}
                className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {importing ? 'Importando...' : 'Importar'}
              </button>
              <button
                onClick={function () { setShowCsvModal(false); }}
                className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 transition dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
              >
                Cerrar
              </button>
            </div>

            {importResult && (
              <div className="mt-4 space-y-3">
                {importResult.importados.length > 0 && (
                  <div className="rounded-lg bg-green-50 p-3 dark:bg-green-900/30">
                    <p className="text-sm font-semibold text-green-800 dark:text-green-300">
                      {String.fromCharCode(9989)} {importResult.importados.length} usuario(s) importado(s) correctamente
                    </p>
                    <ul className="mt-1 list-disc pl-5 text-xs text-green-700 dark:text-green-400">
                      {importResult.importados.map(function (u) {
                        return <li key={u.id}>{u.username} - {u.nombre} {u.apellido} ({u.role?.replace('ROLE_', '')})</li>;
                      })}
                    </ul>
                  </div>
                )}
                {importResult.errores.length > 0 && (
                  <div className="rounded-lg bg-red-50 p-3 dark:bg-red-900/30">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-semibold text-red-800 dark:text-red-300">
                        {String.fromCharCode(10060)} {importResult.errores.length} error(es)
                      </p>
                      <button
                        onClick={() => {
                          const blob = new Blob([importResult.errores.join('\n')], { type: 'text/plain' });
                          const url = URL.createObjectURL(blob);
                          const a = document.createElement('a');
                          a.href = url;
                          a.download = 'errores_importacion.txt';
                          a.click();
                          URL.revokeObjectURL(url);
                        }}
                        className="text-xs text-red-600 hover:underline dark:text-red-400"
                      >
                        Exportar a TXT
                      </button>
                    </div>
                    <div className="mt-2 max-h-40 overflow-y-auto text-xs text-red-700 dark:text-red-400">
                      <table className="w-full">
                        <tbody>
                          {importResult.errores.map((err, i) => (
                            <tr key={i} className="border-b border-red-200 dark:border-red-800">
                              <td className="py-1">{err}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
