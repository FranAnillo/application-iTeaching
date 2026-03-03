import { useEffect, useState, type FormEvent } from 'react';
import { valoracionesApi, profesoresApi, asignaturasApi } from '../../api/endpoints';
import type { Valoracion, Profesor, Asignatura } from '../../types';

export default function ValoracionesPage() {
  const [items, setItems] = useState<Valoracion[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [profesores, setProfesores] = useState<Profesor[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [form, setForm] = useState({
    puntuacion: 5,
    comentario: '',
    profesorId: 0,
    asignaturaId: 0,
    alumnoId: 0,
  });
  const [saving, setSaving] = useState(false);

  const loadValoraciones = () => {
    valoracionesApi
      .getAll()
      .then((r) => setItems(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadValoraciones();
    Promise.all([profesoresApi.getAll(), asignaturasApi.getAll()]).then(
      ([p, a]) => {
        setProfesores(p.data);
        setAsignaturas(a.data);
      }
    );
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await valoracionesApi.create(form);
      setShowForm(false);
      loadValoraciones();
    } catch {
      alert('Error al crear valoración');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar esta valoración?')) return;
    try {
      await valoracionesApi.delete(id);
      setItems((prev) => prev.filter((v) => v.id !== id));
    } catch {
      alert('Error al eliminar');
    }
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Valoraciones</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
        >
          {showForm ? 'Cancelar' : '+ Nueva valoración'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="mb-6 space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Profesor</label>
              <select
                value={form.profesorId}
                onChange={(e) => setForm({ ...form, profesorId: Number(e.target.value) })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              >
                <option value={0} disabled>Seleccionar...</option>
                {profesores.map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre} {p.apellido}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Asignatura</label>
              <select
                value={form.asignaturaId}
                onChange={(e) => setForm({ ...form, asignaturaId: Number(e.target.value) })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              >
                <option value={0} disabled>Seleccionar...</option>
                {asignaturas.map((a) => (
                  <option key={a.id} value={a.id}>{a.nombre}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">
              Puntuación: {form.puntuacion}
            </label>
            <input
              type="range"
              min={1}
              max={5}
              value={form.puntuacion}
              onChange={(e) => setForm({ ...form, puntuacion: Number(e.target.value) })}
              className="w-full accent-indigo-600"
            />
            <div className="flex justify-between text-xs text-gray-400">
              <span>1</span><span>2</span><span>3</span><span>4</span><span>5</span>
            </div>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Comentario</label>
            <textarea
              value={form.comentario}
              onChange={(e) => setForm({ ...form, comentario: e.target.value })}
              rows={3}
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition"
          >
            {saving ? 'Guardando...' : 'Enviar valoración'}
          </button>
        </form>
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : items.length === 0 ? (
        <p className="py-12 text-center text-gray-500">No hay valoraciones</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((v) => (
            <div key={v.id} className="rounded-xl bg-white p-5 shadow-sm">
              <div className="mb-2 flex items-start justify-between">
                <div>
                  <p className="font-medium text-gray-900">{v.profesorNombre}</p>
                  <p className="text-xs text-gray-400">{v.asignaturaNombre}</p>
                </div>
                <div className="flex gap-0.5 text-amber-400">
                  {Array.from({ length: 5 }, (_, i) => (
                    <span key={i}>{i < v.puntuacion ? '★' : '☆'}</span>
                  ))}
                </div>
              </div>
              <p className="mb-3 text-sm text-gray-600">{v.comentario}</p>
              <div className="flex items-center justify-between">
                <p className="text-xs text-gray-400">Por: {v.alumnoNombre}</p>
                <button
                  onClick={() => handleDelete(v.id)}
                  className="rounded bg-red-50 px-2 py-1 text-xs text-red-600 hover:bg-red-100 transition"
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
