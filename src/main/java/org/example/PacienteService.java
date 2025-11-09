package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class PacienteService {

    public static void inserirPacienteTeste(Statement stmt, String cpf, String nome, int crit, String sintomas, int upaId) throws SQLException {
        stmt.execute("MERGE INTO paciente KEY(cpf) VALUES ('" + Banco.criptografarCPF(cpf) + "','" + nome + "'," + crit + ",'" + sintomas + "'," + upaId + ")");
    }

    public static void adicionarPaciente(Connection conn, Scanner sc) throws SQLException {
        // Consentimento
        System.out.print("Consentimento LGPD (s/n): ");
        while (true) {
            String consent = sc.nextLine().trim().toLowerCase();
            if (consent.equals("s")) break;
            if (consent.equals("n")) { System.out.println("Cadastro cancelado."); return; }
            System.out.print("Digite apenas 's' ou 'n': ");
        }

        // Dados do paciente
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        String cpf;
        while (true) {
            System.out.print("CPF (11 números): ");
            cpf = sc.nextLine().trim();
            if (cpf.matches("\\d{11}")) break;
            System.out.println("CPF inválido!");
        }
        String cpfHash = Banco.criptografarCPF(cpf);

        // Verifica duplicidade
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM paciente WHERE cpf=?")) {
            check.setString(1, cpfHash);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) { System.out.println("CPF já cadastrado!"); return; }
            }
        }

        // Criticidade
        int crit = 1;
        crit = perguntarCrit(sc,"Dor intensa no peito?", crit,3);
        crit = perguntarCrit(sc,"Dificuldade para respirar?", crit,3);
        crit = perguntarCrit(sc,"Perda de consciência ou tontura intensa?", crit,3);
        crit = perguntarCrit(sc,"Sangramento abundante?", crit,3);
        crit = perguntarCrit(sc,"Febre alta ou persistente?", crit,2);
        crit = perguntarCrit(sc,"Desmaios recentes ou convulsões?", crit,2);
        crit = perguntarCrit(sc,"Dor abdominal intensa ou persistente?", crit,2);

        // Sintomas obrigatórios
        String sintomas;
        while (true) {
            System.out.print("Descreva seus sintomas: ");
            sintomas = sc.nextLine().trim();
            if (!sintomas.isEmpty()) break;
            System.out.println("⚠️ Informe pelo menos um sintoma.");
        }

        // Região e UPA
        int regiao = selecionarRegiaoPaciente(sc);
        double[] coords = UPAService.coordenadasPorRegiao(regiao);
        int upaId = UPAService.selecionarUPAParaPaciente(conn, coords[0], coords[1], sc);

        try (PreparedStatement pst = conn.prepareStatement(
                "INSERT INTO paciente (cpf, nome, criticidade, sintomas, upa_id) VALUES (?, ?, ?, ?, ?)")) {
            pst.setString(1, cpfHash);
            pst.setString(2, nome);
            pst.setInt(3, crit);
            pst.setString(4, sintomas);
            pst.setInt(5, upaId);
            pst.executeUpdate();
        }

        int ticketNum = UPAService.gerarTicket(conn, cpfHash, upaId);
        UPAService.atualizarFila(conn, upaId);

        System.out.println("✅ Cadastrado na UPA " + UPAService.getUpaNome(conn,upaId));
        System.out.println("Ticket: #" + ticketNum);
        System.out.println("Sintomas: " + sintomas);
    }

    private static int perguntarCrit(Scanner sc, String pergunta, int critAtual, int novoCrit) {
        while (true) {
            System.out.print(pergunta + " (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s")) return Math.max(critAtual, novoCrit);
            if (resp.equals("n")) return critAtual;
            System.out.println("Digite apenas 's' ou 'n'.");
        }
    }

    public static void cancelarPaciente(Connection conn, Scanner sc) throws SQLException {
        System.out.print("CPF para cancelar: ");
        String cpfHash = Banco.criptografarCPF(sc.nextLine().trim());

        int upaId = -1;
        try (PreparedStatement pst = conn.prepareStatement("SELECT upa_id FROM paciente WHERE cpf=?")) {
            pst.setString(1, cpfHash);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) upaId = rs.getInt("upa_id");
                else { System.out.println("Paciente não encontrado!"); return; }
            }
        }

        try (PreparedStatement delTicket = conn.prepareStatement("DELETE FROM ticket WHERE cpf=?")) {
            delTicket.setString(1, cpfHash); delTicket.executeUpdate();
        }

        try (PreparedStatement delPaciente = conn.prepareStatement("DELETE FROM paciente WHERE cpf=?")) {
            delPaciente.setString(1, cpfHash); delPaciente.executeUpdate();
        }

        UPAService.atualizarFila(conn, upaId);
        System.out.println("Consulta cancelada!");
    }

    private static int selecionarRegiaoPaciente(Scanner sc) {
        System.out.println("1 - Norte | 2 - Sul | 3 - Leste | 4 - Oeste");
        while (true) {
            System.out.print("Número da região: ");
            String entrada = sc.nextLine().trim();
            if (entrada.matches("[1-4]")) return Integer.parseInt(entrada);
        }
    }
}
