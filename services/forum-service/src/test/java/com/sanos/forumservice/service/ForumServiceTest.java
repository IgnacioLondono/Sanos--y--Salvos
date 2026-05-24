package com.sanos.forumservice.service;

import com.sanos.forumservice.dto.CreatePostRequest;
import com.sanos.forumservice.dto.CreateThreadRequest;
import com.sanos.forumservice.dto.PostDto;
import com.sanos.forumservice.dto.ThreadDetailDto;
import com.sanos.forumservice.dto.ThreadDto;
import com.sanos.forumservice.model.HiloForo;
import com.sanos.forumservice.model.MensajeForo;
import com.sanos.forumservice.repository.HiloForoRepository;
import com.sanos.forumservice.repository.MensajeForoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceTest {

    @Mock
    private HiloForoRepository hiloRepo;
    @Mock
    private MensajeForoRepository mensajeRepo;

    private ForumService service;

    @BeforeEach
    void setUp() {
        service = new ForumService(hiloRepo, mensajeRepo);
    }

    @Test
    void listThreads_withCategory_usesFilteredQuery() {
        HiloForo hilo = hilo(1L, "Titulo");
        when(hiloRepo.findByCategoriaIgnoreCaseOrderByFechaActualizacionDesc("AYUDA")).thenReturn(List.of(hilo));
        when(mensajeRepo.countByIdHilo(1L)).thenReturn(1L);
        when(mensajeRepo.findFirstByIdHiloOrderByFechaCreacionAsc(1L)).thenReturn(Optional.empty());

        List<ThreadDto> result = service.listThreads("AYUDA");

        assertEquals(1, result.size());
        verify(hiloRepo).findByCategoriaIgnoreCaseOrderByFechaActualizacionDesc("AYUDA");
        verify(hiloRepo, never()).findAllByOrderByFechaActualizacionDesc();
    }

    @Test
    void createThread_createsFirstMessageAndReturnsDetail() {
        CreateThreadRequest req = new CreateThreadRequest(
                "Como reportar mascota", "Necesito ayuda para usar el mapa", "ayuda", 10L, "Ana"
        );
        HiloForo savedHilo = hilo(20L, "Como reportar mascota");
        savedHilo.setCategoria("AYUDA");
        savedHilo.setIdUsuario(10L);
        savedHilo.setNombreAutor("Ana");

        when(hiloRepo.save(any(HiloForo.class))).thenReturn(savedHilo);
        when(hiloRepo.findById(20L)).thenReturn(Optional.of(savedHilo));
        when(mensajeRepo.save(any(MensajeForo.class))).thenAnswer(inv -> {
            MensajeForo m = inv.getArgument(0);
            m.setIdMensaje(99L);
            m.setFechaCreacion(LocalDateTime.now());
            return m;
        });
        when(mensajeRepo.findByIdHiloOrderByFechaCreacionAsc(20L)).thenReturn(List.of(firstMsg(20L)));
        when(mensajeRepo.countByIdHilo(20L)).thenReturn(1L);
        when(mensajeRepo.findFirstByIdHiloOrderByFechaCreacionAsc(20L)).thenReturn(Optional.of(firstMsg(20L)));

        ThreadDetailDto detail = service.createThread(req);

        assertEquals(20L, detail.thread().id());
        assertEquals(1, detail.posts().size());
        verify(mensajeRepo).save(any(MensajeForo.class));
    }

    @Test
    void createThread_rejectsShortTitle() {
        CreateThreadRequest req = new CreateThreadRequest("abc", "Contenido largo suficiente", "AYUDA", 1L, "Ana");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createThread(req));

        assertTrue(ex.getMessage().contains("titulo"));
    }

    @Test
    void addPost_rejectsBlankMessage() {
        CreatePostRequest req = new CreatePostRequest("   ", 2L, "Pedro");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addPost(5L, req));

        assertTrue(ex.getMessage().contains("vacio"));
    }

    @Test
    void addPost_updatesThreadTimestamp() {
        HiloForo hilo = hilo(5L, "Tema");
        when(hiloRepo.findById(5L)).thenReturn(Optional.of(hilo));
        when(mensajeRepo.save(any(MensajeForo.class))).thenAnswer(inv -> {
            MensajeForo m = inv.getArgument(0);
            m.setIdMensaje(10L);
            m.setFechaCreacion(LocalDateTime.now());
            return m;
        });

        PostDto dto = service.addPost(5L, new CreatePostRequest("Respuesta util", 3L, "Carlos"));

        assertEquals(5L, dto.threadId());
        verify(hiloRepo).save(hilo);
    }

    @Test
    void getThread_throwsWhenNotFound() {
        when(hiloRepo.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.getThread(404L));
    }

    private HiloForo hilo(Long id, String titulo) {
        HiloForo h = new HiloForo();
        h.setIdHilo(id);
        h.setTitulo(titulo);
        h.setCategoria("GENERAL");
        h.setNombreAutor("Autor");
        h.setFechaCreacion(LocalDateTime.now());
        h.setFechaActualizacion(LocalDateTime.now());
        return h;
    }

    private MensajeForo firstMsg(Long threadId) {
        MensajeForo m = new MensajeForo();
        m.setIdMensaje(1L);
        m.setIdHilo(threadId);
        m.setContenido("Mensaje inicial del hilo");
        m.setIdUsuario(1L);
        m.setNombreAutor("Autor");
        m.setFechaCreacion(LocalDateTime.now());
        return m;
    }
}
