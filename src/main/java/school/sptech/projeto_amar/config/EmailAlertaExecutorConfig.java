package school.sptech.projeto_amar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EmailAlertaExecutorConfig {
  @Bean(name = "alertaEmailExecutor", destroyMethod = "shutdown")
  public ThreadPoolTaskExecutor alertaEmailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(100);
    executor.setKeepAliveSeconds(5);
    executor.setAllowCoreThreadTimeOut(true);
    CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("alerta-email-");
    threadFactory.setDaemon(true);
    executor.setThreadFactory(threadFactory);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(10);
    return executor;
  }
}
