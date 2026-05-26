package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.ReportDto;
import com.sanos.reportsservice.messaging.ReportEventPublisher;
import com.sanos.reportsservice.model.DetalleReporte;
import com.sanos.reportsservice.model.ReporteEvento;
import com.sanos.reportsservice.repository.DetalleReporteRepository;
import com.sanos.reportsservice.repository.ReporteEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReporteEventoRepository reporteRepo;
    private final DetalleReporteRepository detalleRepo;
    private final ReportEventPublisher eventPublisher;

    public ReportService(
            ReporteEventoRepository reporteRepo,
            DetalleReporteRepository detalleRepo,
            ReportEventPublisher eventPublisher) {
        this.reporteRepo = reporteRepo;
        this.detalleRepo = detalleRepo;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<ReportDto> listAll() {
        return reporteRepo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ReportDto> findById(Long id) {
        return reporteRepo.findById(id).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<ReportDto> findByPet(Long petId) {
        return reporteRepo.findByIdMascota(petId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDto> findByStatus(String status) {
        return reporteRepo.findByEstado(status).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportDto> findByUser(Long userId) {
        return reporteRepo.findByIdUsuarioCreador(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public ReportDto create(ReportDto req) {
        ReporteEvento rep = new ReporteEvento();
        rep.setIdMascota(req.petId());
        rep.setIdUsuarioCreador(req.createdBy());
        rep.setTipoReporte(req.type());
        rep.setEstado(req.status() == null ? "ABIERTO" : req.status());
        rep.setComuna(req.commune());
        rep.setDescripcion(req.description());
        rep.setEstadoSalud(req.healthStatus());
        rep.setLatitud(req.latitude());
        rep.setLongitud(req.longitude());
        rep.setFechaCreacion(LocalDateTime.now());
        rep = reporteRepo.save(rep);

        DetalleReporte detalle = new DetalleReporte();
        detalle.setIdReporte(rep.getIdReporte());
        detalle.setEstadoActual(rep.getEstado());
        detalle.setCondicionSalud(rep.getEstadoSalud());
        detalleRepo.save(detalle);

        eventPublisher.publishReportCreated(rep);

        return toDto(rep);
    }

    @Transactional
    public Optional<ReportDto> updateStatus(Long id, String newStatus) {
        return reporteRepo.findById(id).map(rep -> {
            rep.setEstado(newStatus);
            ReporteEvento saved = reporteRepo.save(rep);
            detalleRepo.findByIdReporte(id).ifPresent(d -> {
                d.setEstadoActual(newStatus);
                detalleRepo.save(d);
            });
            return toDto(saved);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (id == null || !reporteRepo.existsById(id)) {
            return false;
        }
        detalleRepo.findByIdReporte(id).ifPresent(detalleRepo::delete);
        reporteRepo.deleteById(id);
        return true;
    }

    private ReportDto toDto(ReporteEvento r) {
        if (r == null) {
            return null;
        }
        // Handle missing coordinates gracefully - use 0 as default if null
        var latitude = r.getLatitud() != null ? r.getLatitud() : java.math.BigDecimal.ZERO;
        var longitude = r.getLongitud() != null ? r.getLongitud() : java.math.BigDecimal.ZERO;
        
        return new ReportDto(
                r.getIdReporte(),
                r.getIdMascota(),
                r.getIdUsuarioCreador(),
                r.getTipoReporte(),
                r.getEstado(),
                r.getComuna(),
                r.getDescripcion(),
                r.getEstadoSalud(),
                latitude,
                longitude,
                r.getFechaCreacion() != null ? r.getFechaCreacion().toString() : null
        );
    }
}
