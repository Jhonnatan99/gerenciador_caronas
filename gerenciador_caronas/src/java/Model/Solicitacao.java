package model;

import java.time.LocalDateTime;

public class Solicitacao {

    private Long id;
    private Long caronaId;
    private Long usuarioId;
    private LocalDateTime dataSolicitacao;
    private StatusSolicitacao status;

    public Solicitacao() {
    }

    public Solicitacao(Carona carona, Usuario usuario) {
        this.caronaId = carona.getId();
        this.usuarioId = usuario.getId();
        this.dataSolicitacao = LocalDateTime.now();
        this.status = StatusSolicitacao.PENDENTE;
    }

    public void aceitar() {
        if (this.status != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Só é possível aceitar solicitações pendentes");
        }
        this.status = StatusSolicitacao.ACEITA;
    }

    public void recusar() {
        if (this.status != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Só é possível recusar solicitações pendentes");
        }
        this.status = StatusSolicitacao.RECUSADA;
    }

    public void cancelar() {
        if (this.status == StatusSolicitacao.RECUSADA) {
            throw new IllegalStateException("Não é possível cancelar uma solicitação já recusada");
        }
        this.status = StatusSolicitacao.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaronaId() {
        return caronaId;
    }

    public void setCaronaId(Long caronaId) {
        this.caronaId = caronaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
}