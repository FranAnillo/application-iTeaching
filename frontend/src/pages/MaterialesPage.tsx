import { useEffect, useState, type FormEvent } from 'react';
import { materialesApi, asignaturasApi } from '../api/endpoints';
import type { Material, Asignatura } from '../types';

const TIPOS = ['DOCUMENTO', 'VIDEO', 'ENLACE', 'PRESENTACION', 'EJERCICIO', 'OTRO'];

export default function MaterialesPage() {
  const [materiales, setMateriales] = useState<Material[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [error, setError] = useState('');

  const emptyForm = { titulo: '', descripcion: '', urlRecurso: '', tipo: 'DOCUMENTO', asignaturaId: '' };
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    try {
      const [matRes, asigRes] = await Promise.all([
        materialesApi.getMisMateriales(),
        asignaturasApi.getAll(),
      ]);
      setMateriales(matRes.data);
      setAsignaturas(asigRes.data);
    } catch {
      // silently fail
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const payload: any = {
        titulo: form.titulo,
        descripcion: form.descripcion,
        urlRecurso: form.urlRecurso,
        tipo: form.tipo,
        asignaturaId: form.asignaturaId ? Number(form.asignaturaId) : null,
      };
      if (editId) {
        await materialesApi.update(editId, payload);
      } else {
        await materialesApi.create(payload);
      }
      setShowForm(false);
      setEditId(null);
      setForm(emptyForm);
      await load();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al guardar material');
    }
  };

  const handleEdit = (m: Material) => {
    setForm({
      titulo: m.titulo,
      descripcion: m.descripcion || '',
      urlRecurso: m.urlRecurso || '',
      tipo: m.tipo,
      asignaturaId: m.asignaturaId ? String(m.asignaturaId) : '',
    });
    setEditId(m.id);
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar este material?')) return;
    try {
      await materialesApi.delete(id);
      await load();
    } catch {
      // silently fail
    }
  };

  const tipoEmoji: Record<string, string> = {
    DOCUMENTO: '📄',
    VIDEO: '🎬',
    ENLACE: '🔗',
    PRESENTACION: '📊',
    EJERCICIO: '✏️',
    OTRO: '📦',
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Materiales</h2>
          <p className="text-gray-500">Gestiona tus materiales educativos</p>
        </div>
        <button
          onClick={() => { setShowForm(true); setEditId(null); setForm(emptyForm); }}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
        >
          + Nuevo Material
        </button>
      </div>

      {/* Form modal */}
      {showForm && (
        <div className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          <h3 className="mb-4 text-lg font-semibold text-gray-900">
            {editId ? 'Editar Material' : 'Nuevo Material'}
          </h3>

          {error && (
            <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Titulo</label>
              <input
                type="text"
                value={form.titulo}
                onChange={(e) => setForm({ ...form, titulo: e.target.value })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Descripcion</label>
              <textarea
                value={form.descripcion}
                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                rows={3}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">URL del recurso</label>
              <input
                type="text"
                value={form.urlRecurso}
                onChange={(e) => setForm({ ...form, urlRecurso: e.target.value })}
                placeholder="https://..."
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Tipo</label>
                <select
                  value={form.tipo}
                  onChange={(e) => setForm({ ...form, tipo: e.target.value })}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  {TIPOS.map((t) => (
                    <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Asignatura (opcional)</label>
                <select
                  value={form.asignaturaId}
                  onChange={(e) => setForm({ ...form, asignaturaId: e.target.value })}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="">Sin asignatura</option>
                  {asignaturas.map((a) => (
                    <option key={a.id} value={a.id}>{a.nombre}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                type="submit"
                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
              >
                {editId ? 'Guardar Cambios' : 'Crear Material'}
              </button>
              <button
                type="button"
                onClick={() => { setShowForm(false); setEditId(null); setForm(emptyForm); }}
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition"
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>
      )}

      {/* List */}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : materiales.length === 0 ? (
        <div className="rounded-xl bg-white p-12 text-center shadow-sm">
          <p className="text-5xl">📂</p>
          <p className="mt-4 text-lg font-medium text-gray-900">No hay materiales</p>
          <p className="text-gray-500">Sube tu primer material educativo</p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {materiales.map((m) => (
            <div key={m.id} className="rounded-xl bg-white p-5 shadow-sm hover:shadow-md transition">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2">
                  <span className="text-2xl">{tipoEmoji[m.tipo] || '📦'}</span>
                  <div>
                    <h3 className="font-semibold text-gray-900">{m.titulo}</h3>
                    <p className="text-xs text-gray-400">{m.tipo}</p>
                  </div>
                </div>
              </div>
              {m.descripcion && (
                <p className="mt-2 text-sm text-gray-600 line-clamp-2">{m.descripcion}</p>
              )}
              <div className="mt-3 flex flex-wrap gap-2 text-xs text-gray-500">
                {m.autorNombre && (
                  <span className="rounded-full bg-green-50 px-2 py-0.5 text-green-700">
                    👤 {m.autorNombre}
                  </span>
                )}
                {m.asignaturaNombre && (
                  <span className="rounded-full bg-blue-50 px-2 py-0.5 text-blue-700">
                    📚 {m.asignaturaNombre}
                  </span>
                )}
              </div>
              {m.urlRecurso && (
                <a
                  href={m.urlRecurso}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="mt-3 inline-block text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                  Abrir recurso →
                </a>
              )}
              <div className="mt-3 flex gap-2 border-t pt-3">
                <button
                  onClick={() => handleEdit(m)}
                  className="text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                  Editar
                </button>
                <button
                  onClick={() => handleDelete(m.id)}
                  className="text-sm font-medium text-red-600 hover:text-red-500"
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
