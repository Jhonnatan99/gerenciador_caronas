package Conexxo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexxo {

    private static final String URL = "jdbc:mysql://localhost:3306/gerenciador_caronas";
    private static final String USUARIO = "root";
    private static final String SENHA = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver do MySQL (mysql-connector-j) não encontrado no classpath. "
                    + "Confirme se o .jar está em WEB-INF/lib. Detalhe: " + e.getMessage());
        }
    }

    public static Connection getConexao() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar com o banco de dados: " + e.getMessage());
            return null;
        }
    }

    public void fecharConexao(Connection conexao) {
        if (conexao == null) {
            return;
        }
        try {
            if (!conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao fechar a conexão: " + e.getMessage(), e);
        }
    }
}