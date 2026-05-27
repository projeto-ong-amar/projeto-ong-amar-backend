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
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.exception.categoriaExceptions.CategoriaNaoEncontradaException;
import school.sptech.projeto_amar.exception.produtoExceptions.ExisteProdutoByNomeException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoExceptions.SemProdutosCadastradosException;
import school.sptech.projeto_amar.repository.CategoriaRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @InjectMocks
    private ProdutoService service;

    @Mock
    private ProdutoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private Produto produto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNome("Alimentos");
        categoria.setStatus("ATIVO");

        produto = new Produto();
        produto.setId(1);
        produto.setNome("Arroz");
        produto.setUnidadeMedida("kg");
        produto.setStatus("ATIVO");
        produto.setCategoria(categoria);
    }

    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar produto com dados válidos")
        void deveCadastrarComSucesso() {
            when(repository.existsByNomeAndStatus("Arroz", "ATIVO")).thenReturn(false);
            when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
            when(repository.save(any(Produto.class))).thenReturn(produto);

            Produto resultado = service.cadastrar(produto);

            assertNotNull(resultado);
            assertEquals("ATIVO", resultado.getStatus());
            verify(repository).save(any(Produto.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome já existe com status ATIVO")
        void deveLancarExcecaoNomeDuplicado() {
            when(repository.existsByNomeAndStatus("Arroz", "ATIVO")).thenReturn(true);

            assertThrows(ExisteProdutoByNomeException.class, () -> service.cadastrar(produto));
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria não encontrada")
        void deveLancarExcecaoCategoriaNaoEncontrada() {
            when(repository.existsByNomeAndStatus("Arroz", "ATIVO")).thenReturn(false);
            when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(CategoriaNaoEncontradaException.class, () -> service.cadastrar(produto));
        }
    }

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("Deve retornar produtos ativos")
        void deveRetornarProdutos() {
            when(repository.findAllByStatus("ATIVO")).thenReturn(List.of(produto));

            List<Produto> resultado = service.listar();

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nenhum produto ativo")
        void deveLancarExcecaoSemProdutos() {
            when(repository.findAllByStatus("ATIVO")).thenReturn(Collections.emptyList());

            assertThrows(SemProdutosCadastradosException.class, () -> service.listar());
        }
    }

    @Nested
    @DisplayName("listarPorCategoria()")
    class ListarPorCategoria {

        @Test
        @DisplayName("Deve retornar produtos por categoria existente")
        void deveRetornarProdutosPorCategoria() {
            when(categoriaRepository.existsByNome("Alimentos")).thenReturn(true);
            when(repository.findAllByCategoriaNomeAndStatus("Alimentos", "ATIVO"))
                    .thenReturn(List.of(produto));

            List<Produto> resultado = service.listarPorCategoria("Alimentos");

            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria inexistente")
        void deveLancarExcecaoCategoriaInexistente() {
            when(categoriaRepository.existsByNome("Inexistente")).thenReturn(false);

            assertThrows(CategoriaNaoEncontradaException.class,
                    () -> service.listarPorCategoria("Inexistente"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria sem produtos ativos")
        void deveLancarExcecaoSemProdutosNaCategoria() {
            when(categoriaRepository.existsByNome("Alimentos")).thenReturn(true);
            when(repository.findAllByCategoriaNomeAndStatus("Alimentos", "ATIVO"))
                    .thenReturn(Collections.emptyList());

            assertThrows(SemProdutosCadastradosException.class,
                    () -> service.listarPorCategoria("Alimentos"));
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar produto ATIVO existente")
        void deveAtualizarComSucesso() {
            when(repository.findById(1)).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
            when(repository.save(any(Produto.class))).thenReturn(produto);

            Produto novosDados = new Produto();
            novosDados.setNome("Feijão");
            novosDados.setUnidadeMedida("kg");
            novosDados.setCategoria(categoria);

            Produto resultado = service.atualizar(1, novosDados);

            assertNotNull(resultado);
            verify(repository).save(any(Produto.class));
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            Produto novosDados = new Produto();
            novosDados.setCategoria(categoria);

            assertThrows(ProdutoNaoEncontradoException.class,
                    () -> service.atualizar(99, novosDados));
        }

        @Test
        @DisplayName("Deve lançar exceção para produto INATIVO")
        void deveLancarExcecaoProdutoInativo() {
            produto.setStatus("INATIVO");
            when(repository.findById(1)).thenReturn(Optional.of(produto));

            Produto novosDados = new Produto();
            novosDados.setCategoria(categoria);

            assertThrows(ProdutoNaoEncontradoException.class,
                    () -> service.atualizar(1, novosDados));
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria inexistente")
        void deveLancarExcecaoCategoriaInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

            Produto novosDados = new Produto();
            novosDados.setCategoria(categoria);

            assertThrows(CategoriaNaoEncontradaException.class,
                    () -> service.atualizar(1, novosDados));
        }
    }

    @Nested
    @DisplayName("removerPorId()")
    class RemoverPorId {

        @Test
        @DisplayName("Deve inativar produto ATIVO")
        void deveInativarComSucesso() {
            when(repository.findById(1)).thenReturn(Optional.of(produto));
            when(repository.save(any(Produto.class))).thenReturn(produto);

            service.removerPorId(1);

            verify(repository).save(argThat(p -> "INATIVO".equals(p.getStatus())));
        }

        @Test
        @DisplayName("Deve lançar exceção para ID inexistente")
        void deveLancarExcecaoIdInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(ProdutoNaoEncontradoException.class, () -> service.removerPorId(99));
        }

        @Test
        @DisplayName("Deve lançar exceção para produto INATIVO")
        void deveLancarExcecaoProdutoInativo() {
            produto.setStatus("INATIVO");
            when(repository.findById(1)).thenReturn(Optional.of(produto));

            assertThrows(ProdutoNaoEncontradoException.class, () -> service.removerPorId(1));
        }
    }
}

