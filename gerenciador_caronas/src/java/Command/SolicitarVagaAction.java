package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Carona;
import model.Solicitacao;
import model.StatusCarona;

public class SolicitarVagaAction implements ICommand {

    private final Solicitacao solicitacao;
    private final CaronaDAO caronaDAO;
    private final SolicitacaoDAO solicitacaoDAO;

    public SolicitarVagaAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.solicitacao = (Solicitacao) dado;
        this.caronaDAO = caronaDAO;
        this.solicitacaoDAO = solicitacaoDAO;
    }

    @Override
    public void executar() {
        Carona referenciaCarona = Carona.getBuilder().id(solicitacao.getCaronaId()).build();
        Carona carona = caronaDAO.buscarPorId(referenciaCarona);
        if (carona == null) {
            throw new IllegalArgumentException("Carona não encontrada");
        }
        if (carona.getStatus() != StatusCarona.DISPONIVEL) {
            throw new IllegalStateException("Carona não está disponível para novas solicitações");
        }

        solicitacaoDAO.salvar(solicitacao);
    }
}