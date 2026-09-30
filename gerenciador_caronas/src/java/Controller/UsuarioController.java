package Controller;

import Command.ActionFactory;
import Command.ICommand;
import dao.UsuarioDAO;
import model.Usuario;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;
    private final ActionFactory actionFactory;

    public UsuarioController(UsuarioDAO usuarioDAO, ActionFactory actionFactory) {
        this.usuarioDAO = usuarioDAO;
        this.actionFactory = actionFactory;
    }

    public void cadastrarUsuario(Usuario usuario) {
        ICommand action = actionFactory.criarAction(ActionFactory.CADASTRAR_USUARIO, usuario);
        action.executar();
    }

    public Usuario login(Usuario tentativa) {
        Usuario existente = usuarioDAO.buscarPorCpf(tentativa);
        if (existente == null) {
            throw new IllegalArgumentException("CPF não cadastrado");
        }
        if (!existente.validarSenha(tentativa.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta");
        }

        existente.setSenha(null);
        return existente;
    }

    public Usuario visualizarPerfil(Usuario usuario) {
        Usuario encontrado = usuarioDAO.buscarPorId(usuario);
        if (encontrado != null) {
            encontrado.setSenha(null);
        }
        return encontrado;
    }
}