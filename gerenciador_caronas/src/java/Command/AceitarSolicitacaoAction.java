package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Carona;
import model.Solicitacao;

public class AceitarSolicitacaoAction implements ICommand {

    private final Solicitacao solicitacao;
    private final SolicitacaoDAO solicitacaoDAO;
    private final CaronaDAO caronaDAO;

    public AceitarSolicitacaoAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
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

        Carona referenciaCarona = Carona.getBuilder().id(solicitacaoCompleta.getCaronaId()).build();
        Carona carona = caronaDAO.buscarPorId(referenciaCarona);
        if (carona == null) {
            throw new IllegalArgumentException("Carona não encontrada");
        }

        solicitacaoCompleta.aceitar();
        carona.atualizarVagas(1);

        solicitacaoDAO.atualizar(solicitacaoCompleta);
        caronaDAO.atualizar(carona);
    }
}