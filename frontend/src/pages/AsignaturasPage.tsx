import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { asignaturasApi } from '../api/endpoints';
import type { Asignatura } from '../types';

export default function AsignaturasPage() {
  var authCtx = useAuth();
  var user = authCtx.user;
  var isAdmin = user && user.role === 'ROLE_ADMIN';
  const [items, setItems] = useState<Asignatura[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  // CSV import state
  const [showCsvModal, setShowCsvModal] = useState(false);
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<{ imported: Asignatura[]; error: string | null } | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const res = search.trim()
        ? await asignaturasApi.search(search)
        : await asignaturasApi.getAll();
      setItems(res.data);
    } catch {
      /* ignore */
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleSearch = () => load();

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar esta asignatura?')) return;
    try {
      await asignaturasApi.delete(id);
      setItems((prev) => prev.filter((a) => a.id !== id));
    } catch {
      alert('Error al eliminar');
    }
  };

  const handleImportCsv = () => {
    if (!csvFile) return;
    setImporting(true);
    setImportResult(null);
    asignaturasApi.importCsv(csvFile)
      .then((res) => {
        setImportResult({ imported: res.data, error: null });
        load();
      })
      .catch((err) => {
        const msg = err.response?.data?.message || err.message || 'Error al importar';
        setImportResult({ imported: [], error: msg });
      })
      .finally(() => {
        setImporting(false);
      });
  };

  return (
    <div>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Asignaturas</h2>
        {isAdmin && (
          <div className="flex gap-2">
            <button
              onClick={() => { setShowCsvModal(true); setImportResult(null); setCsvFile(null); }}
              className="inline-flex items-center gap-2 rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 transition"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" /></svg>
              Importar CSV
            </button>
            <Link
              to="/asignaturas/new"
              className="inline-flex items-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
            >
              + Nueva asignatura
            </Link>
          </div>
        )}
      </div>

      {/* Search bar */}
      <div className="mb-4 flex gap-2">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="Buscar asignaturas..."
          className="flex-1 rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
        />
        <button
          onClick={handleSearch}
          className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium hover:bg-gray-200 transition"
        >
          Buscar
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : items.length === 0 ? (
        <p className="py-12 text-center text-gray-500">No se encontraron asignaturas</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((a) => (
            <div key={a.id} className="rounded-xl bg-white p-5 shadow-sm hover:shadow-md transition">
              <div className="mb-3">
                <h3 className="font-semibold text-gray-900">{a.nombre}</h3>
              </div>
              <p className="mb-3 line-clamp-2 text-sm text-gray-500">{a.descripcion || 'Sin descripción'}</p>
              {a.creadorNombre && (
                <p className="mb-3 text-xs text-gray-400">Creador: {a.creadorNombre}</p>
              )}
              <div className="flex gap-2">
                <Link
                  to={`/asignaturas/${a.id}`}
                  className="rounded-lg bg-gray-100 px-3 py-1.5 text-xs font-medium hover:bg-gray-200 transition"
                >
                  Ver detalle
                </Link>
                {isAdmin && <button
                  onClick={() => handleDelete(a.id)}
                  className="rounded-lg bg-red-50 px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-100 transition"
                >
                  Eliminar
                </button>}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ===== CSV IMPORT MODAL ===== */}
      {showCsvModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" onClick={() => setShowCsvModal(false)}>
          <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl dark:bg-gray-800" onClick={(e) => e.stopPropagation()}>
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Importar asignaturas desde CSV</h3>
              <button onClick={() => setShowCsvModal(false)} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
              </button>
            </div>

            <div className="mb-4 rounded-lg bg-green-50 p-4 text-sm text-green-800 dark:bg-green-900/30 dark:text-green-300">
              <p className="mb-2 font-semibold">Formato del CSV (separado por ;)</p>
              <code className="block rounded bg-white p-2 text-xs dark:bg-gray-900 dark:text-gray-300">
                nombre;descripcion;url
              </code>
              <p className="mt-2 text-xs">
                <strong>Campo obligatorio:</strong> nombre<br />
                <strong>Campos opcionales:</strong> descripcion, url
              </p>
            </div>

            <div className="mb-4">
              <label className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">Seleccionar archivo CSV</label>
              <input
                type="file"
                accept=".csv,text/csv"
                onChange={(e) => {
                  const files = e.target.files;
                  setCsvFile(files && files.length > 0 ? files[0] : null);
                }}
                className="block w-full text-sm text-gray-500 file:mr-4 file:rounded-lg file:border-0 file:bg-green-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-green-700 dark:text-gray-300"
              />
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleImportCsv}
                disabled={!csvFile || importing}
                className="flex-1 rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {importing ? 'Importando...' : 'Importar'}
              </button>
              <button
                onClick={() => setShowCsvModal(false)}
                className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 transition dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
              >
                Cerrar
              </button>
            </div>

            {importResult && (
              <div className="mt-4 space-y-3">
                {importResult.imported.length > 0 && (
                  <div className="rounded-lg bg-green-50 p-3 dark:bg-green-900/30">
                    <p className="text-sm font-semibold text-green-800 dark:text-green-300">
                      ✅ {importResult.imported.length} asignatura(s) importada(s) correctamente
                    </p>
                    <ul className="mt-1 list-disc pl-5 text-xs text-green-700 dark:text-green-400">
                      {importResult.imported.map((a) => (
                        <li key={a.id}>{a.nombre}{a.descripcion ? ` - ${a.descripcion}` : ''}</li>
                      ))}
                    </ul>
                  </div>
                )}
                {importResult.error && (
                  <div className="rounded-lg bg-red-50 p-3 dark:bg-red-900/30">
                    <p className="text-sm font-semibold text-red-800 dark:text-red-300">
                      ❌ Error
                    </p>
                    <p className="mt-1 text-xs text-red-700 dark:text-red-400">{importResult.error}</p>
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
