package com.sanos.matchingservice.service;

import com.sanos.matchingservice.dto.MatchDto;
import com.sanos.matchingservice.model.CoincidenciaIa;
import com.sanos.matchingservice.model.DesgloseSimilitud;
import com.sanos.matchingservice.repository.CoincidenciaIaRepository;
import com.sanos.matchingservice.repository.DesgloseSimilitudRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MatchingEngine {

    private final RestTemplate rest;
    private final CoincidenciaIaRepository matchRepo;
    private final DesgloseSimilitudRepository breakdownRepo;
    private final String reportsUrl;

    public MatchingEngine(RestTemplate rest,
                          CoincidenciaIaRepository matchRepo,
                          DesgloseSimilitudRepository breakdownRepo,
                          @Value("${services.reportsUrl:http://localhost:8093}") String reportsUrl) {
        this.rest = rest;
        this.matchRepo = matchRepo;
        this.breakdownRepo = breakdownRepo;
        this.reportsUrl = reportsUrl;
    }

    @Transactional
    public List<MatchDto> runFullMatching() {
        List<Map<String, Object>> reports = fetchReports();
        if (reports.isEmpty()) return List.of();

        List<Map<String, Object>> perdidas = filterByType(reports, "PERDIDA");
        List<Map<String, Object>> encontradas = filterByType(reports, "ENCONTRADA");

        matchRepo.deleteAll();
        breakdownRepo.deleteAll();

        List<MatchDto> results = new ArrayList<>();
        for (Map<String, Object> lost : perdidas) {
            for (Map<String, Object> found : encontradas) {
                Score s = score(lost, found);
                if (s.total < 0.3f) continue;

                CoincidenciaIa entity = new CoincidenciaIa();
                entity.setIdReportePerdida(asLong(lost.get("id")));
                entity.setIdReporteEncontrada(asLong(found.get("id")));
                entity.setScoreTotal(s.total);
                entity.setExplicacion(s.explanation);
                entity.setCreadoEn(LocalDateTime.now());
                entity = matchRepo.save(entity);

                for (String criterio : s.criteria) {
                    DesgloseSimilitud d = new DesgloseSimilitud();
                    d.setIdMatch(entity.getIdMatch());
                    d.setCriterio(criterio);
                    breakdownRepo.save(d);
                }

                results.add(toDto(entity));
            }
        }
        return results;
    }

    @Transactional
    public MatchDto manualCreate(MatchDto req) {
        CoincidenciaIa entity = new CoincidenciaIa();
        entity.setIdReportePerdida(req.lostReportId());
        entity.setIdReporteEncontrada(req.foundReportId());
        entity.setScoreTotal(req.score() == null ? 0f : req.score());
        entity.setExplicacion(req.explanation());
        entity.setCreadoEn(LocalDateTime.now());
        entity = matchRepo.save(entity);
        return toDto(entity);
    }

    public List<MatchDto> listAll() {
        return matchRepo.findAll().stream().map(this::toDto).toList();
    }

    public List<MatchDto> byReport(Long reportId) {
        List<MatchDto> byLost = matchRepo.findByIdReportePerdida(reportId).stream().map(this::toDto).toList();
        List<MatchDto> byFound = matchRepo.findByIdReporteEncontrada(reportId).stream().map(this::toDto).toList();
        List<MatchDto> all = new ArrayList<>(byLost);
        all.addAll(byFound);
        return all;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchReports() {
        try {
            List<?> raw = rest.getForObject(reportsUrl + "/api/reports", List.class);
            if (raw == null) return List.of();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object o : raw) {
                if (o instanceof Map<?, ?> m) {
                    result.add((Map<String, Object>) m);
                }
            }
            return result;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<Map<String, Object>> filterByType(List<Map<String, Object>> items, String type) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> m : items) {
            Object t = m.get("type");
            if (t != null && type.equalsIgnoreCase(String.valueOf(t))) {
                out.add(m);
            }
        }
        return out;
    }

    private Score score(Map<String, Object> lost, Map<String, Object> found) {
        List<String> reasons = new ArrayList<>();
        float score = 0f;

        String cLost = String.valueOf(lost.getOrDefault("commune", ""));
        String cFound = String.valueOf(found.getOrDefault("commune", ""));
        if (!cLost.isBlank() && cLost.equalsIgnoreCase(cFound)) {
            score += 0.35f;
            reasons.add("misma-comuna:" + cLost);
        }

        Double latL = asDouble(lost.get("latitude"));
        Double lngL = asDouble(lost.get("longitude"));
        Double latF = asDouble(found.get("latitude"));
        Double lngF = asDouble(found.get("longitude"));
        if (latL != null && lngL != null && latF != null && lngF != null) {
            double km = haversine(latL, lngL, latF, lngF);
            if (km < 1) {
                score += 0.45f;
                reasons.add("distancia<1km");
            } else if (km < 5) {
                score += 0.30f;
                reasons.add("distancia<5km");
            } else if (km < 15) {
                score += 0.15f;
                reasons.add("distancia<15km");
            }
        }

        Long petL = asLong(lost.get("petId"));
        Long petF = asLong(found.get("petId"));
        if (petL != null && petL.equals(petF)) {
            score += 0.25f;
            reasons.add("misma-mascota");
        }

        score = Math.min(1f, score);
        String explanation = reasons.isEmpty()
                ? "Sin coincidencias significativas"
                : "Coincidencias: " + String.join(", ", reasons);
        return new Score(score, explanation, reasons);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception ex) { return null; }
    }

    private Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception ex) { return null; }
    }

    private MatchDto toDto(CoincidenciaIa e) {
        return new MatchDto(
                e.getIdMatch(),
                e.getIdReportePerdida(),
                e.getIdReporteEncontrada(),
                e.getScoreTotal(),
                e.getExplicacion(),
                com.sanos.matchingservice.util.ApiDateTimes.format(e.getCreadoEn())
        );
    }

    private static class Score {
        final float total;
        final String explanation;
        final List<String> criteria;
        Score(float total, String explanation, List<String> criteria) {
            this.total = total;
            this.explanation = explanation;
            this.criteria = criteria;
        }
    }
}
