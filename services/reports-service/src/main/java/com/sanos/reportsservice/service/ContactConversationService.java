package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.*;
import com.sanos.reportsservice.model.ConversacionContacto;
import com.sanos.reportsservice.model.MensajeContacto;
import com.sanos.reportsservice.model.SolicitudContacto;
import com.sanos.reportsservice.repository.ConversacionContactoRepository;
import com.sanos.reportsservice.repository.MensajeContactoRepository;
import com.sanos.reportsservice.repository.SolicitudContactoRepository;
import com.sanos.reportsservice.util.ApiDateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactConversationService {

    private final ConversacionContactoRepository conversacionRepo;
    private final MensajeContactoRepository mensajeRepo;
    private final SolicitudContactoRepository solicitudRepo;

    public ContactConversationService(ConversacionContactoRepository conversacionRepo,
                                      MensajeContactoRepository mensajeRepo,
                                      SolicitudContactoRepository solicitudRepo) {
        this.conversacionRepo = conversacionRepo;
        this.mensajeRepo = mensajeRepo;
        this.solicitudRepo = solicitudRepo;
    }

    @Transactional
    public ConversacionContacto openFromAcceptedRequest(SolicitudContacto solicitud) {
        return conversacionRepo.findByIdSolicitud(solicitud.getIdSolicitud())
                .orElseGet(() -> {
                    ConversacionContacto c = new ConversacionContacto();
                    c.setIdSolicitud(solicitud.getIdSolicitud());
                    c.setIdReporte(solicitud.getIdReporte());
                    c.setIdUsuarioEmisor(solicitud.getIdUsuarioEmisor());
                    c.setIdUsuarioReceptor(solicitud.getIdUsuarioReceptor());
                    c.setEstado(ConversacionContacto.OPEN);
                    c.setFechaCreacion(LocalDateTime.now());
                    c = conversacionRepo.save(c);

                    String intro = solicitud.getMensaje();
                    if (intro != null && !intro.isBlank()) {
                        MensajeContacto m = new MensajeContacto();
                        m.setIdConversacion(c.getIdConversacion());
                        m.setIdUsuarioAutor(solicitud.getIdUsuarioEmisor());
                        m.setContenido("[Solicitud] " + intro.trim());
                        m.setFechaCreacion(LocalDateTime.now());
                        mensajeRepo.save(m);
                    }

                    MensajeContacto sys = new MensajeContacto();
                    sys.setIdConversacion(c.getIdConversacion());
                    sys.setIdUsuarioAutor(solicitud.getIdUsuarioReceptor());
                    sys.setContenido("Solicitud aceptada. Pueden coordinarse por este chat.");
                    sys.setFechaCreacion(LocalDateTime.now().plusSeconds(1));
                    mensajeRepo.save(sys);

                    return c;
                });
    }

    public Long conversationIdForRequest(Long requestId) {
        return conversacionRepo.findByIdSolicitud(requestId)
                .map(ConversacionContacto::getIdConversacion)
                .orElse(null);
    }

    public List<ContactConversationDto> listForUser(Long userId, String statusFilter) {
        if (userId == null) throw new IllegalArgumentException("userId requerido.");
        String estado = null;
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            estado = statusFilter.trim().toUpperCase();
        }
        return conversacionRepo.findForParticipant(userId, estado)
                .stream().map(this::toConversationDto).toList();
    }

    @Transactional
    public Long ensureConversationId(SolicitudContacto solicitud) {
        if (solicitud == null || !"ACCEPTED".equals(solicitud.getEstado())) {
            return null;
        }
        return conversacionRepo.findByIdSolicitud(solicitud.getIdSolicitud())
                .map(ConversacionContacto::getIdConversacion)
                .orElseGet(() -> openFromAcceptedRequest(solicitud).getIdConversacion());
    }

    public ContactConversationDto getByRequestId(Long requestId, Long userId) {
        SolicitudContacto solicitud = solicitudRepo.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Solicitud no encontrada."));
        if (!userId.equals(solicitud.getIdUsuarioEmisor()) && !userId.equals(solicitud.getIdUsuarioReceptor())) {
            throw new IllegalStateException("No participas en esta solicitud.");
        }
        if (!"ACCEPTED".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud aun no fue aceptada.");
        }
        Long convId = ensureConversationId(solicitud);
        return getConversation(convId, userId);
    }

    public ContactConversationDto getConversation(Long id, Long userId) {
        ConversacionContacto c = findForParticipant(id, userId);
        return toConversationDto(c);
    }

    public List<ContactMessageDto> listMessages(Long conversationId, Long userId) {
        findForParticipant(conversationId, userId);
        return mensajeRepo.findByIdConversacionOrderByFechaCreacionAsc(conversationId)
                .stream().map(this::toMessageDto).toList();
    }

    @Transactional
    public ContactMessageDto postMessage(Long conversationId, CreateContactMessageRequest req) {
        ConversacionContacto c = findForParticipant(conversationId, req.authorUserId());
        if (ConversacionContacto.CLOSED.equals(c.getEstado())) {
            throw new IllegalStateException("La conversacion esta cerrada (historial).");
        }
        String content = req.content() == null ? "" : req.content().trim();
        if (content.length() < 1) {
            throw new IllegalArgumentException("El mensaje no puede estar vacio.");
        }
        MensajeContacto m = new MensajeContacto();
        m.setIdConversacion(conversationId);
        m.setIdUsuarioAutor(req.authorUserId());
        m.setContenido(content);
        m.setFechaCreacion(LocalDateTime.now());
        m = mensajeRepo.save(m);
        return toMessageDto(m);
    }

    @Transactional
    public ContactConversationDto close(Long conversationId, CloseContactConversationRequest req) {
        ConversacionContacto c = conversacionRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Conversacion no encontrada."));
        if (!req.userId().equals(c.getIdUsuarioReceptor())) {
            throw new IllegalStateException("Solo quien recibio la solicitud puede cerrar el chat.");
        }
        if (ConversacionContacto.CLOSED.equals(c.getEstado())) {
            return toConversationDto(c);
        }
        c.setEstado(ConversacionContacto.CLOSED);
        c.setFechaCierre(LocalDateTime.now());
        c.setIdUsuarioCerro(req.userId());
        c = conversacionRepo.save(c);

        MensajeContacto m = new MensajeContacto();
        m.setIdConversacion(conversationId);
        m.setIdUsuarioAutor(req.userId());
        m.setContenido("Chat cerrado por el dueno del reporte. Queda en historial.");
        m.setFechaCreacion(LocalDateTime.now());
        mensajeRepo.save(m);

        return toConversationDto(c);
    }

    private ConversacionContacto findForParticipant(Long conversationId, Long userId) {
        ConversacionContacto c = conversacionRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Conversacion no encontrada."));
        if (!userId.equals(c.getIdUsuarioEmisor()) && !userId.equals(c.getIdUsuarioReceptor())) {
            throw new IllegalStateException("No participas en esta conversacion.");
        }
        return c;
    }

    private ContactConversationDto toConversationDto(ConversacionContacto c) {
        List<MensajeContacto> msgs = mensajeRepo.findByIdConversacionOrderByFechaCreacionAsc(c.getIdConversacion());
        String preview = "";
        if (!msgs.isEmpty()) {
            MensajeContacto last = msgs.get(msgs.size() - 1);
            preview = last.getContenido();
            if (preview.length() > 80) preview = preview.substring(0, 77) + "...";
        }
        return new ContactConversationDto(
                c.getIdConversacion(),
                c.getIdSolicitud(),
                c.getIdReporte(),
                c.getIdUsuarioEmisor(),
                c.getIdUsuarioReceptor(),
                c.getEstado(),
                ApiDateTimes.format(c.getFechaCreacion()),
                ApiDateTimes.format(c.getFechaCierre()),
                c.getIdUsuarioCerro(),
                preview,
                msgs.size()
        );
    }

    private ContactMessageDto toMessageDto(MensajeContacto m) {
        return new ContactMessageDto(
                m.getIdMensaje(),
                m.getIdConversacion(),
                m.getIdUsuarioAutor(),
                m.getContenido(),
                ApiDateTimes.format(m.getFechaCreacion())
        );
    }
}
