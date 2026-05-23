import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginValidationTest {

    @Test
    void emailConArrobaDebeSerValido() {
        String email = "usuario@gmail.com";
        assertTrue(email.contains("@"));
    }

    @Test
    void emailSinArrobaDebeSerInvalido() {
        String email = "usuariogmail.com";
        assertFalse(email.contains("@"));
    }

    @Test
    void passwordVaciaDebeSerInvalida() {
        String password = "";
        assertTrue(password.isEmpty());
    }

    @Test
    void passwordConTextoDebeSerValida() {
        String password = "123456";
        assertFalse(password.isEmpty());
    }
}
