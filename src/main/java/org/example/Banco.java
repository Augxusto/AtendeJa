package org.example;

import java.sql.*;
import java.security.MessageDigest;

public class Banco {

    private static final String JDBC_URL = "jdbc:h2:./upa_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void inicializarBanco(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Criação de tabelas
            stmt.execute("CREATE TABLE IF NOT EXISTS upa (id INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100), latitude DOUBLE, longitude DOUBLE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS paciente (cpf VARCHAR(64) PRIMARY KEY, nome VARCHAR(100), criticidade INT, sintomas VARCHAR(255), upa_id INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ticket (ticket_num INT AUTO_INCREMENT PRIMARY KEY, cpf VARCHAR(64), upa_id INT, posicao_fila INT)");

            // Inserção de UPAs
            stmt.execute("MERGE INTO upa KEY(id) VALUES " +
                    "(1,'UPA Centro', -23.4200, -51.9350)," +
                    "(2,'UPA Norte', -23.3900, -51.9300)," +
                    "(3,'UPA Sul', -23.4500, -51.9400)," +
                    "(4,'UPA Leste', -23.4200, -51.9100)," +
                    "(5,'UPA Oeste', -23.4200, -51.9600)");

            // Pacientes de teste
            PacienteService.inserirPacienteTeste(stmt, "11111111111","João Silva",3,"Dores no peito",1);
            PacienteService.inserirPacienteTeste(stmt, "22222222222","Maria Souza",2,"Febre e tosse",2);
            PacienteService.inserirPacienteTeste(stmt, "33333333333","Carlos Lima",1,"Gripe leve",3);
            PacienteService.inserirPacienteTeste(stmt, "44444444444","Ana Costa",2,"Dores abdominais",4);
            PacienteService.inserirPacienteTeste(stmt, "55555555555","Pedro Alves",3,"Fratura no braço",5);
        }
    }

    public static String criptografarCPF(String cpf) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(cpf.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return cpf; }
    }
}
