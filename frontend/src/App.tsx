import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AsignaturasPage from './pages/AsignaturasPage';
import AsignaturaDetailPage from './pages/AsignaturaDetailPage';
import UsuariosPage from './pages/UsuariosPage';
import ClasesPage from './pages/ClasesPage';
import ValoracionesPage from './pages/ValoracionesPage';
import MaterialesPage from './pages/MaterialesPage';
import CalendarioPage from './pages/CalendarioPage';
import MensajesPage from './pages/MensajesPage';
import CalificacionesGlobalPage from './pages/CalificacionesGlobalPage';
import LogrosPage from './pages/LogrosPage';
import ChatPage from './pages/ChatPage';

export default function App() {
  return (
    <ThemeProvider>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected */}
          <Route
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="asignaturas" element={<AsignaturasPage />} />
            <Route path="asignaturas/:id" element={<AsignaturaDetailPage />} />
            <Route path="calendario" element={<CalendarioPage />} />
            <Route path="mensajes" element={<MensajesPage />} />
            <Route path="chat" element={<ChatPage />} />
            <Route path="calificaciones" element={<CalificacionesGlobalPage />} />
            <Route path="logros" element={<LogrosPage />} />
            <Route path="usuarios" element={<UsuariosPage />} />
            <Route path="clases" element={<ClasesPage />} />
            <Route path="valoraciones" element={<ValoracionesPage />} />
            <Route path="materiales" element={<MaterialesPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
    </ThemeProvider>
  );
}
