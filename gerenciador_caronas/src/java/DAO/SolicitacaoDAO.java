package dao;

import Conexxo.Conexxo;
import model.Solicitacao;
import model.StatusSolicitacao;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoDAO {

    private final Conexxo conexxo;

    public SolicitacaoDAO(Conexxo conexxo) {
        this.conexxo = conexxo;
    }

    public Solicitacao buscarPorId(Solicitacao solicitacao) {
        String sql = "SELECT id, carona_id, usuario_id, data_solicitacao, status "
                    + "FROM solicitacao WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, solicitacao.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearSolicitacao(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar solicitação por id: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public List<Solicitacao> listarPorUsuario(Usuario usuario) {
        String sql = "SELECT id, carona_id, usuario_id, data_solicitacao, status "
                    + "FROM solicitacao WHERE usuario_id = ? "
                    + "ORDER BY data_solicitacao DESC";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, usuario.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Solicitacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearSolicitacao(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitações do usuário: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public List<Solicitacao> listarPorMotorista(Usuario motorista) {
        String sql = "SELECT s.id, s.carona_id, s.usuario_id, s.data_solicitacao, s.status "
                    + "FROM solicitacao s "
                    + "INNER JOIN carona c ON c.id = s.carona_id "
                    + "WHERE c.motorista_id = ? "
                    + "ORDER BY s.data_solicitacao DESC";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, motorista.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Solicitacao> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapearSolicitacao(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar solicitações recebidas do motorista: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void salvar(Solicitacao solicitacao) {
        String sql = "INSERT INTO solicitacao (carona_id, usuario_id, data_solicitacao, status) "
                    + "VALUES (?, ?, ?, ?)";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, solicitacao.getCaronaId());
            stmt.setLong(2, solicitacao.getUsuarioId());
            stmt.setTimestamp(3, Timestamp.valueOf(solicitacao.getDataSolicitacao()));
            stmt.setString(4, solicitacao.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    solicitacao.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar solicitação: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void atualizar(Solicitacao solicitacao) {
        String sql = "UPDATE solicitacao "
                    + "SET carona_id = ?, usuario_id = ?, data_solicitacao = ?, status = ? "
                    + "WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, solicitacao.getCaronaId());
            stmt.setLong(2, solicitacao.getUsuarioId());
            stmt.setTimestamp(3, Timestamp.valueOf(solicitacao.getDataSolicitacao()));
            stmt.setString(4, solicitacao.getStatus().name());
            stmt.setLong(5, solicitacao.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new IllegalStateException("Solicitação com id " + solicitacao.getId() + " não encontrada");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar solicitação: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void remover(Solicitacao solicitacao) {
        String sql = "DELETE FROM solicitacao WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, solicitacao.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover solicitação: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    private Solicitacao mapearSolicitacao(ResultSet rs) throws SQLException {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId(rs.getLong("id"));
        solicitacao.setCaronaId(rs.getLong("carona_id"));
        solicitacao.setUsuarioId(rs.getLong("usuario_id"));
        solicitacao.setDataSolicitacao(rs.getTimestamp("data_solicitacao").toLocalDateTime());
        solicitacao.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));
        return solicitacao;
    }
}