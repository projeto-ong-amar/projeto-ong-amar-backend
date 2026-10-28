package school.sptech.projeto_amar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.sptech.projeto_amar.domain.*;
import school.sptech.projeto_amar.domain.enums.TipoMovimentacao;
import school.sptech.projeto_amar.dto.movimentacao.entrada.EntradaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.perda.PerdaRequestDTO;
import school.sptech.projeto_amar.dto.movimentacao.saida.SaidaRequestDTO;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.EstoqueInsuficienteException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.MovimentacaoNaoEncontradaException;
import school.sptech.projeto_amar.exception.movimentacaoExceptions.SemMovimentacoesException;
import school.sptech.projeto_amar.exception.produtoExceptions.ProdutoNaoEncontradoException;
import school.sptech.projeto_amar.exception.produtoLoteExceptions.LoteNaoEncontradoException;
import school.sptech.projeto_amar.exception.usuarioExceptions.UsuarioNaoEncontradoException;
import school.sptech.projeto_amar.repository.*;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @InjectMocks
    private MovimentacaoService service;

    @Mock
    private MovimentacaoRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProdutoLoteRepository produtoLoteRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Produto produto;
    private Usuario usuario;
    private Movimentacao movimentacao;
    private ProdutoLote lote;
    private MovimentacaoEstoque movEstoque;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1);
        produto.setNome("Arroz");
        produto.setStatus("ATIVO");

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("João");

        lote = new ProdutoLote();
        lote.setId(1);
        lote.setProduto(produto);
        lote.setQuantidade(100);
        lote.setDataEntrada(LocalDate.now());
        lote.setValidade(LocalDate.now().plusMonths(6));

        movimentacao = new Movimentacao();
        movimentacao.setId(1);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(50);
        movimentacao.setProduto(produto);
        movimentacao.setUsuario(usuario);
        movimentacao.setData(LocalDate.now());
        movimentacao.setMotivo("Doação");

        movEstoque = new MovimentacaoEstoque();
        movEstoque.setId(1);
        movEstoque.setMovimentacao(movimentacao);
        movEstoque.setProdutoLote(lote);
        movEstoque.setQuantidade(50);
    }

    // ==================== LISTAGEM ====================

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("Deve retornar lista de movimentações")
        void deveRetornarLista() {
            when(repository.findAll()).thenReturn(List.of(movimentacao));
            List<Movimentacao> resultado = service.listar();
            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando lista vazia")
        void deveLancarExcecaoListaVazia() {
            when(repository.findAll()).thenReturn(Collections.emptyList());
            assertThrows(SemMovimentacoesException.class, () -> service.listar());
        }
    }

    @Nested
    @DisplayName("listarPorTipoEProdutoId()")
    class ListarPorTipoEProdutoId {

        @Test
        @DisplayName("Deve retornar lista filtrada")
        void deveRetornarListaFiltrada() {
            when(repository.findByTipoAndProdutoId(TipoMovimentacao.ENTRADA, 1))
                    .thenReturn(List.of(movimentacao));
            List<Movimentacao> resultado = service.listarPorTipoEProdutoId(TipoMovimentacao.ENTRADA, 1);
            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sem resultados")
        void deveLancarExcecaoSemResultados() {
            when(repository.findByTipoAndProdutoId(TipoMovimentacao.ENTRADA, 1))
                    .thenReturn(Collections.emptyList());
            assertThrows(SemMovimentacoesException.class,
                    () -> service.listarPorTipoEProdutoId(TipoMovimentacao.ENTRADA, 1));
        }
    }

    @Nested
    @DisplayName("listarPorProdutoId()")
    class ListarPorProdutoId {

        @Test
        @DisplayName("Deve retornar lista filtrada por produto")
        void deveRetornarListaFiltrada() {
            when(repository.findByProdutoId(1)).thenReturn(List.of(movimentacao));
            List<Movimentacao> resultado = service.listarPorProdutoId(1);
            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sem resultados")
        void deveLancarExcecaoSemResultados() {
            when(repository.findByProdutoId(1)).thenReturn(Collections.emptyList());
            assertThrows(SemMovimentacoesException.class, () -> service.listarPorProdutoId(1));
        }
    }

    @Nested
    @DisplayName("listarPorTipo()")
    class ListarPorTipo {

        @Test
        @DisplayName("Deve retornar lista filtrada por tipo")
        void deveRetornarListaFiltrada() {
            when(repository.findByTipo(TipoMovimentacao.ENTRADA)).thenReturn(List.of(movimentacao));
            List<Movimentacao> resultado = service.listarPorTipo(TipoMovimentacao.ENTRADA);
            assertFalse(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando sem resultados")
        void deveLancarExcecaoSemResultados() {
            when(repository.findByTipo(TipoMovimentacao.ENTRADA)).thenReturn(Collections.emptyList());
            assertThrows(SemMovimentacoesException.class,
                    () -> service.listarPorTipo(TipoMovimentacao.ENTRADA));
        }
    }

    // ==================== ENTRADA ====================

    @Nested
    @DisplayName("entrada()")
    class Entrada {

        private EntradaRequestDTO entradaDTO;

        @BeforeEach
        void setUp() {
            entradaDTO = new EntradaRequestDTO(50, LocalDate.now(), LocalDate.now().plusMonths(6),
                    "Doação", 1, 1);
        }

        @Test
        @DisplayName("Deve registrar entrada com dados válidos")
        void deveRegistrarEntrada() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.save(any(ProdutoLote.class))).thenReturn(lote);
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);
            when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class))).thenReturn(movEstoque);

            Movimentacao resultado = service.entrada(entradaDTO);

            assertNotNull(resultado);
            verify(produtoLoteRepository).save(any(ProdutoLote.class));
            verify(repository).save(any(Movimentacao.class));
            verify(movimentacaoEstoqueRepository).save(any(MovimentacaoEstoque.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(ProdutoNaoEncontradoException.class, () -> service.entrada(entradaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(UsuarioNaoEncontradoException.class, () -> service.entrada(entradaDTO));
        }
    }

    @Nested
    @DisplayName("atualizarEntrada()")
    class AtualizarEntrada {

        private EntradaRequestDTO entradaDTO;

        @BeforeEach
        void setUp() {
            entradaDTO = new EntradaRequestDTO(60, LocalDate.now(), LocalDate.now().plusMonths(6),
                    "Doação atualizada", 1, 1);
        }

        @Test
        @DisplayName("Deve atualizar entrada com dados válidos")
        void deveAtualizarEntrada() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);

            Movimentacao resultado = service.atualizarEntrada(entradaDTO, 1);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarEntrada(entradaDTO, 99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação não é ENTRADA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarEntrada(entradaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(ProdutoNaoEncontradoException.class,
                    () -> service.atualizarEntrada(entradaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> service.atualizarEntrada(entradaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando MovimentacaoEstoque não encontrada")
        void deveLancarExcecaoMovEstoqueNaoEncontrada() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarEntrada(entradaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade resultante negativa")
        void deveLancarExcecaoEstoqueInsuficiente() {
            lote.setQuantidade(5);
            EntradaRequestDTO dtoReduzido = new EntradaRequestDTO(1, LocalDate.now(),
                    LocalDate.now().plusMonths(6), "Motivo", 1, 1);
            movimentacao.setQuantidade(50);

            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));

            assertThrows(EstoqueInsuficienteException.class,
                    () -> service.atualizarEntrada(dtoReduzido, 1));
        }
    }

    @Nested
    @DisplayName("removerEntrada()")
    class RemoverEntrada {

        @Test
        @DisplayName("Deve remover entrada com sucesso")
        void deveRemoverEntrada() {
            lote.setQuantidade(100);
            movEstoque.setQuantidade(50);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));

            service.removerEntrada(1);

            verify(repository).delete(movimentacao);
            verify(movimentacaoEstoqueRepository).delete(movEstoque);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerEntrada(99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é ENTRADA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerEntrada(1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando MovimentacaoEstoque não encontrada")
        void deveLancarExcecaoMovEstoqueNaoEncontrada() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerEntrada(1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade resultante negativa")
        void deveLancarExcecaoEstoqueInsuficiente() {
            lote.setQuantidade(10);
            movEstoque.setQuantidade(50);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));

            assertThrows(EstoqueInsuficienteException.class, () -> service.removerEntrada(1));
        }
    }

    // ==================== SAÍDA ====================

    @Nested
    @DisplayName("saida()")
    class Saida {

        private SaidaRequestDTO saidaDTO;

        @BeforeEach
        void setUp() {
            saidaDTO = new SaidaRequestDTO(10, LocalDate.now(), "Distribuição", 1, 1);
        }

        @Test
        @DisplayName("Deve registrar saída com estoque suficiente")
        void deveRegistrarSaida() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(100);
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);
            when(produtoLoteRepository.findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(1, 0))
                    .thenReturn(List.of(lote));

            Movimentacao resultado = service.saida(saidaDTO);

            assertNotNull(resultado);
            verify(repository).save(any(Movimentacao.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(ProdutoNaoEncontradoException.class, () -> service.saida(saidaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(UsuarioNaoEncontradoException.class, () -> service.saida(saidaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente")
        void deveLancarExcecaoEstoqueInsuficiente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(5);

            assertThrows(EstoqueInsuficienteException.class, () -> service.saida(saidaDTO));
        }

        @Test
        @DisplayName("Deve distribuir saída em múltiplos lotes")
        void deveDistribuirEmMultiplosLotes() {
            SaidaRequestDTO saidaGrande = new SaidaRequestDTO(80, LocalDate.now(), "Distribuição", 1, 1);

            ProdutoLote lote1 = new ProdutoLote();
            lote1.setId(1);
            lote1.setQuantidade(30);
            lote1.setProduto(produto);

            ProdutoLote lote2 = new ProdutoLote();
            lote2.setId(2);
            lote2.setQuantidade(60);
            lote2.setProduto(produto);

            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(90);
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);
            when(produtoLoteRepository.findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(1, 0))
                    .thenReturn(List.of(lote1, lote2));

            Movimentacao resultado = service.saida(saidaGrande);

            assertNotNull(resultado);
            verify(produtoLoteRepository, times(2)).save(any(ProdutoLote.class));
            verify(movimentacaoEstoqueRepository, times(2)).save(any(MovimentacaoEstoque.class));
        }
    }

    @Nested
    @DisplayName("atualizarSaida()")
    class AtualizarSaida {

        private SaidaRequestDTO saidaDTO;

        @BeforeEach
        void setUp() {
            saidaDTO = new SaidaRequestDTO(10, LocalDate.now(), "Distribuição atualizada", 1, 1);
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
        }

        @Test
        @DisplayName("Deve atualizar saída com dados válidos")
        void deveAtualizarSaida() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findByMovimentacaoId(1))
                    .thenReturn(List.of(movEstoque));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(100);
            when(produtoLoteRepository.findByProdutoIdAndQuantidadeGreaterThanOrderByValidadeAsc(1, 0))
                    .thenReturn(List.of(lote));
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);

            Movimentacao resultado = service.atualizarSaida(saidaDTO, 1);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarSaida(saidaDTO, 99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é SAIDA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarSaida(saidaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando itens antigos não encontrados")
        void deveLancarExcecaoItensNaoEncontrados() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findByMovimentacaoId(1))
                    .thenReturn(Collections.emptyList());

            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarSaida(saidaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente após estorno")
        void deveLancarExcecaoEstoqueInsuficiente() {
            SaidaRequestDTO saidaGrande = new SaidaRequestDTO(500, LocalDate.now(), "Grande", 1, 1);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(movimentacaoEstoqueRepository.findByMovimentacaoId(1))
                    .thenReturn(List.of(movEstoque));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(10);

            assertThrows(EstoqueInsuficienteException.class,
                    () -> service.atualizarSaida(saidaGrande, 1));
        }
    }

    @Nested
    @DisplayName("removerSaida()")
    class RemoverSaida {

        @BeforeEach
        void setUp() {
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
        }

        @Test
        @DisplayName("Deve remover saída e estornar quantidades")
        void deveRemoverSaida() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findByMovimentacaoId(1))
                    .thenReturn(List.of(movEstoque));

            service.removerSaida(1);

            verify(movimentacaoEstoqueRepository).deleteAll(anyList());
            verify(repository).delete(movimentacao);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerSaida(99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é SAIDA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerSaida(1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando itens não encontrados")
        void deveLancarExcecaoItensNaoEncontrados() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findByMovimentacaoId(1))
                    .thenReturn(Collections.emptyList());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerSaida(1));
        }
    }

    // ==================== PERDA ====================

    @Nested
    @DisplayName("perda()")
    class Perda {

        private PerdaRequestDTO perdaDTO;

        @BeforeEach
        void setUp() {
            perdaDTO = new PerdaRequestDTO(5, LocalDate.now(), "Produto vencido", 1, 1, 1);
        }

        @Test
        @DisplayName("Deve registrar perda com estoque suficiente")
        void deveRegistrarPerda() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(100);
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);

            Movimentacao resultado = service.perda(perdaDTO);

            assertNotNull(resultado);
            verify(repository).save(any(Movimentacao.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(ProdutoNaoEncontradoException.class, () -> service.perda(perdaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(UsuarioNaoEncontradoException.class, () -> service.perda(perdaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque total insuficiente")
        void deveLancarExcecaoEstoqueTotalInsuficiente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(2);

            assertThrows(EstoqueInsuficienteException.class, () -> service.perda(perdaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando lote inexistente")
        void deveLancarExcecaoLoteInexistente() {
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(100);
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(LoteNaoEncontradoException.class, () -> service.perda(perdaDTO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade do lote insuficiente")
        void deveLancarExcecaoQuantidadeLoteInsuficiente() {
            lote.setQuantidade(2);
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(100);
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));

            assertThrows(EstoqueInsuficienteException.class, () -> service.perda(perdaDTO));
        }
    }

    @Nested
    @DisplayName("atualizarPerda()")
    class AtualizarPerda {

        private PerdaRequestDTO perdaDTO;

        @BeforeEach
        void setUp() {
            perdaDTO = new PerdaRequestDTO(5, LocalDate.now(), "Perda atualizada", 1, 1, 1);
            movimentacao.setTipo(TipoMovimentacao.PERDA);
        }

        @Test
        @DisplayName("Deve atualizar perda com dados válidos")
        void deveAtualizarPerda() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(200);
            when(repository.save(any(Movimentacao.class))).thenReturn(movimentacao);

            Movimentacao resultado = service.atualizarPerda(perdaDTO, 1);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarPerda(perdaDTO, 99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é PERDA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarPerda(perdaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto inexistente")
        void deveLancarExcecaoProdutoInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(ProdutoNaoEncontradoException.class,
                    () -> service.atualizarPerda(perdaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(UsuarioNaoEncontradoException.class,
                    () -> service.atualizarPerda(perdaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando lote inexistente")
        void deveLancarExcecaoLoteInexistente() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.empty());
            assertThrows(LoteNaoEncontradoException.class,
                    () -> service.atualizarPerda(perdaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando MovimentacaoEstoque não encontrada")
        void deveLancarExcecaoMovEstoqueNaoEncontrada() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class,
                    () -> service.atualizarPerda(perdaDTO, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque total insuficiente")
        void deveLancarExcecaoEstoqueTotalInsuficiente() {
            PerdaRequestDTO perdaGrande = new PerdaRequestDTO(500, LocalDate.now(), "Grande", 1, 1, 1);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(10);

            assertThrows(EstoqueInsuficienteException.class,
                    () -> service.atualizarPerda(perdaGrande, 1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade do novo lote insuficiente")
        void deveLancarExcecaoQuantidadeNovoLoteInsuficiente() {
            PerdaRequestDTO perdaGrande = new PerdaRequestDTO(200, LocalDate.now(), "Grande", 1, 1, 1);
            lote.setQuantidade(50);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
            when(produtoLoteRepository.findById(1)).thenReturn(Optional.of(lote));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(500);

            assertThrows(EstoqueInsuficienteException.class,
                    () -> service.atualizarPerda(perdaGrande, 1));
        }
    }

    @Nested
    @DisplayName("removerPerda()")
    class RemoverPerda {

        @BeforeEach
        void setUp() {
            movimentacao.setTipo(TipoMovimentacao.PERDA);
        }

        @Test
        @DisplayName("Deve remover perda e estornar quantidade")
        void deveRemoverPerda() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.of(movEstoque));

            service.removerPerda(1);

            verify(movimentacaoEstoqueRepository).delete(movEstoque);
            verify(repository).delete(movimentacao);
        }

        @Test
        @DisplayName("Deve lançar exceção quando movimentação inexistente")
        void deveLancarExcecaoMovInexistente() {
            when(repository.findById(99)).thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerPerda(99));
        }

        @Test
        @DisplayName("Deve lançar exceção quando não é PERDA")
        void deveLancarExcecaoTipoErrado() {
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerPerda(1));
        }

        @Test
        @DisplayName("Deve lançar exceção quando MovimentacaoEstoque não encontrada")
        void deveLancarExcecaoMovEstoqueNaoEncontrada() {
            when(repository.findById(1)).thenReturn(Optional.of(movimentacao));
            when(movimentacaoEstoqueRepository.findFirstByMovimentacaoId(1))
                    .thenReturn(Optional.empty());
            assertThrows(MovimentacaoNaoEncontradaException.class, () -> service.removerPerda(1));
        }
    }

    // ==================== UTILITÁRIO ====================

    @Nested
    @DisplayName("calcularEstoqueTotal()")
    class CalcularEstoqueTotal {

        @Test
        @DisplayName("Deve retornar soma das quantidades")
        void deveRetornarSoma() {
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(150);
            Integer resultado = service.calcularEstoqueTotal(1);
            assertEquals(150, resultado);
        }

        @Test
        @DisplayName("Deve retornar 0 quando null")
        void deveRetornarZeroQuandoNull() {
            when(produtoLoteRepository.sumQuantidadeByProdutoId(1)).thenReturn(null);
            Integer resultado = service.calcularEstoqueTotal(1);
            assertEquals(0, resultado);
        }
    }

    @Nested
    @DisplayName("listarHistoricoEntrada()")
    class ListarHistoricoEntrada {

        @Test
        @DisplayName("Deve retornar página com movimentações de entrada")
        void deveRetornarPaginaComEntradas() {
            movimentacao.setTipo(TipoMovimentacao.ENTRADA);
            Page<Movimentacao> page = new org.springframework.data.domain.PageImpl<>(
                    List.of(movimentacao), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            when(repository.findByTipoOrderByDataDesc(
                    eq(TipoMovimentacao.ENTRADA), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            Page<Movimentacao> resultado = service.listarHistoricoEntrada(
                    org.springframework.data.domain.PageRequest.of(0, 10));

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals(TipoMovimentacao.ENTRADA, resultado.getContent().get(0).getTipo());
            verify(repository).findByTipoOrderByDataDesc(
                    eq(TipoMovimentacao.ENTRADA), any(org.springframework.data.domain.Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há entradas")
        void deveRetornarPaginaVazia() {
            Page<Movimentacao> page = new org.springframework.data.domain.PageImpl<>(
                    Collections.emptyList(), org.springframework.data.domain.PageRequest.of(0, 10), 0);
            when(repository.findByTipoOrderByDataDesc(
                    eq(TipoMovimentacao.ENTRADA), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            Page<Movimentacao> resultado = service.listarHistoricoEntrada(
                    org.springframework.data.domain.PageRequest.of(0, 10));

            assertNotNull(resultado);
            assertEquals(0, resultado.getTotalElements());
            assertTrue(resultado.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("listarHistoricoSaida()")
    class ListarHistoricoSaida {

        @Test
        @DisplayName("Deve retornar página com movimentações de saída e perda")
        void deveRetornarPaginaComSaidasEPerdas() {
            movimentacao.setTipo(TipoMovimentacao.SAIDA);
            Page<Movimentacao> page = new org.springframework.data.domain.PageImpl<>(
                    List.of(movimentacao), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            when(repository.findByTipoInOrderByDataDesc(
                    eq(List.of(TipoMovimentacao.SAIDA, TipoMovimentacao.PERDA)),
                    any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            Page<Movimentacao> resultado = service.listarHistoricoSaida(
                    org.springframework.data.domain.PageRequest.of(0, 10));

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            verify(repository).findByTipoInOrderByDataDesc(
                    eq(List.of(TipoMovimentacao.SAIDA, TipoMovimentacao.PERDA)),
                    any(org.springframework.data.domain.Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há saídas nem perdas")
        void deveRetornarPaginaVazia() {
            Page<Movimentacao> page = new org.springframework.data.domain.PageImpl<>(
                    Collections.emptyList(), org.springframework.data.domain.PageRequest.of(0, 10), 0);
            when(repository.findByTipoInOrderByDataDesc(
                    eq(List.of(TipoMovimentacao.SAIDA, TipoMovimentacao.PERDA)),
                    any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            Page<Movimentacao> resultado = service.listarHistoricoSaida(
                    org.springframework.data.domain.PageRequest.of(0, 10));

            assertNotNull(resultado);
            assertEquals(0, resultado.getTotalElements());
            assertTrue(resultado.getContent().isEmpty());
        }
    }
}
