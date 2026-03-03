import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AsignaturasPage from './pages/AsignaturasPage';
import AsignaturaDetailPage from './pages/AsignaturaDetailPage';
import ProfesoresPage from './pages/ProfesoresPage';
import EstudiantesPage from './pages/EstudiantesPage';
import ClasesPage from './pages/ClasesPage';
import ValoracionesPage from './pages/ValoracionesPage';

export default function App() {
  return (
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
            <Route path="profesores" element={<ProfesoresPage />} />
            <Route path="estudiantes" element={<EstudiantesPage />} />
            <Route path="clases" element={<ClasesPage />} />
            <Route path="valoraciones" element={<ValoracionesPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
