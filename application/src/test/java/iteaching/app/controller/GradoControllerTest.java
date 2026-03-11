package iteaching.app.controller;

import iteaching.app.dto.GradoDTO;
import iteaching.app.service.GradoService;
import iteaching.app.Models.Asignatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = iteaching.app.application.Application.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@DisplayName("GradoController API")
class GradoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradoService gradoService;

    @Test
    @WithMockUser
    void list_returnsGrades() throws Exception {
        GradoDTO g = new GradoDTO();
        g.setId(1L);
        g.setNombre("Ingenieria");
        when(gradoService.findAll()).thenReturn(List.of(g));

        mockMvc.perform(get("/api/grados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].nombre").value("Ingenieria"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_grade_asAdmin() throws Exception {
        GradoDTO g = new GradoDTO();
        g.setId(1L);
        g.setNombre("Historia");
        when(gradoService.save(any())).thenReturn(g);

        mockMvc.perform(post("/api/grados").contentType("application/json").content("{\"nombre\":\"Historia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".nombre").value("Historia"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addAsignatura_asAdmin() throws Exception {
        GradoDTO g = new GradoDTO();
        g.setId(1L);
        g.setNombre("Data");
        when(gradoService.addAsignatura(eq(1L), eq(5L))).thenReturn(g);

        mockMvc.perform(post("/api/grados/1/asignaturas/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeAsignatura_asAdmin() throws Exception {
        GradoDTO g = new GradoDTO();
        g.setId(1L);
        g.setNombre("Data");
        when(gradoService.removeAsignatura(eq(1L), eq(5L))).thenReturn(g);

        mockMvc.perform(delete("/api/grados/1/asignaturas/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void listAsignaturas_returnsSet() throws Exception {
        Asignatura a = new Asignatura();
        a.setId(10L);
        a.setNombre("X");
        when(gradoService.getAsignaturas(1L)).thenReturn(List.of(a));

        mockMvc.perform(get("/api/grados/1/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].nombre").value("X"));
    }
}
