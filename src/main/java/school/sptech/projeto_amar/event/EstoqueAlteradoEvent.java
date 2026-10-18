package school.sptech.projeto_amar.event;

public record EstoqueAlteradoEvent(Integer produtoId, String produto, Integer quantidadeAnterior) {
}
