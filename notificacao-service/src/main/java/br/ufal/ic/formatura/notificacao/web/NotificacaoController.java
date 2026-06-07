package br.ufal.ic.formatura.notificacao.web;

import br.ufal.ic.formatura.core.domain.Formando;
import br.ufal.ic.formatura.core.hotspot.notificacao.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Expõe o hot spot {@link CanalNotificacao}. O canal de envio é escolhido pelo
 * cliente (e-mail, SMS, WhatsApp), todos seguindo o mesmo Template Method.
 */
@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "notificacao-service");
    }

    @GetMapping("/canais")
    public List<String> canais() {
        return List.of("EMAIL", "SMS", "WHATSAPP");
    }

    @PostMapping("/enviar")
    public Map<String, Object> enviar(@RequestBody EnvioRequest req) {
        CanalNotificacao canal = canalPorNome(req.canal());
        int enviados = 0;
        for (FormandoDTO dto : req.formandos()) {
            Formando f = new Formando(dto.id(), dto.nome(), "curso", dto.email(), dto.telefone());
            if (canal.notificar(f, req.mensagem())) {
                enviados++;
            }
        }
        return Map.of("canal", canal.nome(), "enviados", enviados,
                "total", req.formandos().size());
    }

    private CanalNotificacao canalPorNome(String nome) {
        return switch (nome == null ? "" : nome.toUpperCase()) {
            case "SMS" -> new CanalSMS();
            case "WHATSAPP" -> new CanalWhatsApp();
            default -> new CanalEmail();
        };
    }

    public record FormandoDTO(String id, String nome, String email, String telefone) {}

    public record EnvioRequest(String canal, String mensagem, List<FormandoDTO> formandos) {}
}
