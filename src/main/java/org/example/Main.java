package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Connection conn = Banco.getConnection(); Scanner sc = new Scanner(System.in)) {
            Banco.inicializarBanco(conn);
            boolean rodando = true;

            while (rodando) {
                System.out.println("\n==== PORTAL DO PACIENTE - UPA DIGITAL ====");
                System.out.println("1 - Cadastrar / Pegar Ticket");
                System.out.println("2 - Cancelar consulta");
                System.out.println("3 - Ver UPAs Disponíveis");
                System.out.println("4 - Meu Painel");
                System.out.println("5 - Sair");
                System.out.print("Escolha uma opção: ");
                String opcao = sc.nextLine().trim();

                switch (opcao) {
                    case "1" -> PacienteService.adicionarPaciente(conn, sc);
                    case "2" -> PacienteService.cancelarPaciente(conn, sc);
                    case "3" -> UPAService.mostrarUPAsDisponiveis(conn);
                    case "4" -> PainelService.exibirInformacoesPaciente(conn, sc);
                    case "5" -> rodando = false;
                    default -> System.out.println("Opção inválida!");
                }
            }
            System.out.println("Encerrando sistema...");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
