package com.sanos.matchingservice.service;

import com.sanos.matchingservice.dto.MatchDto;
import com.sanos.matchingservice.model.CoincidenciaIa;
import com.sanos.matchingservice.repository.CoincidenciaIaRepository;
import com.sanos.matchingservice.repository.DesgloseSimilitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingEngineTest {

    @Mock
    private RestTemplate rest;
    @Mock
    private CoincidenciaIaRepository matchRepo;
    @Mock
    private DesgloseSimilitudRepository breakdownRepo;

    private MatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine(rest, matchRepo, breakdownRepo, "http://reports");
    }

    @Test
    void runFullMatching_createsMatchesWhenScoreIsHigh() {
        List<Map<String, Object>> reports = List.of(
                Map.of("id", 1L, "type", "PERDIDA", "commune", "Santiago", "latitude", -33.45, "longitude", -70.66, "petId", 11L),
                Map.of("id", 2L, "type", "ENCONTRADA", "commune", "Santiago", "latitude", -33.451, "longitude", -70.661, "petId", 11L)
        );
        when(rest.getForObject(anyString(), eq(List.class))).thenReturn(reports);
        when(matchRepo.save(any(CoincidenciaIa.class))).thenAnswer(inv -> {
            CoincidenciaIa c = inv.getArgument(0);
            c.setIdMatch(99L);
            return c;
        });

        List<MatchDto> result = engine.runFullMatching();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).lostReportId());
        assertEquals(2L, result.get(0).foundReportId());
        assertTrue(result.get(0).score() >= 0.3f);
        verify(matchRepo).deleteAll();
        verify(breakdownRepo).deleteAll();
        verify(breakdownRepo, atLeastOnce()).save(any());
    }

    @Test
    void runFullMatching_returnsEmptyWhenFetchFails() {
        when(rest.getForObject(anyString(), eq(List.class))).thenThrow(new RuntimeException("down"));

        List<MatchDto> result = engine.runFullMatching();

        assertTrue(result.isEmpty());
        verifyNoInteractions(matchRepo, breakdownRepo);
    }

    @Test
    void manualCreate_defaultsScoreToZero() {
        MatchDto req = new MatchDto(null, 10L, 20L, null, "manual", null);
        when(matchRepo.save(any(CoincidenciaIa.class))).thenAnswer(inv -> {
            CoincidenciaIa c = inv.getArgument(0);
            c.setIdMatch(55L);
            c.setCreadoEn(LocalDateTime.now());
            return c;
        });

        MatchDto created = engine.manualCreate(req);

        assertEquals(55L, created.id());
        assertEquals(0f, created.score());
    }
}
