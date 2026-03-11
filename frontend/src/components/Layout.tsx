import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useState, useEffect } from 'react';
import { notificacionesApi } from '../api/endpoints';
import type { Notificacion } from '../types';

const allNavItems = [
  { to: '/', label: 'Dashboard', icon: '🏠', roles: null },
  { to: '/asignaturas', label: 'Asignaturas', icon: '📚', roles: null },
  { to: '/calendario', label: 'Calendario', icon: '📆', roles: null },
  { to: '/chat', label: 'Chat', icon: '🟢', roles: null },
  { to: '/mensajes', label: 'Mensajes', icon: '💬', roles: null },
  { to: '/calificaciones', label: 'Calificaciones', icon: '📊', roles: null },
  { to: '/logros', label: 'Logros', icon: '🏆', roles: null },
  { to: '/usuarios', label: 'Usuarios', icon: '👥', roles: ['ROLE_ADMIN', 'ROLE_PROFESOR'] },
  { to: '/grados', label: 'Grados', icon: '🎓', roles: ['ROLE_ADMIN'] },
  { to: '/clases', label: 'Clases', icon: '📅', roles: ['ROLE_ADMIN', 'ROLE_PROFESOR'] },
  { to: '/valoraciones', label: 'Valoraciones', icon: '⭐', roles: ['ROLE_ADMIN', 'ROLE_PROFESOR'] },
  { to: '/materiales', label: 'Materiales', icon: '📂', roles: ['ROLE_ADMIN', 'ROLE_PROFESOR'] },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const { dark, toggle } = useTheme();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifCount, setNotifCount] = useState(0);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notificaciones, setNotificaciones] = useState<Notificacion[]>([]);

  useEffect(function () {
    loadNotifications();
    var interval = setInterval(loadNotifications, 30000);
    return function () { clearInterval(interval); };
  }, []);

  function loadNotifications() {
    notificacionesApi.countNoLeidas().then(function (res) {
      setNotifCount(res.data.count);
    }).catch(function () {});
  }

  function openNotifications() {
    setNotifOpen(!notifOpen);
    if (!notifOpen) {
      notificacionesApi.getNoLeidas().then(function (res) {
        setNotificaciones(res.data);
      }).catch(function () {});
    }
  }

  function markAllRead() {
    notificacionesApi.marcarTodasLeidas().then(function () {
      setNotifCount(0);
      setNotificaciones([]);
      setNotifOpen(false);
    }).catch(function () {});
  }

  function markOneRead(id: number) {
    notificacionesApi.marcarLeida(id).then(function () {
      setNotificaciones(notificaciones.filter(function (n) { return n.id !== id; }));
      setNotifCount(Math.max(0, notifCount - 1));
    }).catch(function () {});
  }

  // Filter nav items by role
  const navItems = allNavItems.filter(function (item) {
    if (!item.roles) return true;
    return user && item.roles.indexOf(user.role) !== -1;
  });

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

          {/* Notification bell */}
          <div className="relative">
            <button
              onClick={openNotifications}
              className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700 transition relative"
              title="Notificaciones"
            >
              <svg className="h-5 w-5 text-gray-500 dark:text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              {notifCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center font-bold">
                  {notifCount > 9 ? '9+' : notifCount}
                </span>
              )}
            </button>

            {/* Notification dropdown */}
            {notifOpen && (
              <div className="absolute right-0 top-12 w-80 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 z-50 max-h-96 overflow-hidden flex flex-col">
                <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
                  <h4 className="font-semibold text-sm text-gray-900 dark:text-white">Notificaciones</h4>
                  {notificaciones.length > 0 && (
                    <button onClick={markAllRead} className="text-xs text-indigo-600 dark:text-indigo-400 hover:underline">
                      Marcar todas como leídas
                    </button>
                  )}
                </div>
                <div className="overflow-y-auto flex-1">
                  {notificaciones.length === 0 ? (
                    <p className="text-center text-sm text-gray-500 dark:text-gray-400 py-6">
                      No hay notificaciones nuevas
                    </p>
                  ) : (
                    notificaciones.slice(0, 10).map(function (n) {
                      var iconMap: Record<string, string> = { INFO: 'ℹ️', ANUNCIO: '📢', TAREA: '📝', CALIFICACION: '📊', MENSAJE: '💬', LOGRO: '🏆' };
                      return (
                        <div
                          key={n.id}
                          className="px-4 py-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer"
                          onClick={function () { markOneRead(n.id); if (n.enlace) navigate(n.enlace); }}
                        >
                          <div className="flex items-start gap-2">
                            <span className="text-sm">{iconMap[n.tipo] || 'ℹ️'}</span>
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{n.titulo}</p>
                              <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{n.mensaje}</p>
                              <p className="text-xs text-gray-400 dark:text-gray-500 mt-0.5">{n.fechaCreacion.substring(0, 16).replace('T', ' ')}</p>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            )}
          </div>

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
