package Controller;

import Command.ActionFactory;
import Conexxo.Conexxo;
import dao.CaronaDAO;
import dao.SolicitacaoDAO;
import dao.UsuarioDAO;
import model.Carona;
import model.Solicitacao;
import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "CaronaServlet", urlPatterns = {"/CaronaServlet"})
public class CaronaServlet extends HttpServlet {

    private CaronaController controller;
    private CaronaDAO caronaDAO;
    private UsuarioDAO usuarioDAO;

    @Override
    public void init() throws ServletException {
        Conexxo conexxo = new Conexxo();

        this.caronaDAO = new CaronaDAO(conexxo);
        this.usuarioDAO = new UsuarioDAO(conexxo);
        SolicitacaoDAO solicitacaoDAO = new SolicitacaoDAO(conexxo);

        ActionFactory actionFactory = new ActionFactory(caronaDAO, solicitacaoDAO, usuarioDAO);

        this.controller = new CaronaController(caronaDAO, solicitacaoDAO, actionFactory);
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
        if (acao == null || acao.isEmpty()) {
            acao = "ListarCaronas";
        }
        
        PrintWriter out = response.getWriter();
        try {
            switch (acao) {
                case "ListarCaronas":
                    listarCaronas(out);
                    break;

                case "ListarMinhasSolicitacoes":
                    listarMinhasSolicitacoes(request, out);
                    break;

                case "ListarCaronasOferecidas":
                    listarCaronasOferecidas(request, out);
                    break;

                case "ListarSolicitacoesRecebidas":
                    listarSolicitacoesRecebidas(request, out);
                    break;

                case ActionFactory.CADASTRAR_CARONA:
                    cadastrarCarona(request, out);
                    break;

                case ActionFactory.SOLICITAR_VAGA:
                    solicitarVaga(request, out);
                    break;

                case ActionFactory.ACEITAR_SOLICITACAO: {
                    Usuario motoristaLogado = usuarioDaRequisicao(request);
                    controller.aceitarSolicitacao(referenciaSolicitacao(request), motoristaLogado);
                    out.print("{\"sucesso\":true}");
                    break;
                }

                case ActionFactory.RECUSAR_SOLICITACAO: {
                    Usuario motoristaLogado = usuarioDaRequisicao(request);
                    controller.recusarSolicitacao(referenciaSolicitacao(request), motoristaLogado);
                    out.print("{\"sucesso\":true}");
                    break;
                }

                case ActionFactory.CANCELAR_SOLICITACAO: {
                    Usuario usuarioLogado = usuarioDaRequisicao(request);
                    controller.cancelarSolicitacao(referenciaSolicitacao(request), usuarioLogado);
                    out.print("{\"sucesso\":true}");
                    break;
                }

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

    private void listarCaronas(PrintWriter out) {
        List<Carona> caronas = controller.buscarCaronas();
        out.print(caronaListaParaJson(caronas));
    }

    private void listarMinhasSolicitacoes(HttpServletRequest request, PrintWriter out) {
        Usuario usuarioLogado = usuarioDaRequisicao(request);
        List<Solicitacao> solicitacoes = controller.buscarSolicitacoesPorUsuario(usuarioLogado);
        out.print(solicitacaoListaParaJson(solicitacoes, false));
    }

    private void listarCaronasOferecidas(HttpServletRequest request, PrintWriter out) {
        Usuario motoristaLogado = usuarioDaRequisicao(request);
        List<Carona> caronas = controller.buscarCaronasOferecidas(motoristaLogado);
        out.print(caronaListaParaJson(caronas));
    }

    private void listarSolicitacoesRecebidas(HttpServletRequest request, PrintWriter out) {
        Usuario motoristaLogado = usuarioDaRequisicao(request);
        List<Solicitacao> solicitacoes = controller.buscarSolicitacoesRecebidas(motoristaLogado);
        out.print(solicitacaoListaParaJson(solicitacoes, true));
    }

    private void cadastrarCarona(HttpServletRequest request, PrintWriter out) {
    
        Usuario motoristaLogado = usuarioDaRequisicao(request);

        Carona carona = Carona.getBuilder()
                .motoristaId(motoristaLogado.getId())
                .origem(paramTexto(request, "origem"))
                .destino(paramTexto(request, "destino"))
                .data(parseData(request.getParameter("data")))
                .horario(parseHorario(request.getParameter("horario")))
                .vagas(parseInteiro(request.getParameter("vagas"), "Vagas"))
                .valorPorPessoa(parseDecimal(request.getParameter("valor"), "Valor"))
                .build();

        controller.cadastrarCarona(carona);
        out.print("{\"sucesso\":true}");
    }

    private void solicitarVaga(HttpServletRequest request, PrintWriter out) {
        Usuario usuarioLogado = usuarioDaRequisicao(request);
        Long caronaId = Long.parseLong(request.getParameter("caronaId"));

        Carona referenciaCarona = Carona.getBuilder().id(caronaId).build();

        Solicitacao solicitacao = controller.solicitarVaga(referenciaCarona, usuarioLogado);
        out.print("{\"sucesso\":true,\"solicitacaoId\":" + solicitacao.getId() + "}");
    }

    private Solicitacao referenciaSolicitacao(HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("solicitacaoId"));
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(id);
        return solicitacao;
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

    private String paramTexto(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        return valor == null ? "" : valor.trim();
    }

    private LocalDate parseData(String valor) {
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Data inválida");
        }
    }

    private LocalTime parseHorario(String valor) {
        try {
            return LocalTime.parse(valor);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Horário inválido");
        }
    }

    private int parseInteiro(String valor, String nomeCampo) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um número inteiro válido");
        }
    }

    private double parseDecimal(String valor, String nomeCampo) {
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um número válido");
        }
    }

    private String caronaListaParaJson(List<Carona> caronas) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < caronas.size(); i++) {
            if (i > 0) json.append(",");
            json.append(caronaParaJson(caronas.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String caronaParaJson(Carona c) {
        return "{"
                + "\"id\":" + c.getId() + ","
                + "\"origem\":\"" + escapar(c.getOrigem()) + "\","
                + "\"destino\":\"" + escapar(c.getDestino()) + "\","
                + "\"data\":\"" + c.getData() + "\","
                + "\"horario\":\"" + c.getHorario() + "\","
                + "\"vagasDisponiveis\":" + c.getVagasDisponiveis() + ","
                + "\"vagasTotais\":" + c.getVagasTotais() + ","
                + "\"valorPorPessoa\":" + c.getValorPorPessoa() + ","
                + "\"status\":\"" + c.getStatus() + "\""
                + "}";
    }

    private String solicitacaoListaParaJson(List<Solicitacao> solicitacoes, boolean incluirSolicitante) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < solicitacoes.size(); i++) {
            if (i > 0) json.append(",");
            json.append(solicitacaoComCaronaParaJson(solicitacoes.get(i), incluirSolicitante));
        }
        json.append("]");
        return json.toString();
    }

    private String solicitacaoComCaronaParaJson(Solicitacao s, boolean incluirSolicitante) {
        Carona referencia = Carona.getBuilder().id(s.getCaronaId()).build();
        Carona carona = caronaDAO.buscarPorId(referencia);

        String origem = carona != null ? carona.getOrigem() : "?";
        String destino = carona != null ? carona.getDestino() : "?";
        String data = carona != null ? String.valueOf(carona.getData()) : "";
        String horario = carona != null ? String.valueOf(carona.getHorario()) : "";

        String solicitanteNomeJson = "";
        if (incluirSolicitante) {
            Usuario referenciaUsuario = new Usuario();
            referenciaUsuario.setId(s.getUsuarioId());
            Usuario solicitante = usuarioDAO.buscarPorId(referenciaUsuario);
            String nome = solicitante != null ? solicitante.getNome() : "?";
            solicitanteNomeJson = ",\"solicitanteNome\":\"" + escapar(nome) + "\"";
        }

        return "{"
                + "\"id\":" + s.getId() + ","
                + "\"status\":\"" + s.getStatus() + "\","
                + "\"dataSolicitacao\":\"" + s.getDataSolicitacao() + "\","
                + "\"caronaOrigem\":\"" + escapar(origem) + "\","
                + "\"caronaDestino\":\"" + escapar(destino) + "\","
                + "\"caronaData\":\"" + data + "\","
                + "\"caronaHorario\":\"" + horario + "\""
                + solicitanteNomeJson
                + "}";
    }

    private String erroJson(String mensagem) {
        return "{\"sucesso\":false,\"erro\":\"" + escapar(mensagem) + "\"}";
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
    }
}