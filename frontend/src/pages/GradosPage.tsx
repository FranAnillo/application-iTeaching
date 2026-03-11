import React, { useEffect, useState } from 'react';
import { gradosApi, asignaturasApi } from '../api/endpoints';
import type { Grado, Asignatura } from '../types';
import GradoModal from '../components/GradoModal';
import AsignaturaModal from '../components/AsignaturaModal';

export default function GradosPage() {
  const [grados, setGrados] = useState<Grado[]>([]);
  const [asignaturas, setAsignaturas] = useState<Asignatura[]>([]);
  const [showGradoModal, setShowGradoModal] = useState(false);
  const [editGrado, setEditGrado] = useState<Grado | null>(null);
  const [showAsignaturaModal, setShowAsignaturaModal] = useState<{ open: boolean, grado: Grado | null }>({ open: false, grado: null });
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    refresh();
    asignaturasApi.getAll().then(r => setAsignaturas(r.data));
  }, []);

  function refresh() {
    gradosApi.getAll().then(r => setGrados(r.data));
  }

  function handleCreateGrado(data: Omit<Grado, 'id'>) {
    gradosApi.create(data).then(() => {
      setShowGradoModal(false);
      refresh();
    });
  }

  function openEdit(g: Grado) {
    setEditGrado(g);
    setSelected(new Set(g.asignaturaIds || []));
  }

  function toggleAsignatura(id: number) {
    setSelected(prev => {
      const copy = new Set(prev);
      if (copy.has(id)) copy.delete(id);
      else copy.add(id);
      return copy;
    });
  }

  function saveEdit() {
    if (!editGrado) return;
    const original = new Set(editGrado.asignaturaIds || []);
    const added = Array.from(selected).filter(x => !original.has(x));
    const removed = Array.from(original).filter(x => !selected.has(x));
    setLoading(true);
    Promise.all([
      ...added.map(aid => gradosApi.addAsignatura(editGrado.id, aid)),
      ...removed.map(aid => gradosApi.removeAsignatura(editGrado.id, aid)),
    ]).then(() => {
      setLoading(false);
      setEditGrado(null);
      refresh();
    });
  }

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">Grados universitarios</h2>
      <div className="mb-4 flex gap-2">
        <button
          onClick={() => setShowGradoModal(true)}
          className="bg-indigo-600 text-white px-4 py-2 rounded flex items-center gap-2"
        >
          <span role="img" aria-label="Grado">🎓</span> Nuevo grado
        </button>
      </div>
      <table className="min-w-full mb-4">
        <thead>
          <tr>
            <th className="px-4 py-2 text-left">Nombre</th>
            <th className="px-4 py-2 text-left">Curso académico</th>
            <th className="px-4 py-2 text-left">Centro</th>
            <th className="px-4 py-2 text-left">Asignaturas</th>
            <th className="px-4 py-2 text-left">Acciones</th>
          </tr>
        </thead>
        <tbody>
          {grados.map(g => (
            <tr key={g.id} className="border">
              <td className="px-4 py-2">{g.nombre}</td>
              <td className="px-4 py-2">{g.cursoAcademico}</td>
              <td className="px-4 py-2">{g.centroImparticion || 'N/A'}</td>
              <td className="px-4 py-2">{g.asignaturaIds?.length ?? 0}</td>
              <td className="px-4 py-2 flex gap-2">
                <button
                  className="text-xs text-green-600 hover:underline"
                  onClick={() => setShowAsignaturaModal({ open: true, grado: g })}
                >Nueva asignatura</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {showGradoModal && (
        <GradoModal
          open={showGradoModal}
          onClose={() => setShowGradoModal(false)}
          onSubmit={handleCreateGrado}
        />
      )}

      {showAsignaturaModal.open && showAsignaturaModal.grado && (
        <AsignaturaModal
          open={showAsignaturaModal.open}
          onClose={() => setShowAsignaturaModal({ open: false, grado: null })}
          grado={showAsignaturaModal.grado}
          onSubmit={data => {
            asignaturasApi.create(data).then(() => {
              setShowAsignaturaModal({ open: false, grado: null });
              refresh();
            });
          }}
        />
      )}

      {editGrado && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-lg w-full max-w-md" onClick={e => e.stopPropagation()}>
            <h3 className="text-lg font-semibold mb-2">Asignaturas para {editGrado.nombre}</h3>
            <div className="max-h-64 overflow-y-auto mb-4">
              {asignaturas.map(a => (
                <label key={a.id} className="block text-sm">
                  <input
                    type="checkbox"
                    checked={selected.has(a.id)}
                    onChange={() => toggleAsignatura(a.id)}
                    className="mr-2"
                  />
                  {a.nombre}
                </label>
              ))}
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setEditGrado(null)}
                className="px-3 py-1 border rounded"
                disabled={loading}
              >Cancelar</button>
              <button
                onClick={saveEdit}
                className="px-3 py-1 bg-indigo-600 text-white rounded"
                disabled={loading}
              >Guardar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
