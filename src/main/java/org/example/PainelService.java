package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class PainelService {

    // Exibe informações do paciente (painel)
    public static void exibirInformacoesPaciente(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Digite seu CPF: ");
        String cpfHash = Banco.criptografarCPF(sc.nextLine().trim());

        String query = """
            SELECT p.nome, p.sintomas, t.ticket_num, t.posicao_fila, u.nome AS upa_nome
            FROM paciente p
            JOIN ticket t ON p.cpf = t.cpf
            JOIN upa u ON p.upa_id = u.id
            WHERE p.cpf = ?
        """;

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, cpfHash);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String sintomas = rs.getString("sintomas");
                    int ticket = rs.getInt("ticket_num");
                    int posicao = rs.getInt("posicao_fila");
                    String upa = rs.getString("upa_nome");

                    System.out.println("\n=== MINHAS INFORMAÇÕES ===");
                    System.out.println("Nome: " + nome);
                    System.out.println("UPA designada: " + upa);
                    System.out.println("Número do ticket: #" + ticket);
                    System.out.println("Sua posição na fila: " + posicao);
                    System.out.println("Sintomas: " + sintomas);
                    System.out.println("============================");
                } else {
                    System.out.println("Paciente não encontrado ou CPF incorreto.");
                }
            }
        }
    }
}
