# iTeaching 2.0 — Aula Virtual

Plataforma de gestión académica que permite la interacción entre administradores, profesores y estudiantes. Incluye gestión de asignaturas, materiales, tareas, foros, valoraciones anónimas con moderación de contenido por IA, y más.

---

## Tabla de Contenidos

- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Ejecución](#instalación-y-ejecución)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [Docker (MySQL)](#docker-mysql)
- [Perfiles de Ejecución](#perfiles-de-ejecución)
- [Cuentas de Demostración](#cuentas-de-demostración)
- [API REST](#api-rest)
- [Modelos de Datos](#modelos-de-datos)
- [Seguridad](#seguridad)
- [Moderación de Contenido (IA)](#moderación-de-contenido-ia)
- [Estructura del Proyecto](#estructura-del-proyecto)

---

## Tecnologías

### Backend
| Tecnología        | Versión  |
|-------------------|----------|
| Java              | 17       |
| Spring Boot       | 3.2.5    |
| Spring Security   | 6.x      |
| Spring Data JPA   | 3.x      |
| JJWT              | 0.12.5   |
| MySQL             | 8.0      |
| H2 (desarrollo)   | Runtime  |
| Lombok             | —        |
| Maven Wrapper     | —        |

### Frontend
| Tecnología        | Versión  |
|-------------------|----------|
| React             | 18.2.0   |
| TypeScript        | 4.9.5    |
| Vite              | 2.9.18   |
| Tailwind CSS      | 3.4.3    |
| Axios             | 0.27.2   |
| React Router DOM  | 6.23.1   |

---

## Arquitectura

```
┌────────────────────┐       ┌──────────────────────┐       ┌────────────┐
│     Frontend       │──────▶│      Backend         │──────▶│  Database  │
│  React + Vite      │ :5173 │  Spring Boot REST    │ :8081 │  H2 / MySQL│
│  Tailwind CSS      │       │  JWT Authentication  │       │            │
└────────────────────┘       │  Content Moderation  │       └────────────┘
                              └──────────────────────┘
```

- **Frontend** se conecta al backend a través de un proxy Vite (`/api` → `http://localhost:8081`).
- **Autenticación** mediante tokens JWT (Bearer) en cabecera `Authorization`.
- **Base de datos**: H2 en memoria para desarrollo local, MySQL 8.0 para producción.

---

## Requisitos Previos

- **Java 17** (JDK): [Descargar](https://adoptium.net/)
- **Node.js** (compatible con Vite 2.9)
- **Docker** (opcional, para MySQL en producción)

---

## Instalación y Ejecución

### Backend

#### Perfil local (H2 en memoria — desarrollo)

```powershell
cd application
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

> **Nota:** el script `start.ps1` también ejecuta las instalaciones necesarias de npm para el frontend,
> así que no es imprescindible instalarlas manualmente antes de usarlo. Si arrancas el frontend
directamente (`npm run dev`), ejecuta `npm install` primero.

El backend estará disponible en `http://localhost:8081`.

La consola H2 estará accesible en `http://localhost:8081/h2-console`:
- **JDBC URL**: `jdbc:h2:mem:iteaching`
- **User**: `sa`
- **Password**: *(vacío)*

#### Perfil por defecto (MySQL — producción)

```powershell
cd application
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

Puerto: `8080`. Requiere MySQL corriendo en `localhost:3306` (ver [Docker](#docker-mysql)).

#### Compilar sin ejecutar

```powershell
cd application
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd clean compile
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

El frontend estará disponible en `http://localhost:5173`.

#### Build de producción

```powershell
cd frontend
npx vite build
```

Los artefactos se generan en `frontend/dist/`.

### Docker (MySQL)

```powershell
cd application
docker compose up -d
```

Levanta MySQL 8.0 con:
- **Base de datos**: `iteaching`
- **Usuario**: `myuser` / `secret`
- **Root**: `verysecret`
- **Puerto**: `3306`

---

## Perfiles de Ejecución

| Perfil    | Puerto | Base de Datos          | DDL-Auto      | H2 Console |
|-----------|--------|------------------------|---------------|------------|
| `local`   | 8081   | H2 en memoria          | `create-drop` | Sí         |
| `default` | 8080   | MySQL `localhost:3306`  | `update`      | No         |

---

## Cuentas de Demostración

Al arrancar la aplicación, se crean automáticamente tres cuentas de prueba:

| Usuario       | Contraseña   | Rol              |
|---------------|--------------|------------------|
| `admin`       | `Admin1234`  | `ROLE_ADMIN`     |
| `profesor`    | `Profe123!`  | `ROLE_PROFESOR`  |
| `estudiante`  | `Estud123!`  | `ROLE_ESTUDIANTE`|

---

## API REST

Base URL: `http://localhost:8081/api` (perfil local)

### Autenticación (`/api/auth`) — Pública

| Método | Ruta                | Descripción          |
|--------|---------------------|----------------------|
| POST   | `/api/auth/register` | Registrar usuario   |
| POST   | `/api/auth/login`    | Login (devuelve JWT)|

### Usuarios (`/api/usuarios`)

| Método | Ruta                    | Descripción            |
|--------|-------------------------|------------------------|
| GET    | `/api/usuarios`          | Listar usuarios       |
| GET    | `/api/usuarios/{id}`     | Obtener por ID        |
| GET    | `/api/usuarios/me`       | Usuario actual        |
| GET    | `/api/usuarios/search?q=`| Buscar usuarios       |

### Asignaturas (`/api/asignaturas`)

| Método | Ruta                                                | Descripción               |
|--------|-----------------------------------------------------|---------------------------|
| GET    | `/api/asignaturas`                                   | Listar todas             |
| GET    | `/api/asignaturas/{id}`                              | Obtener por ID           |
| GET    | `/api/asignaturas/search?q=`                         | Buscar                   |
| POST   | `/api/asignaturas`                                   | Crear                    |
| PUT    | `/api/asignaturas/{id}`                              | Actualizar               |
| DELETE | `/api/asignaturas/{id}`                              | Eliminar                 |
| POST   | `/api/asignaturas/import-csv`                        | Importar desde CSV       |
| POST   | `/api/asignaturas/{id}/profesores/{personaId}`       | Asignar profesor         |
| DELETE | `/api/asignaturas/{id}/profesores/{personaId}`       | Desasignar profesor      |
| POST   | `/api/asignaturas/{id}/estudiantes/{personaId}`      | Matricular estudiante    |
| DELETE | `/api/asignaturas/{id}/estudiantes/{personaId}`      | Desmatricular estudiante |
| POST   | `/api/asignaturas/{id}/inscribirse`                  | Auto-inscripción         |
| DELETE | `/api/asignaturas/{id}/desinscribirse`               | Auto-desinscripción      |

### Clases Particulares (`/api/clases`)

| Método | Ruta                               | Descripción              |
|--------|------------------------------------|--------------------------|
| GET    | `/api/clases`                       | Listar todas            |
| GET    | `/api/clases/{id}`                  | Obtener por ID          |
| GET    | `/api/clases/alumno/{username}`     | Por estudiante          |
| GET    | `/api/clases/profesor/{username}`   | Por profesor            |
| GET    | `/api/clases/estado/{estado}`       | Por estado              |
| POST   | `/api/clases`                       | Crear                   |
| PATCH  | `/api/clases/{id}/estado?estado=`   | Cambiar estado          |
| DELETE | `/api/clases/{id}`                  | Eliminar                |

### Valoraciones (`/api/valoraciones`) — Anónimas

| Método | Ruta                                                    | Descripción           |
|--------|---------------------------------------------------------|-----------------------|
| GET    | `/api/valoraciones`                                      | Listar todas         |
| GET    | `/api/valoraciones/profesor/{id}`                        | Por profesor         |
| GET    | `/api/valoraciones/asignatura/{id}`                      | Por asignatura       |
| GET    | `/api/valoraciones/profesor/{pid}/asignatura/{aid}`      | Por profesor+asig    |
| GET    | `/api/valoraciones/profesor/{id}/promedio`                | Promedio profesor    |
| POST   | `/api/valoraciones`                                      | Crear (ESTUDIANTE)   |
| DELETE | `/api/valoraciones/{id}`                                 | Eliminar (ADMIN)     |

### Materiales (`/api/materiales`)

| Método | Ruta                                  | Descripción           |
|--------|---------------------------------------|-----------------------|
| GET    | `/api/materiales`                      | Listar todos         |
| GET    | `/api/materiales/{id}`                 | Obtener por ID       |
| GET    | `/api/materiales/autor/{autorId}`      | Por autor            |
| GET    | `/api/materiales/mis-materiales`       | Mis materiales       |
| GET    | `/api/materiales/asignatura/{id}`      | Por asignatura       |
| GET    | `/api/materiales/search?q=`            | Buscar               |
| POST   | `/api/materiales`                      | Crear                |
| PUT    | `/api/materiales/{id}`                 | Actualizar           |
| DELETE | `/api/materiales/{id}`                 | Eliminar             |

### Anuncios (`/api/anuncios`)

| Método | Ruta                              | Descripción       |
|--------|-----------------------------------|-----------------  |
| GET    | `/api/anuncios/asignatura/{id}`    | Por asignatura   |
| GET    | `/api/anuncios/{id}`               | Obtener por ID   |
| POST   | `/api/anuncios`                    | Crear            |
| DELETE | `/api/anuncios/{id}`               | Eliminar         |

### Tareas (`/api/tareas`)

| Método | Ruta                           | Descripción       |
|--------|--------------------------------|-------------------|
| GET    | `/api/tareas/asignatura/{id}`   | Por asignatura   |
| GET    | `/api/tareas/{id}`              | Obtener por ID   |
| POST   | `/api/tareas`                   | Crear            |
| DELETE | `/api/tareas/{id}`              | Eliminar         |

### Entregas (`/api/entregas`)

| Método | Ruta                                                | Descripción       |
|--------|-----------------------------------------------------|-------------------|
| GET    | `/api/entregas/tarea/{tareaId}`                      | Por tarea        |
| GET    | `/api/entregas/mis-entregas`                         | Mis entregas     |
| POST   | `/api/entregas`                                      | Entregar         |
| PATCH  | `/api/entregas/{id}/calificar?calificacion=&comentario=` | Calificar    |

### Foro (`/api/foro`)

| Método | Ruta                            | Descripción            |
|--------|---------------------------------|------------------------|
| GET    | `/api/foro/asignatura/{id}`      | Temas por asignatura  |
| GET    | `/api/foro/temas/{id}`           | Tema con respuestas   |
| POST   | `/api/foro/temas`                | Crear tema            |
| POST   | `/api/foro/respuestas`           | Crear respuesta       |
| DELETE | `/api/foro/temas/{id}`           | Eliminar tema         |
| DELETE | `/api/foro/respuestas/{id}`      | Eliminar respuesta    |

### Grupos (`/api/grupos`)

| Método | Ruta                                          | Descripción          |
|--------|-----------------------------------------------|----------------------|
| GET    | `/api/grupos/asignatura/{id}`                  | Por asignatura      |
| GET    | `/api/grupos/{id}`                             | Obtener por ID      |
| POST   | `/api/grupos`                                  | Crear               |
| PUT    | `/api/grupos/{id}`                             | Actualizar          |
| DELETE | `/api/grupos/{id}`                             | Eliminar            |
| POST   | `/api/grupos/{id}/miembros/{personaId}`        | Añadir miembro      |
| DELETE | `/api/grupos/{id}/miembros/{personaId}`        | Quitar miembro      |

### Carpetas (`/api/carpetas`)

| Método | Ruta                                    | Descripción            |
|--------|-----------------------------------------|------------------------|
| GET    | `/api/carpetas/asignatura/{id}`          | Todas por asignatura  |
| GET    | `/api/carpetas/asignatura/{id}/root`     | Carpetas raíz         |
| GET    | `/api/carpetas/{id}/subcarpetas`         | Subcarpetas           |
| GET    | `/api/carpetas/{id}`                     | Obtener por ID        |
| POST   | `/api/carpetas`                          | Crear                 |
| PUT    | `/api/carpetas/{id}`                     | Actualizar            |
| DELETE | `/api/carpetas/{id}`                     | Eliminar              |

### Administración (`/api/admin`) — Solo ADMIN

| Método | Ruta                                | Descripción                |
|--------|-------------------------------------|----------------------------|
| PATCH  | `/api/admin/clases/{id}/cancelar`    | Cancelar clase fraudulenta|
| GET    | `/api/admin/audit-logs`              | Logs de auditoría         |
| GET    | `/api/admin/audit-logs/user/{user}`  | Logs por usuario          |

---

## Modelos de Datos

```
┌──────────┐       ┌──────────────┐       ┌────────────┐
│ Usuarios │◄──────│   Persona    │       │ Asignatura │
│  (base)  │       │  (herencia)  │──M:N──│            │
│  id, user│       │ nombre, email│       │ nombre,cod │
│  role    │       │ puntuacion   │       │ creditos   │
└──────────┘       └──────────────┘       └────────────┘
                          │                      │
                    ┌─────┴─────┐          ┌─────┴─────┐
                    ▼           ▼          ▼           ▼
              ┌──────────┐ ┌────────┐ ┌────────┐ ┌────────┐
              │  Clase   │ │Valorac.│ │ Tarea  │ │Material│
              │ particular│ │anónima │ │        │ │        │
              └──────────┘ └────────┘ └────────┘ └────────┘
                                          │
                                          ▼
                                    ┌──────────┐
                                    │ Entrega  │
                                    │(alumno+  │
                                    │ tarea)   │
                                    └──────────┘
```

### Entidades

| Entidad         | Tabla           | Descripción                                          |
|-----------------|-----------------|------------------------------------------------------|
| `Usuarios`      | `users`         | Entidad base (herencia JOINED): id, username, role   |
| `Persona`       | `personas`      | Extiende Usuarios: nombre, apellido, email, teléfono |
| `Asignatura`    | `asignaturas`   | Asignatura con profesores y estudiantes (M:N)        |
| `Clase`         | `clases`        | Clase particular alumno↔profesor con estados         |
| `Valoracion`    | `valoraciones`  | Valoración anónima de profesor por asignatura         |
| `Material`      | `materiales`    | Material didáctico con tipo (APUNTE, VIDEO, etc.)    |
| `Anuncio`       | `anuncios`      | Anuncio dentro de una asignatura                     |
| `Tarea`         | `tareas`        | Tarea o actividad de una asignatura                  |
| `Entrega`       | `entregas`      | Entrega de un estudiante para una tarea              |
| `ForoTema`      | `foro_temas`    | Tema de foro dentro de una asignatura                |
| `ForoRespuesta` | `foro_respuestas`| Respuesta a un tema de foro                         |
| `Grupo`         | `grupos`        | Grupo de teoría o práctica                           |
| `Carpeta`       | `carpetas`      | Carpeta jerárquica por asignatura                    |
| `AuditLog`      | `audit_logs`    | Registro de auditoría de acciones                    |

### Enumeraciones

| Enum            | Valores                                              |
|-----------------|------------------------------------------------------|
| `Role`          | `ROLE_ADMIN`, `ROLE_PROFESOR`, `ROLE_ESTUDIANTE`     |
| `EstadoClase`   | `SOLICITADA`, `ACEPTADA`, `RECHAZADA`, `CANCELADA`, `COMPLETADA` |
| `TipoMaterial`  | Definido en `Material.java`                          |
| `TipoGrupo`     | `TEORIA`, `PRACTICA`                                 |

---

## Seguridad

### Autenticación JWT
- Login en `/api/auth/login` devuelve un token JWT.
- El token se envía en la cabecera `Authorization: Bearer <token>`.
- Expiración: 24 horas.
- Sesiones stateless (sin cookies de sesión).

### Política de Contraseñas (ENS)
- Mínimo 8 caracteres
- Al menos una mayúscula, una minúscula, un dígito y un carácter especial
- No puede contener el nombre de usuario

### Control de Acceso por Roles
| Rol              | Permisos                                                              |
|------------------|-----------------------------------------------------------------------|
| `ROLE_ADMIN`     | Gestión completa: usuarios, asignaturas, clases, logs de auditoría   |
| `ROLE_PROFESOR`  | Gestión de sus asignaturas, materiales, tareas, calificaciones        |
| `ROLE_ESTUDIANTE`| Inscripción, entregas, valoraciones anónimas, foro                    |

### Otras medidas
- **CORS**: Solo `localhost:5173` y `localhost:3000`
- **CSRF**: Deshabilitado (protección JWT)
- **HSTS**: 1 año, incluye subdominios
- **X-Content-Type-Options**: `nosniff`
- **Cache-Control**: `no-cache, no-store`
- **Bloqueo de cuenta**: Tras 5 intentos fallidos en 15 minutos
- **Auditoría**: Registro de acciones en `AuditLog`

---

## Moderación de Contenido (IA)

Las valoraciones de profesores pasan por un sistema de moderación heurístico basado en IA (`ContentModerationService`) que analiza múltiples capas:

1. **Longitud**: Mínimo 10 caracteres, máximo 2000
2. **Mayúsculas excesivas**: Bloquea textos con >60% en mayúsculas
3. **Palabras prohibidas**: Diccionario de insultos y vulgaridades (ES/EN)
4. **Detección de leet speak**: Convierte sustituciones (`0→o`, `1→i`, `3→e`, `@→a`, etc.)
5. **Patrones ofensivos**: Regex para ataques personales, deseos negativos, etc.
6. **Puntuación excesiva**: Bloquea uso excesivo de `!!!` o `???`

Se aplica tanto al comentario como a los puntos de mejora.

---

## Estructura del Proyecto

```
application-iTeaching/
├── application/                          # Backend (Spring Boot)
│   ├── compose.yaml                      # Docker Compose (MySQL)
│   ├── mvnw.cmd                          # Maven Wrapper (Windows)
│   ├── pom.xml                           # Dependencias Maven
│   └── src/main/java/iteaching/app/
│       ├── application/                  # Clase principal
│       │   └── Application.java
│       ├── config/
│       │   └── DataSeeder.java           # Datos iniciales (3 cuentas demo)
│       ├── controller/                   # Controladores REST
│       │   ├── AdminController.java
│       │   ├── AnuncioController.java
│       │   ├── AsignaturaController.java
│       │   ├── AuthController.java
│       │   ├── CarpetaController.java
│       │   ├── ClaseController.java
│       │   ├── EntregaController.java
│       │   ├── ForoController.java
│       │   ├── GrupoController.java
│       │   ├── MaterialController.java
│       │   ├── PersonaController.java
│       │   ├── TareaController.java
│       │   └── ValoracionController.java
│       ├── dto/                          # DTOs (Data Transfer Objects)
│       │   ├── AnuncioDTO.java
│       │   ├── AsignaturaDTO.java
│       │   ├── AuthResponse.java
│       │   ├── CarpetaDTO.java
│       │   ├── ClaseCreateRequest.java
│       │   ├── ClaseDTO.java
│       │   ├── EntregaDTO.java
│       │   ├── ForoRespuestaDTO.java
│       │   ├── ForoTemaDTO.java
│       │   ├── GrupoDTO.java
│       │   ├── LoginRequest.java
│       │   ├── MaterialDTO.java
│       │   ├── RegisterRequest.java
│       │   ├── TareaDTO.java
│       │   ├── UsuarioDTO.java
│       │   └── ValoracionDTO.java
│       ├── exception/
│       │   └── GlobalExceptionHandler.java
│       ├── Models/                       # Entidades JPA
│       │   ├── Anuncio.java
│       │   ├── Asignatura.java
│       │   ├── AuditLog.java
│       │   ├── Carpeta.java
│       │   ├── Clase.java
│       │   ├── Entrega.java
│       │   ├── EstadoClase.java
│       │   ├── ForoRespuesta.java
│       │   ├── ForoTema.java
│       │   ├── Grupo.java
│       │   ├── Material.java
│       │   ├── Persona.java
│       │   ├── Tarea.java
│       │   ├── Usuarios.java
│       │   └── Valoracion.java
│       ├── repository/                   # Repositorios Spring Data
│       ├── security/                     # Configuración de seguridad
│       │   ├── CustomUserDetailsService.java
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtUtil.java
│       │   ├── PasswordPolicyValidator.java
│       │   └── SecurityConfig.java
│       └── service/                      # Lógica de negocio
│           ├── ContentModerationService.java  # IA moderación
│           └── ...Service.java
│
├── frontend/                             # Frontend (React + Vite)
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   └── src/
│       ├── App.tsx                       # Router y rutas
│       ├── main.tsx                      # Punto de entrada
│       ├── api/
│       │   ├── client.ts                 # Axios con interceptor JWT
│       │   └── endpoints.ts              # Funciones API tipadas
│       ├── components/
│       │   ├── Layout.tsx                # Layout principal
│       │   └── PrivateRoute.tsx          # Ruta protegida
│       ├── context/
│       │   └── AuthContext.tsx           # Estado de autenticación
│       ├── pages/
│       │   ├── LoginPage.tsx
│       │   ├── RegisterPage.tsx
│       │   ├── DashboardPage.tsx
│       │   ├── AsignaturasPage.tsx
│       │   ├── AsignaturaDetailPage.tsx
│       │   ├── ClasesPage.tsx
│       │   ├── MaterialesPage.tsx
│       │   ├── ValoracionesPage.tsx
│       │   └── UsuariosPage.tsx
│       └── types/
│           └── index.ts                  # Interfaces TypeScript
│
└── README.md
```

---

## Licencia

Proyecto académico — iTeaching 2.0.

## Arranque de los dos servicios (Front y back)
powershell -ExecutionPolicy Bypass -File .\start.ps1