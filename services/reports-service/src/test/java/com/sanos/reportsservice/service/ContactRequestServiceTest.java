package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.ContactRequestDto;
import com.sanos.reportsservice.dto.CreateContactRequest;
import com.sanos.reportsservice.dto.RespondContactRequest;
import com.sanos.reportsservice.model.ReporteEvento;
import com.sanos.reportsservice.model.SolicitudContacto;
import com.sanos.reportsservice.repository.ReporteEventoRepository;
import com.sanos.reportsservice.repository.SolicitudContactoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactRequestServiceTest {

    @Mock
    private SolicitudContactoRepository solicitudRepo;
    @Mock
    private ReporteEventoRepository reporteRepo;
    @Mock
    private ContactConversationService conversationService;

    private ContactRequestService service;

    @BeforeEach
    void setUp() {
        service = new ContactRequestService(solicitudRepo, reporteRepo, conversationService);
    }

    @Test
    void create_persistsPendingRequest() {
        CreateContactRequest req = new CreateContactRequest(5L, 2L, "Hola, vi tu reporte en el mapa.");
        ReporteEvento reporte = new ReporteEvento();
        reporte.setIdReporte(5L);
        reporte.setIdUsuarioCreador(9L);

        when(reporteRepo.findById(5L)).thenReturn(Optional.of(reporte));
        when(solicitudRepo.findByIdReporteAndIdUsuarioEmisorAndEstado(5L, 2L, "PENDING"))
                .thenReturn(Optional.empty());
        when(solicitudRepo.save(any(SolicitudContacto.class))).thenAnswer(inv -> {
            SolicitudContacto s = inv.getArgument(0);
            s.setIdSolicitud(11L);
            return s;
        });
        when(conversationService.conversationIdForRequest(11L)).thenReturn(null);

        ContactRequestDto dto = service.create(req);

        assertEquals(11L, dto.id());
        assertEquals("PENDING", dto.status());
        assertEquals(9L, dto.toUserId());
        verify(solicitudRepo).save(any(SolicitudContacto.class));
    }

    @Test
    void create_rejectsShortMessage() {
        CreateContactRequest req = new CreateContactRequest(5L, 2L, "corto");
        assertThrows(IllegalArgumentException.class, () -> service.create(req));
    }

    @Test
    void respond_acceptedOpensConversation() {
        SolicitudContacto s = new SolicitudContacto();
        s.setIdSolicitud(3L);
        s.setIdReporte(5L);
        s.setIdUsuarioEmisor(2L);
        s.setIdUsuarioReceptor(9L);
        s.setEstado("PENDING");

        when(solicitudRepo.findById(3L)).thenReturn(Optional.of(s));
        when(solicitudRepo.save(any(SolicitudContacto.class))).thenAnswer(i -> i.getArgument(0));
        when(conversationService.conversationIdForRequest(3L)).thenReturn(99L);

        RespondContactRequest body = new RespondContactRequest(9L, "ACCEPTED");
        ContactRequestDto dto = service.respond(3L, body);

        assertEquals("ACCEPTED", dto.status());
        verify(conversationService).openFromAcceptedRequest(s);
    }

    @Test
    void inbox_returnsMappedList() {
        SolicitudContacto s = new SolicitudContacto();
        s.setIdSolicitud(1L);
        s.setIdReporte(2L);
        s.setIdUsuarioEmisor(3L);
        s.setIdUsuarioReceptor(9L);
        s.setMensaje("mensaje largo de prueba");
        s.setEstado("PENDING");

        when(solicitudRepo.findByIdUsuarioReceptorOrderByFechaCreacionDesc(9L)).thenReturn(List.of(s));
        when(conversationService.conversationIdForRequest(1L)).thenReturn(null);

        List<ContactRequestDto> list = service.inbox(9L);

        assertEquals(1, list.size());
        assertEquals(3L, list.get(0).fromUserId());
    }
}
