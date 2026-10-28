package school.sptech.projeto_amar.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Categoria {
    ALIMENTOS("ALIMENTOS", "Alimentos"),
    PRODUTOS_DE_HIGIENE("PRODUTOS DE HIGIENE", "Produtos de Higiene");

    private final String valor;
    private final String nome;

    Categoria(String valor, String nome) {
        this.valor = valor;
        this.nome = nome;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public String getNome() {
        return nome;
    }

    @JsonCreator
    public static Categoria fromValor(String valor) {
        for (Categoria categoria : values()) {
            if (categoria.valor.equals(valor)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Categoria invalida: " + valor);
    }
}
