package org.example;

import java.sql.*;
import java.util.*;

public class TicketService {

    public static int gerarTicket(Connection conn, String cpfHash, int upaId) throws SQLException {
        int posicao = 1;
        try (PreparedStatement pst = conn.prepareStatement("SELECT COUNT(*) FROM ticket WHERE upa_id=?")) {
            pst.setInt(1, upaId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) posicao = rs.getInt(1) + 1;
            }
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO ticket (cpf, upa_id, posicao_fila) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ins.setString(1, cpfHash);
            ins.setInt(2, upaId);
            ins.setInt(3, posicao);
            ins.executeUpdate();
            try (ResultSet rsKey = ins.getGeneratedKeys()) {
                if (rsKey.next()) return rsKey.getInt(1);
            }
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
            if (cmp != 0) return cmp;
            return Integer.compare(a.ticketNum, b.ticketNum);
        });

        int pos = 1;
        for (TicketFila t : fila) {
            try (PreparedStatement upd = conn.prepareStatement("UPDATE ticket SET posicao_fila=? WHERE ticket_num=?")) {
                upd.setInt(1, pos++);
                upd.setInt(2, t.ticketNum);
                upd.executeUpdate();
            }
        }
    }

    public static double calcularTempoEsperaProximoPaciente(Connection conn, int upaId) throws SQLException {
        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT criticidade FROM paciente WHERE upa_id=? ORDER BY criticidade DESC")) {
            pst.setInt(1, upaId);
            double tempo = 0;
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tempo += switch (rs.getInt("criticidade")) {
                        case 3 -> 30;
                        case 2 -> 20;
                        default -> 10;
                    };
                }
            }
            return tempo / 2.0;
        }
    }

    private static class TicketFila { int ticketNum, criticidade; TicketFila(int t, int c){ticketNum=t;criticidade=c;} }
}
