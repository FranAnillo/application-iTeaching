import { useEffect, useState } from 'react';
import { usuariosApi } from '../api/endpoints';
import type { Usuario } from '../types';

export default function UsuariosPage() {
  const [items, setItems] = useState<Usuario[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const res = search.trim()
        ? await usuariosApi.search(search)
        : await usuariosApi.getAll();
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

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Usuarios</h2>

      <div className="mb-4 flex gap-2">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="Buscar usuarios..."
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
        <p className="py-12 text-center text-gray-500">No se encontraron usuarios</p>
      ) : (
        <div className="overflow-hidden rounded-xl bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Usuario</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Rol</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Puntuación</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((u) => (
                <tr key={u.id} className="hover:bg-gray-50 transition">
                  <td className="whitespace-nowrap px-6 py-4 font-medium text-gray-900">
                    {u.nombre} {u.apellido}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{u.username}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{u.email}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm">
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      u.role === 'ROLE_ADMIN'
                        ? 'bg-red-100 text-red-700'
                        : 'bg-green-100 text-green-700'
                    }`}>
                      {u.role?.replace('ROLE_', '')}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm">
                    <span className="inline-flex items-center gap-1">
                      ⭐ {u.puntuacion?.toFixed(1) ?? '0.0'}
                    </span>
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
