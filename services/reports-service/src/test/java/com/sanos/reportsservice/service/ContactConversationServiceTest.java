package com.sanos.reportsservice.service;

import com.sanos.reportsservice.dto.ContactMessageDto;
import com.sanos.reportsservice.dto.CreateContactMessageRequest;
import com.sanos.reportsservice.model.ConversacionContacto;
import com.sanos.reportsservice.model.MensajeContacto;
import com.sanos.reportsservice.repository.ConversacionContactoRepository;
import com.sanos.reportsservice.repository.MensajeContactoRepository;
import com.sanos.reportsservice.repository.SolicitudContactoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactConversationServiceTest {

    @Mock
    private ConversacionContactoRepository conversacionRepo;
    @Mock
    private MensajeContactoRepository mensajeRepo;
    @Mock
    private SolicitudContactoRepository solicitudRepo;

    private ContactConversationService service;

    @BeforeEach
    void setUp() {
        service = new ContactConversationService(conversacionRepo, mensajeRepo, solicitudRepo);
    }

    @Test
    void postMessage_rejectsWhenConversationClosed() {
        ConversacionContacto c = new ConversacionContacto();
        c.setIdConversacion(7L);
        c.setIdUsuarioEmisor(2L);
        c.setIdUsuarioReceptor(9L);
        c.setEstado(ConversacionContacto.CLOSED);

        when(conversacionRepo.findById(7L)).thenReturn(Optional.of(c));

        CreateContactMessageRequest req = new CreateContactMessageRequest(2L, "Hola");

        assertThrows(IllegalStateException.class, () -> service.postMessage(7L, req));
        verify(mensajeRepo, never()).save(any());
    }

    @Test
    void postMessage_savesWhenOpen() {
        ConversacionContacto c = new ConversacionContacto();
        c.setIdConversacion(7L);
        c.setIdUsuarioEmisor(2L);
        c.setIdUsuarioReceptor(9L);
        c.setEstado(ConversacionContacto.OPEN);

        when(conversacionRepo.findById(7L)).thenReturn(Optional.of(c));
        when(mensajeRepo.save(any(MensajeContacto.class))).thenAnswer(inv -> {
            MensajeContacto m = inv.getArgument(0);
            m.setIdMensaje(50L);
            return m;
        });

        ContactMessageDto dto = service.postMessage(7L, new CreateContactMessageRequest(2L, "Coordina aqui"));

        assertEquals("Coordina aqui", dto.content());
        assertEquals(2L, dto.authorUserId());
    }
}
