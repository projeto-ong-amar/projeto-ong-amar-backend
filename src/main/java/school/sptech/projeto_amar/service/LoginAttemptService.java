package school.sptech.projeto_amar.service;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ENTRIES = 10_000;
    private static final int FALHAS_PARA_BLOQUEIO = 5;
    private static final Duration BLOQUEIO_MAXIMO = Duration.ofMinutes(15);
    private final Map<String, Tentativa> tentativas = new ConcurrentHashMap<>();

    public void verificarPermitido(String ip, String email) {
        removerExpiradas();
        if (bloqueado(chaveIp(ip)) || bloqueado(chaveEmail(email))) {
            throw new LockedException("Credenciais inválidas");
        }
    }

    public void registrarFalha(String ip, String email) {
        registrarFalha(chaveIp(ip));
        registrarFalha(chaveEmail(email));
    }

    public void limpar(String ip, String email) {
        tentativas.remove(chaveIp(ip));
        tentativas.remove(chaveEmail(email));
    }

    private void registrarFalha(String chave) {
        tentativas.compute(chave, (key, tentativa) -> {
            Tentativa atual = tentativa == null ? new Tentativa() : tentativa;
            atual.falhas++;
            if (atual.falhas >= FALHAS_PARA_BLOQUEIO) {
                long segundos = Math.min(30L << Math.min(atual.falhas - FALHAS_PARA_BLOQUEIO, 8),
                        BLOQUEIO_MAXIMO.toSeconds());
                atual.bloqueadoAte = Instant.now().plusSeconds(segundos);
            }
            return atual;
        });
    }

    private boolean bloqueado(String chave) {
        Tentativa tentativa = tentativas.get(chave);
        return tentativa != null && tentativa.bloqueadoAte != null && tentativa.bloqueadoAte.isAfter(Instant.now());
    }

    private void removerExpiradas() {
        if (tentativas.size() < MAX_ENTRIES) {
            return;
        }
        Instant agora = Instant.now();
        tentativas.entrySet().removeIf(entry -> entry.getValue().bloqueadoAte == null
                || !entry.getValue().bloqueadoAte.isAfter(agora));
    }

    private String chaveIp(String ip) {
        return "ip:" + ip;
    }

    private String chaveEmail(String email) {
        return "email:" + email.trim().toLowerCase(Locale.ROOT);
    }

    private static class Tentativa {
        private int falhas;
        private Instant bloqueadoAte;
    }
}
