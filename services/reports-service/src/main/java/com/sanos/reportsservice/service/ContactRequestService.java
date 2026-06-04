package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.ContactRequestDto;
import com.sanos.reportsservice.dto.CreateContactRequest;
import com.sanos.reportsservice.dto.RespondContactRequest;
import com.sanos.reportsservice.model.ReporteEvento;
import com.sanos.reportsservice.model.SolicitudContacto;
import com.sanos.reportsservice.repository.ReporteEventoRepository;
import com.sanos.reportsservice.repository.SolicitudContactoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactRequestService {

    private static final String PENDING = "PENDING";
    private static final String ACCEPTED = "ACCEPTED";
    private static final String REJECTED = "REJECTED";

    private final SolicitudContactoRepository solicitudRepo;
    private final ReporteEventoRepository reporteRepo;
    private final ContactConversationService conversationService;

    public ContactRequestService(SolicitudContactoRepository solicitudRepo,
                                 ReporteEventoRepository reporteRepo,
                                 ContactConversationService conversationService) {
        this.solicitudRepo = solicitudRepo;
        this.reporteRepo = reporteRepo;
        this.conversationService = conversationService;
    }

    public ContactRequestDto create(CreateContactRequest req) {
        if (req.reportId() == null || req.fromUserId() == null) {
            throw new IllegalArgumentException("reportId y fromUserId son obligatorios.");
        }
        String message = req.message() == null ? "" : req.message().trim();
        if (message.length() < 10) {
            throw new IllegalArgumentException("El mensaje debe tener al menos 10 caracteres.");
        }

        ReporteEvento reporte = reporteRepo.findById(req.reportId())
                .orElseThrow(() -> new IllegalStateException("Reporte no encontrado."));

        Long ownerId = reporte.getIdUsuarioCreador();
        if (ownerId == null) {
            throw new IllegalStateException("El reporte no tiene dueno asignado.");
        }
        if (ownerId.equals(req.fromUserId())) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a tu propio reporte.");
        }

        solicitudRepo.findByIdReporteAndIdUsuarioEmisorAndEstado(req.reportId(), req.fromUserId(), PENDING)
                .ifPresent(s -> {
                    throw new IllegalStateException("Ya tienes una solicitud pendiente para este reporte.");
                });

        SolicitudContacto s = new SolicitudContacto();
        s.setIdReporte(req.reportId());
        s.setIdUsuarioEmisor(req.fromUserId());
        s.setIdUsuarioReceptor(ownerId);
        s.setMensaje(message);
        s.setEstado(PENDING);
        s.setFechaCreacion(LocalDateTime.now());
        s = solicitudRepo.save(s);
        return toDto(s);
    }

    public List<ContactRequestDto> inbox(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId requerido.");
        return solicitudRepo.findByIdUsuarioReceptorOrderByFechaCreacionDesc(userId)
                .stream().map(this::toDto).toList();
    }

    public List<ContactRequestDto> sent(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId requerido.");
        return solicitudRepo.findByIdUsuarioEmisorOrderByFechaCreacionDesc(userId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ContactRequestDto respond(Long id, RespondContactRequest req) {
        if (req.responderUserId() == null) {
            throw new IllegalArgumentException("responderUserId requerido.");
        }
        String status = req.status() == null ? "" : req.status().trim().toUpperCase();
        if (!ACCEPTED.equals(status) && !REJECTED.equals(status)) {
            throw new IllegalArgumentException("status debe ser ACCEPTED o REJECTED.");
        }

        SolicitudContacto s = solicitudRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Solicitud no encontrada."));
        if (!PENDING.equals(s.getEstado())) {
            throw new IllegalStateException("La solicitud ya fue respondida.");
        }
        if (!req.responderUserId().equals(s.getIdUsuarioReceptor())) {
            throw new IllegalStateException("Solo el receptor puede aceptar o rechazar.");
        }

        s.setEstado(status);
        s.setFechaRespuesta(LocalDateTime.now());
        s = solicitudRepo.save(s);

        if (ACCEPTED.equals(status)) {
            conversationService.openFromAcceptedRequest(s);
        }

        return toDto(s);
    }

    private ContactRequestDto toDto(SolicitudContacto s) {
        Long convId = conversationService.conversationIdForRequest(s.getIdSolicitud());
        if (convId == null && ACCEPTED.equals(s.getEstado())) {
            convId = conversationService.ensureConversationId(s);
        }
        return new ContactRequestDto(
                s.getIdSolicitud(),
                s.getIdReporte(),
                s.getIdUsuarioEmisor(),
                s.getIdUsuarioReceptor(),
                s.getMensaje(),
                s.getEstado(),
                s.getFechaCreacion() != null ? s.getFechaCreacion().toString() : null,
                s.getFechaRespuesta() != null ? s.getFechaRespuesta().toString() : null,
                convId
        );
    }
}
