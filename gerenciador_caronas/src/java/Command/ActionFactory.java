package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;

public class ActionFactory {

    public static final String CADASTRAR_CARONA = "CadastrarCarona";
    public static final String SOLICITAR_VAGA = "SolicitarVaga";
    public static final String ACEITAR_SOLICITACAO = "AceitarSolicitacao";
    public static final String RECUSAR_SOLICITACAO = "RecusarSolicitacao";
    public static final String CANCELAR_SOLICITACAO = "CancelarSolicitacao";
    public static final String CADASTRAR_USUARIO = "CadastrarUsuario";

    private static final String PACOTE = "Command.";
    private static final String SUFIXO = "Action";

    private final CaronaDAO caronaDAO;
    private final SolicitacaoDAO solicitacaoDAO;
    private final UsuarioDAO usuarioDAO;

    public ActionFactory(CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.caronaDAO = caronaDAO;
        this.solicitacaoDAO = solicitacaoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public ICommand criarAction(String tipo, Object dado) {
        try {
            String nomeDaClasse = PACOTE + tipo + SUFIXO;
            Class<?> classeAction = Class.forName(nomeDaClasse);

            return (ICommand) classeAction
                    .getConstructor(Object.class, CaronaDAO.class, SolicitacaoDAO.class, UsuarioDAO.class)
                    .newInstance(dado, caronaDAO, solicitacaoDAO, usuarioDAO);

        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Não foi possível criar a action para o tipo: " + tipo, e);
        }
    }
}