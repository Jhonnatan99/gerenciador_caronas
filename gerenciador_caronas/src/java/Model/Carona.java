package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Carona {

    private Long id;
    private Long motoristaId;
    private String origem;
    private String destino;
    private LocalDate data;
    private LocalTime horario;
    private int vagasTotais;
    private int vagasDisponiveis;
    private double valorPorPessoa;
    private StatusCarona status;

    private Carona() {
    }

    public static CaronaBuilder getBuilder() {
        return new CaronaBuilder();
    }

    public void atualizarVagas(int qtd) {
        if (qtd > this.vagasDisponiveis) {
            throw new IllegalStateException("Não há vagas suficientes disponíveis");
        }
        this.vagasDisponiveis -= qtd;
        if (this.vagasDisponiveis == 0) {
            this.status = StatusCarona.LOTADA;
        }
    }

    public void adicionarPassageiro(Usuario usuario) {
        if (this.status != StatusCarona.DISPONIVEL) {
            throw new IllegalStateException("Carona não está disponível para novos passageiros");
        }
        atualizarVagas(1);
    }

    public void removerPassageiro(Usuario usuario) {
        if (this.vagasDisponiveis >= this.vagasTotais) {
            throw new IllegalStateException("Não há passageiro para remover");
        }
        this.vagasDisponiveis++;
        if (this.status == StatusCarona.LOTADA) {
            this.status = StatusCarona.DISPONIVEL;
        }
    }

    public void alterarStatus(StatusCarona status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMotoristaId() {
        return motoristaId;
    }

    public void setMotoristaId(Long motoristaId) {
        this.motoristaId = motoristaId;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public int getVagasTotais() {
        return vagasTotais;
    }

    public void setVagasTotais(int vagasTotais) {
        this.vagasTotais = vagasTotais;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public double getValorPorPessoa() {
        return valorPorPessoa;
    }

    public void setValorPorPessoa(double valorPorPessoa) {
        this.valorPorPessoa = valorPorPessoa;
    }

    public StatusCarona getStatus() {
        return status;
    }

    public void setStatus(StatusCarona status) {
        this.status = status;
    }

    public static class CaronaBuilder {

        private final Carona carona;

        private CaronaBuilder() {
            this.carona = new Carona();
        }

        public CaronaBuilder id(Long id) {
            carona.id = id;
            return this;
        }

        public CaronaBuilder motoristaId(Long motoristaId) {
            carona.motoristaId = motoristaId;
            return this;
        }

        public CaronaBuilder origem(String origem) {
            carona.origem = origem;
            return this;
        }

        public CaronaBuilder destino(String destino) {
            carona.destino = destino;
            return this;
        }

        public CaronaBuilder data(LocalDate data) {
            carona.data = data;
            return this;
        }

        public CaronaBuilder horario(LocalTime horario) {
            carona.horario = horario;
            return this;
        }

        public CaronaBuilder vagas(int vagas) {
            carona.vagasTotais = vagas;
            carona.vagasDisponiveis = vagas;
            return this;
        }

        public CaronaBuilder vagasTotais(int vagasTotais) {
            carona.vagasTotais = vagasTotais;
            return this;
        }

        public CaronaBuilder vagasDisponiveis(int vagasDisponiveis) {
            carona.vagasDisponiveis = vagasDisponiveis;
            return this;
        }

        public CaronaBuilder valorPorPessoa(double valor) {
            carona.valorPorPessoa = valor;
            return this;
        }

        public CaronaBuilder status(StatusCarona status) {
            carona.status = status;
            return this;
        }

        public Carona build() {
            if (carona.status == null) {
                carona.status = StatusCarona.DISPONIVEL;
            }
            return carona;
        }
    }
}