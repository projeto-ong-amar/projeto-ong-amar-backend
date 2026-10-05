package school.sptech.projeto_amar.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import school.sptech.projeto_amar.domain.enums.Categoria;

@Converter
public class CategoriaConverter implements AttributeConverter<Categoria, String> {
    @Override
    public String convertToDatabaseColumn(Categoria categoria) {
        return categoria == null ? null : categoria.getValor();
    }

    @Override
    public Categoria convertToEntityAttribute(String valor) {
        return valor == null ? null : Categoria.fromValor(valor);
    }
}
