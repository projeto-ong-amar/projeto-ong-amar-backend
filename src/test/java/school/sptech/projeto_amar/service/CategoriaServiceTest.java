package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.domain.Categoria;
import school.sptech.projeto_amar.dto.categoria.CategoriaRequestDTO;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaJaExisteException;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.categoriaExceptions.SemCategoriasCadastradasException;
import school.sptech.projeto_amar.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @InjectMocks
    private CategoriaService service;

    @Mock
    private CategoriaRepository repository;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Alimentos");
        categoria.setDescricao("Produtos alimentícios");
        categoria.setStatus("ATIVO");
    }

    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar categoria com dados válidos")
        void deveCadastrarComSucesso() {
            when(repository.existsByNome("Alimentos")).thenReturn(false);
            when(repository.save(any(Categoria.class))).thenReturn(categoria);

            Categoria resultado = service.cadastrar(categoria);

            assertNotNull(resultado);
            assertEquals("ATIVO", resultado.getStatus());
            verify(repository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome já existe")
        void deveLancarExcecaoNomeDuplicado() {
            when(repository.existsByNome("Alimentos")).thenReturn(true);

            assertThrows(CategoriaJaExisteException.class, () -> service.cadastrar(categoria));
        }
    }

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("Deve retornar categorias ativas")
        void deveRetornarCategoriasAtivas() {
            when(repository.findAll()).thenReturn(List.of(categoria));

            List<Categoria> resultado = service.listar();

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nenhuma categoria ativa")
        void deveLancarExcecaoSemCategorias() {
            Categoria inativa = new Categoria();
            inativa.setStatus("INATIVO");
            when(repository.findAll()).thenReturn(List.of(inativa));

            assertThrows(SemCategoriasCadastradasException.class, () -> service.listar());
        }

        @Test
        @DisplayName("Deve lançar exceção quando lista vazia")
        void deveLancarExcecaoListaVazia() {
            when(repository.findAll()).thenReturn(List.of());

            assertThrows(SemCategoriasCadastradasException.class, () -> service.listar());
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar categoria ativa existente")
        void deveAtualizarComSucesso() {
            CategoriaRequestDTO dto = new CategoriaRequestDTO("Novo Nome", "Nova Desc");
            when(repository.findById(1)).thenReturn(Optional.of(categoria));
            when(repository.save(any(Categoria.class))).thenReturn(categoria);

            Categoria resultado = service.atualizar(1, dto);

            assertNotNull(resultado);
            verify(repository).save(any(Categoria.class));
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            CategoriaRequestDTO dto = new CategoriaRequestDTO("Nome", "Desc");
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(CategoriaNaoEncontradaException.class, () -> service.atualizar(99, dto));
        }

        @Test
        @DisplayName("Deve lançar exceção para categoria INATIVA")
        void deveLancarExcecaoCategoriaInativa() {
            categoria.setStatus("INATIVO");
            CategoriaRequestDTO dto = new CategoriaRequestDTO("Nome", "Desc");
            when(repository.findById(1)).thenReturn(Optional.of(categoria));

            assertThrows(CategoriaNaoEncontradaException.class, () -> service.atualizar(1, dto));
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {

        @Test
        @DisplayName("Deve inativar categoria ativa")
        void deveInativarComSucesso() {
            when(repository.findById(1)).thenReturn(Optional.of(categoria));
            when(repository.save(any(Categoria.class))).thenReturn(categoria);

            service.remover(1);

            verify(repository).save(argThat(c -> "INATIVO".equals(c.getStatus())));
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(CategoriaNaoEncontradaException.class, () -> service.remover(99));
        }

        @Test
        @DisplayName("Deve lançar exceção para categoria INATIVA")
        void deveLancarExcecaoCategoriaInativa() {
            categoria.setStatus("INATIVO");
            when(repository.findById(1)).thenReturn(Optional.of(categoria));

            assertThrows(CategoriaNaoEncontradaException.class, () -> service.remover(1));
        }
    }
}

