package org.example;

import java.sql.*;
import java.util.*;

public class UPAService {

    public static void mostrarUPAsDisponiveis(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM upa")) {

            System.out.println("\n=== UPAs DISPONÍVEIS ===");
            System.out.printf("%-20s | %-20s | %-10s%n", "UPA", "Tempo Estimado", "Pacientes na fila");
            System.out.println("-------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                double tempo = calcularTempoEsperaProximoPaciente(conn, id);

                int qtdFila = 0;
                try (PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM ticket WHERE upa_id=?")) {
                    pst.setInt(1, id);
                    try (ResultSet rsFila = pst.executeQuery()) { if (rsFila.next()) qtdFila = rsFila.getInt(1); }
                }

                System.out.printf("%-20s | %-20.1f | %-10d%n", nome, tempo, qtdFila);
            }
        }
    }

    public static double[] coordenadasPorRegiao(int regiao) {
        return switch (regiao) {
            case 1 -> new double[]{-23.39, -51.94};
            case 2 -> new double[]{-23.44, -51.94};
            case 3 -> new double[]{-23.43, -51.91};
            case 4 -> new double[]{-23.42, -51.96};
            default -> new double[]{-23.42, -51.93};
        };
    }

    public static int selecionarUPAParaPaciente(Connection conn, double lat, double lon, Scanner sc) throws SQLException {
        List<OpcaoUPA> opcoes = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM upa")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                double tempo = calcularTempoEsperaProximoPaciente(conn, id);
                double distancia = Math.hypot(lat - rs.getDouble("latitude"), lon - rs.getDouble("longitude"));
                opcoes.add(new OpcaoUPA(id, nome, distancia, tempo));
            }
        }

        OpcaoUPA upaRegiao = opcoes.stream().min(Comparator.comparingDouble(o -> o.distancia)).orElse(null);
        List<OpcaoUPA> outras = opcoes.stream().filter(o -> o.id != upaRegiao.id)
                .sorted(Comparator.comparingDouble(o -> o.tempoEspera)).limit(2).toList();

        List<OpcaoUPA> exibidas = new ArrayList<>(); exibidas.add(upaRegiao); exibidas.addAll(outras);

        System.out.println("Escolha a UPA:");
        for (int i = 0; i < exibidas.size(); i++) {
            OpcaoUPA o = exibidas.get(i);
            System.out.printf("%d - %s | Tempo estimado: %.1f min%n", i + 1, o.nome, o.tempoEspera);
        }

        while (true) {
            System.out.print("Opção (1-" + exibidas.size() + "): ");
            try {
                int escolha = Integer.parseInt(sc.nextLine());
                if (escolha >= 1 && escolha <= exibidas.size()) return exibidas.get(escolha - 1).id;
            } catch (Exception ignored) {}
            System.out.println("Opção inválida!");
        }
    }

    public static double calcularTempoEsperaProximoPaciente(Connection conn, int upaId) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT criticidade FROM paciente WHERE upa_id=? ORDER BY criticidade DESC")) {
            pst.setInt(1, upaId);
            try (ResultSet rs = pst.executeQuery()) {
                double tempo = 0;
                while (rs.next()) tempo += switch (rs.getInt("criticidade")) { case 3 -> 30; case 2 -> 20; default -> 10; };
                return tempo / 2.0;
            }
        }
    }

    public static String getUpaNome(Connection conn, int id) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement("SELECT nome FROM upa WHERE id=?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) { if (rs.next()) return rs.getString("nome"); }
        }
        return "Desconhecida";
    }

    public static int gerarTicket(Connection conn, String cpfHash, int upaId) throws SQLException {
        int posicao = 1;
        try (PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM ticket WHERE upa_id=?")) {
            pst.setInt(1, upaId);
            try (ResultSet rs = pst.executeQuery()) { if (rs.next()) posicao = rs.getInt(1) + 1; }
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO ticket (cpf, upa_id, posicao_fila) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, cpfHash); ins.setInt(2, upaId); ins.setInt(3, posicao); ins.executeUpdate();
            try (ResultSet rsKey = ins.getGeneratedKeys()) { if (rsKey.next()) return rsKey.getInt(1); }
        }
        return posicao;
    }

    public static void atualizarFila(Connection conn, int upaId) throws SQLException {
        List<TicketFila> fila = new ArrayList<>();
        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT t.ticket_num, p.criticidade FROM ticket t JOIN paciente p ON t.cpf=p.cpf WHERE t.upa_id=?")) {
            pst.setInt(1, upaId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) fila.add(new TicketFila(rs.getInt("ticket_num"), rs.getInt("criticidade")));
            }
        }

        fila.sort((a,b) -> {
            int cmp = Integer.compare(b.criticidade, a.criticidade);
            return cmp != 0 ? cmp : Integer.compare(a.ticketNum, b.ticketNum);
        });

        int pos = 1;
        for (TicketFila t : fila) {
            try (PreparedStatement upd = conn.prepareStatement("UPDATE ticket SET posicao_fila=? WHERE ticket_num=?")) {
                upd.setInt(1,pos++); upd.setInt(2,t.ticketNum); upd.executeUpdate();
            }
        }
    }

    private static class OpcaoUPA { int id; String nome; double distancia, tempoEspera; OpcaoUPA(int i,String n,double d,double t){id=i;nome=n;distancia=d;tempoEspera=t;} }
    private static class TicketFila { int ticketNum, criticidade; TicketFila(int t,int c){ticketNum=t;criticidade=c;} }
}
