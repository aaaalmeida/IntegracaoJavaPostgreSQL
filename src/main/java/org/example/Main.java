package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {
    private static boolean checkDatabase(Connection connection, String bancoNome) throws SQLException {
        String query = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, bancoNome);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void createDatabase(Connection connection, String bancoTeste) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE DATABASE " + bancoTeste)) {
            preparedStatement.executeUpdate();
            System.out.println("Banco criado");
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS tabela1 (idTab1 SERIAL PRIMARY KEY, name VARCHAR(50) NOT NULL);";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        }
    }

    private static boolean insert(Connection connection, String nome) throws SQLException {
        String query = String.format("INSERT INTO tabela1 (name) VALUES ('%s');", nome);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if (ps.executeUpdate() > 0) {
                return true;
            } else return false;
        }
    }

    private static boolean delete(Connection connection, int id) throws SQLException {
        String query = "DELETE FROM tabela1 WHERE idTab1 = " + id + " RETURNING *;";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cod = rs.getInt("idTab1");
                    String nome = rs.getString("name");
                    System.out.println(String.format("ID: %d Nome: %s", cod, nome));
                    return true;
                } else {
                    System.out.println("Exclusão não efetuada");
                    return false;
                }
            }
        }
    }

    public static String consult(Connection connection, int id) throws SQLException {
        String query = String.format("SELECT * FROM tabela1 WHERE idTab1 = %d;", id);
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return String.format("ID: %d Nome: %s", rs.getInt("idTab1"), rs.getString("name"));
                }
                else return null;
            }
        }
    }
    public static boolean update(Connection connection, int id, String novoNome) throws SQLException {
        String query = String.format("UPDATE tabela1 SET name = '%s' where idTab1 = %d RETURNING *;", novoNome, id);
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cod = rs.getInt("idTab1");
                    String nome = rs.getString("name");
                    System.out.println(String.format("ID: %d Nome: %s", cod, nome));
                    return true;
                }
                else return false;
            }
        }
    }

    public static void main(String[] args) {
        // Informações de conexão com o banco de dados
        String bancoNome = "banco1";
        String url = "jdbc:postgresql://localhost:5432/"; // URL do servidor PostgreSQL
        String user = "postgres";
        String password = "postgres";

        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);

            if (!checkDatabase(connection, bancoNome)) {
                createDatabase(connection, bancoNome);
                connection = DriverManager.getConnection(url + bancoNome, user, password);
                createTables(connection);
            } else {
                System.err.println("Banco ja existe");
            }


            insert(connection, "Arthur");
            insert(connection, "Esther");
            insert(connection, "Débora");
            insert(connection, "Isaac");

            System.out.println(consult(connection, 2));
            update(connection, 2, "Sarah");
            System.out.println(consult(connection, 2));

            delete(connection, 3);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
