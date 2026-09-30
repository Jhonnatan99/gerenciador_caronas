package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Solicitacao;

public class RecusarSolicitacaoAction implements ICommand {

    private final Solicitacao solicitacao;
    private final SolicitacaoDAO solicitacaoDAO;

    public RecusarSolicitacaoAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.solicitacao = (Solicitacao) dado;
        this.solicitacaoDAO = solicitacaoDAO;
    }

    @Override
    public void executar() {
        Solicitacao solicitacaoCompleta = solicitacaoDAO.buscarPorId(solicitacao);
        if (solicitacaoCompleta == null) {
            throw new IllegalArgumentException("Solicitação não encontrada");
        }

        solicitacaoCompleta.recusar();
        solicitacaoDAO.atualizar(solicitacaoCompleta);
    }
}