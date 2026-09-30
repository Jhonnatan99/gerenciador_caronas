package Controller;

import Command.ActionFactory;
import Conexxo.Conexxo;
import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    private UsuarioController controller;

    @Override
    public void init() throws ServletException {
        Conexxo conexxo = new Conexxo();

        CaronaDAO caronaDAO = new CaronaDAO(conexxo);
        UsuarioDAO usuarioDAO = new UsuarioDAO(conexxo);
        SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO(conexxo);

        ActionFactory actionFactory = new ActionFactory(caronaDAO, solicitacaoDAO, usuarioDAO);

        this.controller = new UsuarioController(usuarioDAO, actionFactory);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processarRequisicao(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processarRequisicao(request, response);
    }

    private void processarRequisicao(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String acao = request.getParameter("acao");
        
        PrintWriter out = response.getWriter();
        try {
            switch (acao == null ? "" : acao) {
                case "CadastrarUsuario":
                    cadastrarUsuario(request, out);
                    break;

                case "Login":
                    login(request, out);
                    break;

                case "Logout":
                    logout(request, out);
                    break;

                case "VisualizarPerfil":
                    visualizarPerfil(request, out);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(erroJson("Ação desconhecida: " + acao));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(erroJson(e.getMessage()));
        } finally {
            out.flush();
        }
    }

    private void cadastrarUsuario(HttpServletRequest request, PrintWriter out) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getParameter("nome"));
        usuario.setCpf(request.getParameter("cpf"));
        usuario.setEmail(request.getParameter("email"));
        usuario.setTelefone(request.getParameter("telefone"));
        usuario.setSenha(request.getParameter("senha"));

        controller.cadastrarUsuario(usuario);
        out.print("{\"sucesso\":true}");
    }

    private void login(HttpServletRequest request, PrintWriter out) {
        Usuario tentativa = new Usuario();
        tentativa.setCpf(request.getParameter("cpf"));
        tentativa.setSenha(request.getParameter("senha"));

        Usuario usuarioLogado = controller.login(tentativa);

        out.print(usuarioParaJson(usuarioLogado));
    }

    private void logout(HttpServletRequest request, PrintWriter out) {
        out.print("{\"sucesso\":true}");
    }

    private void visualizarPerfil(HttpServletRequest request, PrintWriter out) {
        Usuario referencia = usuarioDaRequisicao(request);

        Usuario usuario = controller.visualizarPerfil(referencia);
        if (usuario == null) {
            out.print(erroJson("Usuário não encontrado"));
            return;
        }
        out.print(usuarioParaJson(usuario));
    }

    private Usuario usuarioDaRequisicao(HttpServletRequest request) {
        String valor = request.getParameter("usuarioId");
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Usuário não autenticado. Faça login novamente.");
        }
        Usuario usuario = new Usuario();
        try {
            usuario.setId(Long.parseLong(valor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Usuário não autenticado. Faça login novamente.");
        }
        return usuario;
    }

    private String usuarioParaJson(Usuario u) {
        return "{"
                + "\"sucesso\":true,"
                + "\"id\":" + u.getId() + ","
                + "\"nome\":\"" + escapar(u.getNome()) + "\","
                + "\"cpf\":\"" + escapar(u.getCpf()) + "\","
                + "\"email\":\"" + escapar(u.getEmail()) + "\","
                + "\"telefone\":\"" + escapar(u.getTelefone()) + "\""
                + "}";
    }

    private String erroJson(String mensagem) {
        return "{\"sucesso\":false,\"erro\":\"" + escapar(mensagem) + "\"}";
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
    }
}