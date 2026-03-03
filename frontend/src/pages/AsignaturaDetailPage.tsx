import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { asignaturasApi, profesoresApi } from '../../api/endpoints';
import type { Asignatura, Profesor } from '../../types';

export default function AsignaturaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [form, setForm] = useState<Partial<Asignatura>>({
    nombre: '',
    descripcion: '',
    tituloAnuncio: '',
    url: '',
    precio: 0,
    profesorId: undefined,
  });
  const [profesores, setProfesores] = useState<Profesor[]>([]);
  const [loading, setLoading] = useState(!isNew);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    profesoresApi.getAll().then((r) => setProfesores(r.data)).catch(() => {});

    if (!isNew && id) {
      asignaturasApi
        .getById(Number(id))
        .then((r) => setForm(r.data))
        .catch(() => setError('Asignatura no encontrada'))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  const update = (field: string, value: any) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (isNew) {
        await asignaturasApi.create(form);
      } else {
        await asignaturasApi.update(Number(id), form);
      }
      navigate('/asignaturas');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al guardar');
    } finally {
      setSaving(false);
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
    <div className="mx-auto max-w-2xl">
      <h2 className="mb-6 text-2xl font-bold text-gray-900">
        {isNew ? 'Nueva asignatura' : 'Editar asignatura'}
      </h2>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
          <input
            type="text"
            value={form.nombre ?? ''}
            onChange={(e) => update('nombre', e.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Descripción</label>
          <textarea
            value={form.descripcion ?? ''}
            onChange={(e) => update('descripcion', e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Título del anuncio</label>
          <input
            type="text"
            value={form.tituloAnuncio ?? ''}
            onChange={(e) => update('tituloAnuncio', e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Precio (€)</label>
            <input
              type="number"
              step="0.01"
              value={form.precio ?? 0}
              onChange={(e) => update('precio', parseFloat(e.target.value))}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Profesor</label>
            <select
              value={form.profesorId ?? ''}
              onChange={(e) => update('profesorId', e.target.value ? Number(e.target.value) : undefined)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">Sin profesor</option>
              {profesores.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nombre} {p.apellido}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">URL</label>
          <input
            type="url"
            value={form.url ?? ''}
            onChange={(e) => update('url', e.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </div>

        <div className="flex gap-3 pt-4">
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition"
          >
            {saving ? 'Guardando...' : 'Guardar'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/asignaturas')}
            className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium hover:bg-gray-200 transition"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}
