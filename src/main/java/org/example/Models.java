package org.example;

public class Models {
    public static class Paciente {
        public String cpf;
        public String nome;
        public int criticidade;
        public String sintomas;
        public int upaId;

        public Paciente(String cpf, String nome, int criticidade, String sintomas, int upaId) {
            this.cpf = cpf;
            this.nome = nome;
            this.criticidade = criticidade;
            this.sintomas = sintomas;
            this.upaId = upaId;
        }
    }

    public static class UPA {
        public int id;
        public String nome;
        public double latitude;
        public double longitude;

        public UPA(int id, String nome, double latitude, double longitude) {
            this.id = id;
            this.nome = nome;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class Ticket {
        public int ticketNum;
        public String cpf;
        public int upaId;
        public int posicaoFila;

        public Ticket(int ticketNum, String cpf, int upaId, int posicaoFila) {
            this.ticketNum = ticketNum;
            this.cpf = cpf;
            this.upaId = upaId;
            this.posicaoFila = posicaoFila;
        }
    }
}
