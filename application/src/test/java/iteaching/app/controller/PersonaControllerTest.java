package iteaching.app.controller;

import iteaching.app.dto.CsvImportResult;
import iteaching.app.service.PersonaService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = iteaching.app.application.Application.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@DisplayName("PersonaController - import CSV")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonaService personaService;

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
    void importCsv_emptyFile_returns400WithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv",
                "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/usuarios/import-csv").file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0]").value("Archivo CSV faltante o vacío"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
    void importCsv_serviceThrows_returns400WithMessage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "users.csv",
                "text/csv", "whatever".getBytes());

        when(personaService.importFromCsv(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(multipart("/api/usuarios/import-csv").file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0]").value("Error al importar CSV: boom"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void findAll_includesGradoFields() throws Exception {
        // create a dummy DTO with grade info
        iteaching.app.dto.UsuarioDTO dto = new iteaching.app.dto.UsuarioDTO();
        dto.setId(5L);
        dto.setUsername("u5");
        dto.setNombre("N");
        dto.setApellido("A");
        dto.setGradoId(2L);
        dto.setGradoNombre("Ingenieria");

        when(personaService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].gradoNombre").value("Ingenieria"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles="ADMIN")
    void findById_includesGradoFields() throws Exception {
        iteaching.app.dto.UsuarioDTO dto = new iteaching.app.dto.UsuarioDTO();
        dto.setId(7L);
        dto.setUsername("u7");
        dto.setGradoId(3L);
        dto.setGradoNombre("Matematicas");

        when(personaService.findById(7L)).thenReturn(dto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/usuarios/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".gradoNombre").value("Matematicas"));
    }
}
