package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Carona;

import java.time.LocalDate;

public class CadastrarCaronaAction implements ICommand {

    private final Carona carona;
    private final CaronaDAO caronaDAO;

    public CadastrarCaronaAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.carona = (Carona) dado;
        this.caronaDAO = caronaDAO;
    }

    @Override
    public void executar() {
        validar();
        caronaDAO.salvar(carona);
    }

    private void validar() {
        if (carona.getMotoristaId() == null) {
            throw new IllegalArgumentException("Carona precisa estar associada a um motorista autenticado");
        }
        if (carona.getOrigem() == null || carona.getOrigem().isBlank()) {
            throw new IllegalArgumentException("Informe a origem da carona");
        }
        if (carona.getDestino() == null || carona.getDestino().isBlank()) {
            throw new IllegalArgumentException("Informe o destino da carona");
        }
        if (carona.getOrigem().equalsIgnoreCase(carona.getDestino())) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais");
        }
        if (carona.getData() == null) {
            throw new IllegalArgumentException("Informe a data da carona");
        }
        if (carona.getData().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data da carona não pode ser no passado");
        }
        if (carona.getHorario() == null) {
            throw new IllegalArgumentException("Informe o horário da carona");
        }
        if (carona.getVagasTotais() <= 0) {
            throw new IllegalArgumentException("A quantidade de vagas deve ser maior que zero");
        }
        if (carona.getVagasTotais() > 4) {
            throw new IllegalArgumentException("A quantidade de vagas não pode ser maior que 4");
        }
        if (carona.getValorPorPessoa() <= 0) {
            throw new IllegalArgumentException("O valor por pessoa deve ser maior que zero");
        }
    }
}