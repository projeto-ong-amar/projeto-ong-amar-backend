package school.sptech.projeto_amar.service;

import org.springframework.stereotype.Service;
import school.sptech.projeto_amar.domain.Movimentacao;
import school.sptech.projeto_amar.exception.SemMovimentacoesException;
import school.sptech.projeto_amar.repository.MovimentacaoRepository;

import java.util.List;

@Service
public class MovimentacaoService {
    private MovimentacaoRepository repository;

    public MovimentacaoService(MovimentacaoRepository repository) {
        this.repository = repository;
    }

    public List<Movimentacao> listar() {
        List<Movimentacao> retorno = repository.findAll();

        if (retorno.isEmpty()) {
            throw new SemMovimentacoesException();
        }

        return retorno;
    }
}
