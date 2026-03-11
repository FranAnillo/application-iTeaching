import { useState } from 'react';
import type { Grado } from '../types';

const CURSOS = [
  { value: '2025-2026', label: '2025-2026' },
  { value: '2026-2027', label: '2026-2027' },
  { value: '2027-2028', label: '2027-2028' },
];

export interface GradoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: Omit<Grado, 'id'>) => void;
  initial?: Partial<Grado>;
}

export default function GradoModal({ open, onClose, onSubmit, initial }: GradoModalProps) {
  const [nombre, setNombre] = useState(initial?.nombre || '');
  const [cursoAcademico, setCursoAcademico] = useState(initial?.cursoAcademico || CURSOS[0].value);
  const [centroImparticion, setCentroImparticion] = useState(initial?.centroImparticion || '');
  const [error, setError] = useState('');

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50" onClick={onClose}>
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl dark:bg-gray-800" onClick={e => e.stopPropagation()}>
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white flex gap-2">
            <span role="img" aria-label="Grado">🎓</span> Nuevo Grado
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
        <form
          onSubmit={e => {
            e.preventDefault();
            if (!nombre.trim()) {
              setError('El nombre es obligatorio');
              return;
            }
            setError('');
            onSubmit({ 
              nombre, 
              cursoAcademico: cursoAcademico as any, 
              centroImparticion, 
              asignaturaIds: initial?.asignaturaIds || [] 
            });
          }}
        >
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              <span role="img" aria-label="Nombre">🏷️</span> Nombre del grado
            </label>
            <input
              type="text"
              value={nombre}
              onChange={e => setNombre(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white"
              placeholder="Ejemplo: Ingeniería"
              autoFocus
            />
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              <span role="img" aria-label="Curso">📅</span> Curso académico
            </label>
            <select
              value={cursoAcademico}
              onChange={e => setCursoAcademico(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white"
            >
              {CURSOS.map(c => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              <span role="img" aria-label="Centro">🏢</span> Centro de impartición
            </label>
            <input
              type="text"
              value={centroImparticion}
              onChange={e => setCentroImparticion(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white"
              placeholder="Ejemplo: Facultad de Informática"
            />
          </div>
          {error && <div className="mb-2 text-xs text-red-600 dark:text-red-400">{error}</div>}
          <div className="flex gap-3 mt-4">
            <button
              type="submit"
              className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 transition"
            >
              Guardar
            </button>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 transition dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
