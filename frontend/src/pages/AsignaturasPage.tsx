import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { asignaturasApi } from '../api/endpoints';
import type { Asignatura } from '../types';

export default function AsignaturasPage() {
  const [items, setItems] = useState<Asignatura[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

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

  return (
    <div>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Asignaturas</h2>
        <Link
          to="/asignaturas/new"
          className="inline-flex items-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
        >
          + Nueva asignatura
        </Link>
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
              <div className="mb-3 flex items-start justify-between">
                <h3 className="font-semibold text-gray-900">{a.nombre}</h3>
                <span className="rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
                  {a.precio?.toFixed(2)} €
                </span>
              </div>
              <p className="mb-3 line-clamp-2 text-sm text-gray-500">{a.descripcion || 'Sin descripción'}</p>
              {a.profesorNombre && (
                <p className="mb-3 text-xs text-gray-400">Profesor: {a.profesorNombre}</p>
              )}
              <div className="flex gap-2">
                <Link
                  to={`/asignaturas/${a.id}`}
                  className="rounded-lg bg-gray-100 px-3 py-1.5 text-xs font-medium hover:bg-gray-200 transition"
                >
                  Ver detalle
                </Link>
                <button
                  onClick={() => handleDelete(a.id)}
                  className="rounded-lg bg-red-50 px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-100 transition"
                >
                  Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
