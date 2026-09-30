package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Carona;
import model.Solicitacao;
import model.StatusSolicitacao;

public class CancelarSolicitacaoAction implements ICommand {

    private final Solicitacao solicitacao;
    private final SolicitacaoDAO solicitacaoDAO;
    private final CaronaDAO caronaDAO;

    public CancelarSolicitacaoAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.solicitacao = (Solicitacao) dado;
        this.caronaDAO = caronaDAO;
        this.solicitacaoDAO = solicitacaoDAO;
    }

    @Override
    public void executar() {
        Solicitacao solicitacaoCompleta = solicitacaoDAO.buscarPorId(solicitacao);
        if (solicitacaoCompleta == null) {
            throw new IllegalArgumentException("Solicitação não encontrada");
        }

        boolean vagaJaOcupada = solicitacaoCompleta.getStatus() == StatusSolicitacao.ACEITA;

        solicitacaoCompleta.cancelar();
        solicitacaoDAO.atualizar(solicitacaoCompleta);

        if (vagaJaOcupada) {
            Carona referenciaCarona = Carona.getBuilder().id(solicitacaoCompleta.getCaronaId()).build();
            Carona carona = caronaDAO.buscarPorId(referenciaCarona);
            if (carona != null) {
                carona.removerPassageiro(null);
                caronaDAO.atualizar(carona);
            }
        }
    }
}