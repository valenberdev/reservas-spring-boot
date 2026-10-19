package com.valenberdev.reservas.reserva;

import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.support.IntegracionTest;
import com.valenberdev.reservas.usuario.Rol;
import com.valenberdev.reservas.usuario.Usuario;
import com.valenberdev.reservas.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservaPaginacionTest extends IntegracionTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ReservaRepository reservaRepository;

    @Autowired
    RecursoRepository recursoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void prepararDatos() throws Exception {
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
        recursoRepository.deleteAll();

        Recurso recurso = recursoRepository.save(new Recurso("Cancha", null));
        Usuario ana = usuarioRepository.save(
                new Usuario("ana@test.com", passwordEncoder.encode("clave12345"), Rol.CLIENTE));

        Instant base = Instant.now().plus(30, ChronoUnit.DAYS);
        for (int i = 0; i < 25; i++) {
            Instant inicio = base.plus(i, ChronoUnit.HOURS);
            reservaRepository.save(new Reserva(recurso, ana, inicio, inicio.plus(30, ChronoUnit.MINUTES)));
        }

        token = login("ana@test.com", "clave12345");
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Matcher matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    @Test
    void devuelveLaPrimeraPaginaConLosTotales() throws Exception {
        mockMvc.perform(get("/api/reservas").param("page", "0").param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(10))
                .andExpect(jsonPath("$.totalElementos").value(25))
                .andExpect(jsonPath("$.totalPaginas").value(3));
    }

    @Test
    void laUltimaPaginaTieneSoloLosRestantes() throws Exception {
        mockMvc.perform(get("/api/reservas").param("page", "2").param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(5));
    }

    @Test
    void recortaElTamanioAlMaximoPermitido() throws Exception {
        mockMvc.perform(get("/api/reservas").param("size", "1000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanio").value(50));
    }
}