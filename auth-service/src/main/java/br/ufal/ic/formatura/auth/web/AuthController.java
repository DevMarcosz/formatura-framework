package br.ufal.ic.formatura.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticação simples (registro, login, verificação). Para fins didáticos o
 * "token" é um Base64 assinado com um segredo — substituível por JWT real.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SEGREDO = "formatura-ufal-2026";
    private final Map<String, String> usuarios = new ConcurrentHashMap<>(); // email -> senha

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "auth-service");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Credenciais c) {
        if (usuarios.containsKey(c.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "E-mail já cadastrado"));
        }
        if (c.senha() == null || c.senha().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Senha deve ter ao menos 6 caracteres"));
        }
        usuarios.put(c.email(), c.senha());
        return ResponseEntity.ok(Map.of("email", c.email(), "token", gerarToken(c.email())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credenciais c) {
        if (!c.senha().equals(usuarios.get(c.email()))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas"));
        }
        return ResponseEntity.ok(Map.of("email", c.email(), "token", gerarToken(c.email())));
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Map<String, String> body) {
        String token = body.getOrDefault("token", "");
        boolean valido = token.endsWith(assinatura(extrairEmail(token)));
        return Map.of("valid", valido, "email", extrairEmail(token));
    }

    private String gerarToken(String email) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(email.getBytes()) + "." + assinatura(email);
    }

    private String extrairEmail(String token) {
        try {
            String parte = token.split("\\.")[0];
            return new String(Base64.getUrlDecoder().decode(parte));
        } catch (Exception e) {
            return "";
        }
    }

    private String assinatura(String email) {
        return Integer.toHexString((email + SEGREDO).hashCode());
    }

    public record Credenciais(String email, String senha) {}
}
