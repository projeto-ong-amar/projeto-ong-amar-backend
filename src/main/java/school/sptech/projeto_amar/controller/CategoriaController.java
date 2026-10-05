package school.sptech.projeto_amar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.sptech.projeto_amar.domain.enums.Categoria;

import java.util.List;

@RestController
@RequestMapping("/categoria")
@Tag(name = "Categorias", description = "Categorias fixas de produtos")
public class CategoriaController {
    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna os valores permitidos no cadastro de produtos")
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(List.of(Categoria.values()));
    }
}
