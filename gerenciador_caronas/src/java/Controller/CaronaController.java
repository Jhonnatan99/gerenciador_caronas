package Controller;

import Command.ActionFactory;
import Command.ICommand;
import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import model.Carona;
import model.Solicitacao;
import model.Usuario;

import java.util.List;

public class CaronaController {

    private final CaronaDAO caronaDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final ActionFactory actionFactory;

    public CaronaController(CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, ActionFactory actionFactory) {
        this.caronaDAO = caronaDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.actionFactory = actionFactory;
    }

    public void cadastrarCarona(Carona carona) {
        ICommand action = actionFactory.criarAction(ActionFactory.CADASTRAR_CARONA, carona);
        action.executar();
    }

    public List<Carona> buscarCaronas() {
        return caronaDAO.listarTodas();
    }

    public Carona buscarCaronaPorId(Carona referencia) {
        return caronaDAO.buscarPorId(referencia);
    }

    public List<Carona> buscarCaronasOferecidas(Usuario motorista) {
        return caronaDAO.listarPorMotorista(motorista);
    }

    public Solicitacao solicitarVaga(Carona carona, Usuario usuario) {
        Solicitacao solicitacao = new Solicitacao(carona, usuario);

        ICommand action = actionFactory.criarAction(ActionFactory.SOLICITAR_VAGA, solicitacao);
        action.executar();

        return solicitacao;
    }

    public void aceitarSolicitacao(Solicitacao solicitacao, Usuario motoristaLogado) {
        verificarDonoDaCarona(solicitacao, motoristaLogado);
        ICommand action = actionFactory.criarAction(ActionFactory.ACEITAR_SOLICITACAO, solicitacao);
        action.executar();
    }

    public void recusarSolicitacao(Solicitacao solicitacao, Usuario motoristaLogado) {
        verificarDonoDaCarona(solicitacao, motoristaLogado);
        ICommand action = actionFactory.criarAction(ActionFactory.RECUSAR_SOLICITACAO, solicitacao);
        action.executar();
    }

    public void cancelarSolicitacao(Solicitacao solicitacao, Usuario usuarioLogado) {
        Solicitacao existente = solicitacaoDAO.buscarPorId(solicitacao);
        if (existente == null) {
            throw new IllegalArgumentException("Solicitação não encontrada");
        }
        if (!existente.getUsuarioId().equals(usuarioLogado.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para cancelar esta solicitação");
        }
        ICommand action = actionFactory.criarAction(ActionFactory.CANCELAR_SOLICITACAO, solicitacao);
        action.executar();
    }

    public List<Solicitacao> buscarSolicitacoesPorUsuario(Usuario usuario) {
        return solicitacaoDAO.listarPorUsuario(usuario);
    }

    public List<Solicitacao> buscarSolicitacoesRecebidas(Usuario motorista) {
        return solicitacaoDAO.listarPorMotorista(motorista);
    }

    public Solicitacao buscarSolicitacaoPorId(Solicitacao referencia) {
        return solicitacaoDAO.buscarPorId(referencia);
    }

    private void verificarDonoDaCarona(Solicitacao solicitacao, Usuario motoristaLogado) {
        Solicitacao existente = solicitacaoDAO.buscarPorId(solicitacao);
        if (existente == null) {
            throw new IllegalArgumentException("Solicitação não encontrada");
        }
        Carona referencia = Carona.getBuilder().id(existente.getCaronaId()).build();
        Carona carona = caronaDAO.buscarPorId(referencia);
        if (carona == null || carona.getMotoristaId() == null
                || !carona.getMotoristaId().equals(motoristaLogado.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para gerenciar esta solicitação");
        }
    }
}