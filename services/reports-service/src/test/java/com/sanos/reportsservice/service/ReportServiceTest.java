package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.ReportDto;
import com.sanos.reportsservice.messaging.ReportEventPublisher;
import com.sanos.reportsservice.model.DetalleReporte;
import com.sanos.reportsservice.model.ReporteEvento;
import com.sanos.reportsservice.repository.DetalleReporteRepository;
import com.sanos.reportsservice.repository.ReporteEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReporteEventoRepository reporteRepo;
    @Mock
    private DetalleReporteRepository detalleRepo;
    @Mock
    private ReportEventPublisher eventPublisher;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(reporteRepo, detalleRepo, eventPublisher);
    }

    @Test
    void create_defaultsStatusAndPublishesEvent() {
        ReportDto req = new ReportDto(
                null, 10L, 7L, "LOST", null, "Santiago", "desc", "ok",
                new BigDecimal("-33.4"), new BigDecimal("-70.6"), null
        );

        when(reporteRepo.save(any(ReporteEvento.class))).thenAnswer(invocation -> {
            ReporteEvento r = invocation.getArgument(0);
            r.setIdReporte(100L);
            return r;
        });

        ReportDto created = service.create(req);

        assertEquals(100L, created.id());
        assertEquals("ABIERTO", created.status());
        assertEquals(10L, created.petId());
        verify(detalleRepo).save(any(DetalleReporte.class));
        verify(eventPublisher).publishReportCreated(any(ReporteEvento.class));
    }

    @Test
    void create_keepsProvidedStatus() {
        ReportDto req = new ReportDto(
                null, 11L, 9L, "FOUND", "CERRADO", "Nunoa", "texto", "bien",
                BigDecimal.ONE, BigDecimal.TEN, null
        );

        when(reporteRepo.save(any(ReporteEvento.class))).thenAnswer(invocation -> {
            ReporteEvento r = invocation.getArgument(0);
            r.setIdReporte(101L);
            return r;
        });

        ReportDto created = service.create(req);
        assertEquals("CERRADO", created.status());
    }

    @Test
    void updateStatus_updatesDetailWhenExists() {
        ReporteEvento rep = new ReporteEvento();
        rep.setIdReporte(200L);
        rep.setEstado("ABIERTO");

        DetalleReporte detalle = new DetalleReporte();
        detalle.setIdReporte(200L);
        detalle.setEstadoActual("ABIERTO");

        when(reporteRepo.findById(200L)).thenReturn(Optional.of(rep));
        when(reporteRepo.save(any(ReporteEvento.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleRepo.findByIdReporte(200L)).thenReturn(Optional.of(detalle));

        Optional<ReportDto> result = service.updateStatus(200L, "CERRADO");

        assertTrue(result.isPresent());
        assertEquals("CERRADO", result.get().status());
        assertEquals("CERRADO", detalle.getEstadoActual());
        verify(detalleRepo).save(detalle);
    }

    @Test
    void updateStatus_lostReportBecomesFoundWhenResolved() {
        ReporteEvento rep = new ReporteEvento();
        rep.setIdReporte(201L);
        rep.setEstado("OPEN");
        rep.setTipoReporte("LOST");

        when(reporteRepo.findById(201L)).thenReturn(Optional.of(rep));
        when(reporteRepo.save(any(ReporteEvento.class))).thenAnswer(i -> i.getArgument(0));
        when(detalleRepo.findByIdReporte(201L)).thenReturn(Optional.empty());

        Optional<ReportDto> result = service.updateStatus(201L, "RESOLVED");

        assertTrue(result.isPresent());
        assertEquals("FOUND", result.get().type());
        assertEquals("RESOLVED", result.get().status());
    }

    @Test
    void updateStatus_returnsEmptyWhenReportNotFound() {
        when(reporteRepo.findById(999L)).thenReturn(Optional.empty());

        Optional<ReportDto> result = service.updateStatus(999L, "CERRADO");

        assertTrue(result.isEmpty());
        verify(detalleRepo, never()).save(any());
    }

    @Test
    void delete_removesDetailAndReport() {
        DetalleReporte detalle = new DetalleReporte();
        detalle.setIdReporte(300L);

        when(reporteRepo.existsById(300L)).thenReturn(true);
        when(detalleRepo.findByIdReporte(300L)).thenReturn(Optional.of(detalle));

        assertTrue(service.delete(300L));

        verify(detalleRepo).delete(detalle);
        verify(reporteRepo).deleteById(300L);
    }

    @Test
    void delete_returnsFalseWhenReportNotFound() {
        when(reporteRepo.existsById(404L)).thenReturn(false);

        assertFalse(service.delete(404L));

        verify(detalleRepo, never()).delete(any());
        verify(reporteRepo, never()).deleteById(any());
    }

    @Test
    void listAll_mapsNullCoordinatesToZero() {
        ReporteEvento rep = new ReporteEvento();
        rep.setIdReporte(1L);
        rep.setIdMascota(2L);
        rep.setIdUsuarioCreador(3L);
        rep.setTipoReporte("LOST");
        rep.setEstado("ABIERTO");
        rep.setComuna("Providencia");
        rep.setDescripcion("x");
        rep.setEstadoSalud("ok");
        rep.setLatitud(null);
        rep.setLongitud(null);
        rep.setFechaCreacion(LocalDateTime.of(2026, 5, 1, 12, 0));

        when(reporteRepo.findAll()).thenReturn(List.of(rep));

        List<ReportDto> result = service.listAll();

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).latitude());
        assertEquals(BigDecimal.ZERO, result.get(0).longitude());
    }
}
