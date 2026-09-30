package dao;

import Conexxo.Conexxo;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private final Conexxo conexxo;

    public UsuarioDAO(Conexxo conexxo) {
        this.conexxo = conexxo;
    }

    public Usuario buscarPorId(Usuario usuario) {
        String sql = "SELECT id, nome, cpf, email, telefone, senha, foto_perfil "
                    + "FROM usuario WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, usuario.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public Usuario buscarPorCpf(Usuario usuario) {
        String sql = "SELECT id, nome, cpf, email, telefone, senha, foto_perfil "
                    + "FROM usuario WHERE cpf = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getCpf());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por cpf: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, cpf, email, telefone, senha, foto_perfil) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getSenha());
            stmt.setString(6, usuario.getFotoPerfil());

            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario "
                    + "SET nome = ?, cpf = ?, email = ?, telefone = ?, senha = ?, foto_perfil = ? "
                    + "WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getSenha());
            stmt.setString(6, usuario.getFotoPerfil());
            stmt.setLong(7, usuario.getId());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new IllegalStateException("Usuário com id " + usuario.getId() + " não encontrado");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    public void remover(Usuario usuario) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        Connection conexao = Conexxo.getConexao();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, usuario.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover usuário: " + e.getMessage(), e);
        } finally {
            conexxo.fecharConexao(conexao);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setFotoPerfil(rs.getString("foto_perfil"));
        return usuario;
    }
}