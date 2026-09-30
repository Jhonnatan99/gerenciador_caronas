package Command;

import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Usuario;
import util.CpfUtil;

public class CadastrarUsuarioAction implements ICommand {

    private final Usuario usuario;
    private final UsuarioDAO usuarioDAO;

    public CadastrarUsuarioAction(Object dado, CaronaDAO caronaDAO, SolicitacaoDAO solicitacaoDAO, UsuarioDAO usuarioDAO) {
        this.usuario = (Usuario) dado;
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public void executar() {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome completo");
        }
        if (!CpfUtil.valido(usuario.getCpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres");
        }

        Usuario existente = usuarioDAO.buscarPorCpf(usuario);
        if (existente != null) {
            throw new IllegalStateException("Já existe um usuário cadastrado com esse CPF");
        }

        usuario.setCpf(CpfUtil.limpar(usuario.getCpf()));
        usuarioDAO.salvar(usuario);
    }
}