package org.example;

import java.util.Scanner;
import java.security.MessageDigest;

public class Utils {

    public static String criptografarCPF(String cpf) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(cpf.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return cpf; }
    }

    public static int perguntaCrit(Scanner sc, String pergunta, int critAtual, int novoCrit) {
        while (true) {
            System.out.print(pergunta + " (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s")) return Math.max(critAtual, novoCrit);
            if (resp.equals("n")) return critAtual;
            System.out.println("Digite apenas 's' ou 'n'.");
        }
    }

    public static int selecionarRegiaoPaciente(Scanner sc) {
        System.out.println("\n1 - Norte | 2 - Sul | 3 - Leste | 4 - Oeste");
        while (true) {
            System.out.print("Número da sua região: ");
            String entrada = sc.nextLine().trim();
            if (entrada.matches("[1-4]")) return Integer.parseInt(entrada);
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
}
