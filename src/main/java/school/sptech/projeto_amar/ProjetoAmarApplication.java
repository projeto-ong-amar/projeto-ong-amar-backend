package school.sptech.projeto_amar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjetoAmarApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoAmarApplication.class, args);
	}

}
