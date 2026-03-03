import { useEffect, useState } from 'react';
import { estudiantesApi } from '../api/endpoints';
import type { Estudiante } from '../types';

export default function EstudiantesPage() {
  const [items, setItems] = useState<Estudiante[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const res = search.trim()
        ? await estudiantesApi.search(search)
        : await estudiantesApi.getAll();
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
    if (!confirm('¿Eliminar este estudiante?')) return;
    try {
      await estudiantesApi.delete(id);
      setItems((prev) => prev.filter((e) => e.id !== id));
    } catch {
      alert('Error al eliminar');
    }
  };

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Estudiantes</h2>

      <div className="mb-4 flex gap-2">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="Buscar estudiantes..."
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
        <p className="py-12 text-center text-gray-500">No se encontraron estudiantes</p>
      ) : (
        <div className="overflow-hidden rounded-xl bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Teléfono</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((e) => (
                <tr key={e.id} className="hover:bg-gray-50 transition">
                  <td className="whitespace-nowrap px-6 py-4 font-medium text-gray-900">
                    {e.nombre} {e.apellido}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{e.email}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{e.telefono}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <button
                      onClick={() => handleDelete(e.id)}
                      className="rounded bg-red-50 px-3 py-1 text-xs font-medium text-red-600 hover:bg-red-100 transition"
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
