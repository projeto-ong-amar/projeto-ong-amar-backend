package school.sptech.projeto_amar.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.sptech.projeto_amar.service.RelatorioMcpToolsService;

@Configuration
public class RelatorioToolCallbackConfig {

  @Bean
  public ToolCallbackProvider relatorioToolCallbackProvider(RelatorioMcpToolsService tools) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(tools)
        .build();
  }
}
