import { useEffect, useState, type FormEvent } from 'react';
import { clasesApi, estudiantesApi, profesoresApi, asignaturasApi } from '../api/endpoints';
import type { Clase, Estudiante, Profesor, Asignatura } from '../types';

const estadoBadge: Record<string, string> = {
  SOLICITADA: 'bg-yellow-100 text-yellow-800',
  ACEPTADA: 'bg-green-100 text-green-800',
  RECHAZADA: 'bg-red-100 text-red-800',
  CANCELADA: 'bg-gray-100 text-gray-800',
  COMPLETADA: 'bg-blue-100 text-blue-800',
};

export default function ClasesPage() {
  const [items, setItems] = useState<Clase[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [estudiantes, setEstudiantes] = useState<Estudiante[]>([]);
  const [profesores, setProfesores] = useState<Profesor[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [form, setForm] = useState({
    horaComienzo: '',
    horaFin: '',
    alumnoId: 0,
    profesorId: 0,
    asignaturaId: 0,
  });
  const [saving, setSaving] = useState(false);

  const loadClases = () => {
    clasesApi
      .getAll()
      .then((r) => setItems(r.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadClases();
    Promise.all([
      estudiantesApi.getAll(),
      profesoresApi.getAll(),
      asignaturasApi.getAll(),
    ]).then(([e, p, a]) => {
      setEstudiantes(e.data);
      setProfesores(p.data);
      setAsignaturas(a.data);
    });
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await clasesApi.create(form);
      setShowForm(false);
      loadClases();
    } catch {
      alert('Error al crear clase');
    } finally {
      setSaving(false);
    }
  };

  const changeEstado = async (id: number, estado: string) => {
    try {
      await clasesApi.updateEstado(id, estado);
      loadClases();
    } catch {
      alert('Error al cambiar estado');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Eliminar esta clase?')) return;
    try {
      await clasesApi.delete(id);
      setItems((prev) => prev.filter((c) => c.id !== id));
    } catch {
      alert('Error al eliminar');
    }
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Clases</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
        >
          {showForm ? 'Cancelar' : '+ Nueva clase'}
        </button>
      </div>

      {/* Create form */}
      {showForm && (
        <form onSubmit={handleCreate} className="mb-6 space-y-4 rounded-xl bg-white p-6 shadow-sm">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Hora inicio</label>
              <input
                type="datetime-local"
                value={form.horaComienzo}
                onChange={(e) => setForm({ ...form, horaComienzo: e.target.value })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Hora fin</label>
              <input
                type="datetime-local"
                value={form.horaFin}
                onChange={(e) => setForm({ ...form, horaFin: e.target.value })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Estudiante</label>
              <select
                value={form.alumnoId}
                onChange={(e) => setForm({ ...form, alumnoId: Number(e.target.value) })}
                required
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              >
                <option value={0} disabled>Seleccionar...</option>
                {estudiantes.map((e) => (
                  <option key={e.id} value={e.id}>{e.nombre} {e.apellido}</option>
                ))}
              </select>
            </div>
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
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 transition"
          >
            {saving ? 'Creando...' : 'Crear clase'}
          </button>
        </form>
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent" />
        </div>
      ) : items.length === 0 ? (
        <p className="py-12 text-center text-gray-500">No hay clases registradas</p>
      ) : (
        <div className="overflow-hidden rounded-xl bg-white shadow-sm">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Asignatura</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Profesor</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Estudiante</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Horario</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Estado</th>
                <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((c) => (
                <tr key={c.id} className="hover:bg-gray-50 transition">
                  <td className="px-4 py-3 text-sm font-medium text-gray-900">{c.asignaturaNombre}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">{c.profesorNombre}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">{c.alumnoNombre}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {c.horaComienzo} — {c.horaFin}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${estadoBadge[c.estadoClase] ?? 'bg-gray-100'}`}>
                      {c.estadoClase}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-1">
                      {c.estadoClase === 'SOLICITADA' && (
                        <>
                          <button onClick={() => changeEstado(c.id, 'ACEPTADA')} className="rounded bg-green-50 px-2 py-1 text-xs text-green-700 hover:bg-green-100">Aceptar</button>
                          <button onClick={() => changeEstado(c.id, 'RECHAZADA')} className="rounded bg-red-50 px-2 py-1 text-xs text-red-700 hover:bg-red-100">Rechazar</button>
                        </>
                      )}
                      {c.estadoClase === 'ACEPTADA' && (
                        <button onClick={() => changeEstado(c.id, 'COMPLETADA')} className="rounded bg-blue-50 px-2 py-1 text-xs text-blue-700 hover:bg-blue-100">Completar</button>
                      )}
                      <button onClick={() => handleDelete(c.id)} className="rounded bg-red-50 px-2 py-1 text-xs text-red-600 hover:bg-red-100">Eliminar</button>
                    </div>
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
