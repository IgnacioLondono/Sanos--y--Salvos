package com.sanos.forumservice.service;

import com.sanos.forumservice.dto.*;
import com.sanos.forumservice.model.HiloForo;
import com.sanos.forumservice.model.MensajeForo;
import com.sanos.forumservice.repository.HiloForoRepository;
import com.sanos.forumservice.repository.MensajeForoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumService {

    private final HiloForoRepository hiloRepo;
    private final MensajeForoRepository mensajeRepo;

    public ForumService(HiloForoRepository hiloRepo, MensajeForoRepository mensajeRepo) {
        this.hiloRepo = hiloRepo;
        this.mensajeRepo = mensajeRepo;
    }

    @Transactional(readOnly = true)
    public List<ThreadDto> listThreads(String category) {
        List<HiloForo> hilos = category == null || category.isBlank()
                ? hiloRepo.findAllByOrderByFechaActualizacionDesc()
                : hiloRepo.findByCategoriaIgnoreCaseOrderByFechaActualizacionDesc(category.trim());
        return hilos.stream().map(this::toThreadDto).toList();
    }

    @Transactional(readOnly = true)
    public ThreadDetailDto getThread(Long threadId) {
        HiloForo hilo = hiloRepo.findById(threadId)
                .orElseThrow(() -> new IllegalStateException("Hilo no encontrado"));
        List<PostDto> posts = mensajeRepo.findByIdHiloOrderByFechaCreacionAsc(threadId).stream()
                .map(this::toPostDto)
                .toList();
        return new ThreadDetailDto(toThreadDto(hilo), posts);
    }

    @Transactional
    public ThreadDetailDto createThread(CreateThreadRequest req) {
        validateThread(req.title(), req.content());
        String category = normalizeCategory(req.category());

        HiloForo hilo = new HiloForo();
        hilo.setTitulo(req.title().trim());
        hilo.setCategoria(category);
        hilo.setIdUsuario(req.authorId());
        hilo.setNombreAutor(resolveAuthor(req.authorName()));
        hilo = hiloRepo.save(hilo);

        MensajeForo first = new MensajeForo();
        first.setIdHilo(hilo.getIdHilo());
        first.setContenido(req.content().trim());
        first.setIdUsuario(req.authorId());
        first.setNombreAutor(hilo.getNombreAutor());
        mensajeRepo.save(first);

        return getThread(hilo.getIdHilo());
    }

    @Transactional
    public PostDto addPost(Long threadId, CreatePostRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacio");
        }
        HiloForo hilo = hiloRepo.findById(threadId)
                .orElseThrow(() -> new IllegalStateException("Hilo no encontrado"));

        MensajeForo msg = new MensajeForo();
        msg.setIdHilo(threadId);
        msg.setContenido(req.content().trim());
        msg.setIdUsuario(req.authorId());
        msg.setNombreAutor(resolveAuthor(req.authorName()));
        msg = mensajeRepo.save(msg);

        hilo.setFechaActualizacion(msg.getFechaCreacion());
        hiloRepo.save(hilo);

        return toPostDto(msg);
    }

    private ThreadDto toThreadDto(HiloForo hilo) {
        long total = mensajeRepo.countByIdHilo(hilo.getIdHilo());
        int replies = (int) Math.max(0, total - 1);
        String preview = mensajeRepo.findFirstByIdHiloOrderByFechaCreacionAsc(hilo.getIdHilo())
                .map(m -> truncate(m.getContenido(), 140))
                .orElse("");
        return new ThreadDto(
                hilo.getIdHilo(),
                hilo.getTitulo(),
                hilo.getCategoria(),
                hilo.getIdUsuario(),
                hilo.getNombreAutor(),
                preview,
                replies,
                formatDate(hilo.getFechaCreacion()),
                formatDate(hilo.getFechaActualizacion())
        );
    }

    private PostDto toPostDto(MensajeForo m) {
        return new PostDto(
                m.getIdMensaje(),
                m.getIdHilo(),
                m.getContenido(),
                m.getIdUsuario(),
                m.getNombreAutor(),
                formatDate(m.getFechaCreacion())
        );
    }

    private void validateThread(String title, String content) {
        if (title == null || title.trim().length() < 5) {
            throw new IllegalArgumentException("El titulo debe tener al menos 5 caracteres");
        }
        if (content == null || content.trim().length() < 10) {
            throw new IllegalArgumentException("El mensaje debe tener al menos 10 caracteres");
        }
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) return "AYUDA";
        return category.trim().toUpperCase();
    }

    private String resolveAuthor(String name) {
        if (name == null || name.isBlank()) return "Ciudadano";
        return name.trim();
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max - 1) + "…";
    }

    private String formatDate(java.time.LocalDateTime dt) {
        return com.sanos.forumservice.util.ApiDateTimes.format(dt);
    }
}
