package school.sptech.projeto_amar.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import school.sptech.projeto_amar.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
  private UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

}
