import { useEffect, useState } from 'react';
import { profesoresApi } from '../api/endpoints';
import type { Profesor } from '../types';

export default function ProfesoresPage() {
  const [items, setItems] = useState<Profesor[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    profesoresApi
      .getAll()
      .then((r) => setItems(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar este profesor?')) return;
    try {
      await profesoresApi.delete(id);
      setItems((prev) => prev.filter((p) => p.id !== id));
    } catch {
      alert('Error al eliminar');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div>
      <h2 className="mb-6 text-2xl font-bold text-gray-900">Profesores</h2>

      {items.length === 0 ? (
        <p className="py-12 text-center text-gray-500">No hay profesores registrados</p>
      ) : (
        <div className="overflow-hidden rounded-xl bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Teléfono</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Puntuación</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((p) => (
                <tr key={p.id} className="hover:bg-gray-50 transition">
                  <td className="whitespace-nowrap px-6 py-4 font-medium text-gray-900">
                    {p.nombre} {p.apellido}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{p.email}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{p.telefono}</td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm">
                    <span className="inline-flex items-center gap-1">
                      ⭐ {p.puntuacion?.toFixed(1) ?? '—'}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <button
                      onClick={() => handleDelete(p.id)}
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
