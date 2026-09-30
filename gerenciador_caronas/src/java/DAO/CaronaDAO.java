package dao;

import Conexxo.Conexxo;
import model.Carona;
import model.StatusCarona;
import model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CaronaDAO {

    private final Conexxo conexxo;

    public CaronaDAO(Conexxo conexxo) {
        this.conexxo = conexxo;
    }

    public Carona buscarPorId(Carona carona) {
        String sql = "SELECT id, motorista_id, origem, destino, data, horario, vagas_totais, "
                    + "vagas_disponiveis, valor_por_pessoa, status "
                    + "FROM carona WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, carona.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearCarona(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar carona por id: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public List<Carona> listarTodas() {
        String sql = "SELECT id, motorista_id, origem, destino, data, horario, vagas_totais, "
                    + "vagas_disponiveis, valor_por_pessoa, status "
                    + "FROM carona ORDER BY data, horario";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Carona> caronas = new ArrayList<>();
            while (rs.next()) {
                caronas.add(mapearCarona(rs));
            }
            return caronas;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar caronas: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public List<Carona> listarPorMotorista(Usuario motorista) {
        String sql = "SELECT id, motorista_id, origem, destino, data, horario, vagas_totais, "
                    + "vagas_disponiveis, valor_por_pessoa, status "
                    + "FROM carona WHERE motorista_id = ? ORDER BY data, horario";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, motorista.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Carona> caronas = new ArrayList<>();
                while (rs.next()) {
                    caronas.add(mapearCarona(rs));
                }
                return caronas;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar caronas do motorista: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void salvar(Carona carona) {
        String sql = "INSERT INTO carona (motorista_id, origem, destino, data, horario, vagas_totais, "
                    + "vagas_disponiveis, valor_por_pessoa, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, carona.getMotoristaId());
            stmt.setString(2, carona.getOrigem());
            stmt.setString(3, carona.getDestino());
            stmt.setDate(4, Date.valueOf(carona.getData()));
            stmt.setTime(5, Time.valueOf(carona.getHorario()));
            stmt.setInt(6, carona.getVagasTotais());
            stmt.setInt(7, carona.getVagasDisponiveis());
            stmt.setDouble(8, carona.getValorPorPessoa());
            stmt.setString(9, carona.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    carona.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar carona: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void atualizar(Carona carona) {
        String sql = "UPDATE carona "
                    + "SET motorista_id = ?, origem = ?, destino = ?, data = ?, horario = ?, vagas_totais = ?, "
                    + "vagas_disponiveis = ?, valor_por_pessoa = ?, status = ? "
                    + "WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, carona.getMotoristaId());
            stmt.setString(2, carona.getOrigem());
            stmt.setString(3, carona.getDestino());
            stmt.setDate(4, Date.valueOf(carona.getData()));
            stmt.setTime(5, Time.valueOf(carona.getHorario()));
            stmt.setInt(6, carona.getVagasTotais());
            stmt.setInt(7, carona.getVagasDisponiveis());
            stmt.setDouble(8, carona.getValorPorPessoa());
            stmt.setString(9, carona.getStatus().name());
            stmt.setLong(10, carona.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new IllegalStateException("Carona com id " + carona.getId() + " não encontrada");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar carona: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void remover(Carona carona) {
        String sql = "DELETE FROM carona WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, carona.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover carona: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    private Carona mapearCarona(ResultSet rs) throws SQLException {
        long motoristaId = rs.getLong("motorista_id");
        return Carona.getBuilder()
                .id(rs.getLong("id"))
                .motoristaId(rs.wasNull() ? null : motoristaId)
                .origem(rs.getString("origem"))
                .destino(rs.getString("destino"))
                .data(rs.getDate("data").toLocalDate())
                .horario(rs.getTime("horario").toLocalTime())
                .vagasTotais(rs.getInt("vagas_totais"))
                .vagasDisponiveis(rs.getInt("vagas_disponiveis"))
                .valorPorPessoa(rs.getDouble("valor_por_pessoa"))
                .status(StatusCarona.valueOf(rs.getString("status")))
                .build();
    }
}