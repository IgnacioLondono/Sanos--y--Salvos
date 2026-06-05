import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BffControllerTest {

    @Test
    void testBasico() {
        assertEquals(2, 1 + 1);
    }

    @Test
    void testBasicoString() {
        assertEquals("bff", "bff");
    }

    @Test
    void testBasicoBoolean() {
        assertTrue(true);
    }

    @Test
    void testBasicoNotNull() {
        assertNotNull("Sanos y Salvos");
    }

    @Test
    void testBasicoLista() {
        assertEquals(3, java.util.List.of(1, 2, 3).size());
    }

    @Test
    void testBasicoMapa() {
        java.util.Map<String, String> mapa = java.util.Map.of("status", "UP");
        assertEquals("UP", mapa.get("status"));
    }
}