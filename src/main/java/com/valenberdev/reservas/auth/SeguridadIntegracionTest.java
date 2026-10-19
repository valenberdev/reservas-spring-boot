package com.valenberdev.reservas.auth;

import com.valenberdev.reservas.recurso.Recurso;
import com.valenberdev.reservas.recurso.RecursoRepository;
import com.valenberdev.reservas.reserva.Reserva;
import com.valenberdev.reservas.reserva.ReservaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeguridadIntegracionTest extends IntegracionTest {

    private static final String JSON_RECURSO = "{\"nombre\":\"Cancha 2\",\"descripcion\":\"Futbol 7\"}";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RecursoRepository recursoRepository;

    @Autowired
    ReservaRepository reservaRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararDatos() {
        reservaRepository.deleteAll();
        usuarioRepository.deleteAll();
        recursoRepository.deleteAll();
        crearUsuario("ana@test.com", "clave12345", Rol.CLIENTE);
        crearUsuario("admin@test.com", "admin12345", Rol.ADMIN);
    }

    private Usuario crearUsuario(String email, String password, Rol rol) {
        return usuarioRepository.save(new Usuario(email, passwordEncoder.encode(password), rol));
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
    void sinTokenDevuelve401ConFormatoProblemDetail() throws Exception {
        mockMvc.perform(get("/api/recursos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void tokenAlteradoDevuelve401() throws Exception {
        String token = login("ana@test.com", "clave12345");

        mockMvc.perform(get("/api/recursos").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginConPasswordIncorrectoDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePuedeListarRecursos() throws Exception {
        String token = login("ana@test.com", "clave12345");

        mockMvc.perform(get("/api/recursos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeCrearRecursos() throws Exception {
        String token = login("ana@test.com", "clave12345");

        mockMvc.perform(post("/api/recursos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_RECURSO))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrearRecursos() throws Exception {
        String token = login("admin@test.com", "admin12345");

        mockMvc.perform(post("/api/recursos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_RECURSO))
                .andExpect(status().isCreated());
    }

    @Test
    void registroPublicoCreaClienteYNoExponeElHash() throws Exception {
        String body = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"Nuevo@Test.com\",\"password\":\"clave12345\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(body)
                .contains("nuevo@test.com")
                .contains("\"rol\":\"CLIENTE\"")
                .doesNotContain("password")
                .doesNotContain("$2a$");
    }

    @Test
    void registroNoPermiteAutoasignarseElRolAdmin() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"intruso@test.com\",\"password\":\"clave12345\",\"rol\":\"ADMIN\"}"));

        assertThat(usuarioRepository.findAll())
                .noneMatch(u -> u.getEmail().equals("intruso@test.com") && u.getRol() == Rol.ADMIN);
    }

    @Test
    void soloElTitularPuedeCancelarSuReserva() throws Exception {
        Usuario ana = usuarioRepository.findByEmail("ana@test.com").orElseThrow();
        crearUsuario("beto@test.com", "clave12345", Rol.CLIENTE);
        Recurso recurso = recursoRepository.save(new Recurso("Cancha 1", null));
        Instant inicio = Instant.now().plus(30, ChronoUnit.DAYS);
        Reserva reserva = reservaRepository.save(
                new Reserva(recurso, ana, inicio, inicio.plus(1, ChronoUnit.HOURS)));

        String tokenBeto = login("beto@test.com", "clave12345");
        mockMvc.perform(patch("/api/reservas/" + reserva.getId() + "/cancelar")
                        .header("Authorization", "Bearer " + tokenBeto))
                .andExpect(status().isNotFound());

        String tokenAna = login("ana@test.com", "clave12345");
        mockMvc.perform(patch("/api/reservas/" + reserva.getId() + "/cancelar")
                        .header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk());
    }
}