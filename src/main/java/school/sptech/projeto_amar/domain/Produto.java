package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;

import school.sptech.projeto_amar.domain.enums.Categoria;

@Entity
public class Produto {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(nullable = false, length = 45)
    private String nome;
    @Column(length = 45)
    private String unidadeMedida;

    @Column(nullable = false, length = 45)
    private String status;

    @Convert(converter = CategoriaConverter.class)
    @Column(columnDefinition = "ENUM('ALIMENTOS', 'PRODUTOS DE HIGIENE')")
    private Categoria categoria;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
