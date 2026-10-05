package school.sptech.projeto_amar;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import school.sptech.projeto_amar.domain.Produto;
import school.sptech.projeto_amar.domain.ProdutoLote;
import school.sptech.projeto_amar.domain.enums.Categoria;
import school.sptech.projeto_amar.repository.ProdutoLoteRepository;
import school.sptech.projeto_amar.repository.ProdutoRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProdutoCategoriaSchemaIntegrationTest {
    private static final String URL = "jdbc:h2:mem:categoria-schema;MODE=MySQL;DB_CLOSE_DELAY=-1";

    @Autowired private EntityManager entityManager;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ProdutoLoteRepository loteRepository;

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) throws IOException, SQLException {
        String schema = new ClassPathResource("schema.sql").getContentAsString(StandardCharsets.UTF_8);
        try (var connection = DriverManager.getConnection(URL, "sa", "");
             var statement = connection.createStatement()) {
            for (String comando : schema.split(";")) {
                String sql = comando.strip();
                // H2 selects its database via JDBC instead of CREATE DATABASE / USE.
                if (!sql.isEmpty() && !sql.startsWith("CREATE DATABASE") && !sql.startsWith("USE ")) {
                    statement.execute(sql);
                }
            }
        }
        registry.add("spring.datasource.url", () -> URL);
    }

    @Test
    void persisteValoresDoEnumDoSchemaEConsultaEstoqueSemTabelaCategoria() {
        Produto alimento = salvarProduto("Arroz", Categoria.ALIMENTOS);
        Produto higiene = salvarProduto("Sabonete", Categoria.PRODUTOS_DE_HIGIENE);
        Produto semCategoria = salvarProduto("Sem categoria", null);
        salvarLote(alimento, 10);
        salvarLote(higiene, 5);
        salvarLote(semCategoria, 2);
        entityManager.flush();
        entityManager.clear();

        assertEquals("PRODUTOS DE HIGIENE", entityManager.createNativeQuery(
                "SELECT categoria FROM produto WHERE id = :id", String.class)
                .setParameter("id", higiene.getId()).getSingleResult());
        assertEquals(Categoria.PRODUTOS_DE_HIGIENE,
                produtoRepository.findById(higiene.getId()).orElseThrow().getCategoria());
        assertNull(produtoRepository.findById(semCategoria.getId()).orElseThrow().getCategoria());
        assertEquals(List.of(higiene.getId()), produtoRepository
                .findAllByCategoriaAndStatus(Categoria.PRODUTOS_DE_HIGIENE, "ATIVO")
                .stream().map(Produto::getId).toList());

        List<Object[]> estoque = loteRepository.sumEstoquePorTipo();
        assertEquals(3, estoque.size());
        assertTrue(estoque.stream().anyMatch(item ->
                item[0] == Categoria.ALIMENTOS && ((Number) item[1]).longValue() == 10));
        assertTrue(estoque.stream().anyMatch(item ->
                item[0] == Categoria.PRODUTOS_DE_HIGIENE && ((Number) item[1]).longValue() == 5));
        assertTrue(estoque.stream().anyMatch(item ->
                item[0] == null && ((Number) item[1]).longValue() == 2));
    }

    private Produto salvarProduto(String nome, Categoria categoria) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setStatus("ATIVO");
        produto.setCategoria(categoria);
        return produtoRepository.save(produto);
    }

    private void salvarLote(Produto produto, int quantidade) {
        ProdutoLote lote = new ProdutoLote();
        lote.setProduto(produto);
        lote.setQuantidade(quantidade);
        lote.setDataEntrada(LocalDate.now());
        loteRepository.save(lote);
    }
}
