package school.sptech.projeto_amar.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleGenAiClientConfig {

    @Bean
    public Client googleGenAiClient(GoogleGenAiConnectionProperties properties,
                                    @Value("${app.ai.timeout-ms:60000}") int timeoutMs) {
        HttpOptions httpOptions = HttpOptions.builder()
                .timeout(timeoutMs)
                .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                .build();

        return Client.builder()
                .apiKey(properties.getApiKey())
                .httpOptions(httpOptions)
                .build();
    }
}
