import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useState } from 'react';

const navItems = [
  { to: '/', label: 'Dashboard', icon: '🏠' },
  { to: '/asignaturas', label: 'Asignaturas', icon: '📚' },
  { to: '/usuarios', label: 'Usuarios', icon: '👥' },
  { to: '/clases', label: 'Clases', icon: '📅' },
  { to: '/valoraciones', label: 'Valoraciones', icon: '⭐' },
  { to: '/materiales', label: 'Materiales', icon: '📂' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const { dark, toggle } = useTheme();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 flex w-64 flex-col bg-indigo-700 dark:bg-gray-800 text-white transition-transform lg:static lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-16 items-center gap-2 px-6 font-bold text-xl">
          <span>🎓</span> iTeaching 2.0
        </div>

        <nav className="flex-1 space-y-1 px-3 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? 'bg-indigo-800 dark:bg-indigo-600 text-white'
                    : 'text-indigo-100 dark:text-gray-300 hover:bg-indigo-600 dark:hover:bg-gray-700'
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="border-t border-indigo-600 dark:border-gray-700 p-4">
          <div className="mb-2 text-sm text-indigo-200 dark:text-gray-400">
            {user?.username}{' '}
            <span className="rounded bg-indigo-800 dark:bg-gray-600 px-1.5 py-0.5 text-xs">
              {user?.role?.replace('ROLE_', '')}
            </span>
          </div>
          <button
            onClick={handleLogout}
            className="w-full rounded-lg bg-indigo-800 dark:bg-gray-700 px-3 py-2 text-sm font-medium hover:bg-indigo-900 dark:hover:bg-gray-600 transition"
          >
            Cerrar sesión
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Top bar */}
        <header className="flex h-16 items-center gap-4 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 px-4 shadow-sm lg:px-6 transition-colors">
          <button
            className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700 lg:hidden"
            onClick={() => setSidebarOpen(true)}
          >
            <svg className="h-6 w-6 text-gray-600 dark:text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <h1 className="text-lg font-semibold text-gray-800 dark:text-white">iTeaching 2.0 — Aula Virtual</h1>

          {/* Spacer */}
          <div className="flex-1" />

          {/* Dark mode toggle */}
          <button
            onClick={toggle}
            className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700 transition"
            title={dark ? 'Modo claro' : 'Modo oscuro'}
          >
            {dark ? (
              <svg className="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            ) : (
              <svg className="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            )}
          </button>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
