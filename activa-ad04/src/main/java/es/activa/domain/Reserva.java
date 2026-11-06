package es.activa.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Mapeo ORM: Tabla reserva
@Entity @Table(name = "reserva")
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "fecha_reserva", nullable = false) private LocalDateTime fechaReserva;
    @Column(name = "numero_plazas", nullable = false) private int numeroPlazas;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actividad_id", nullable = false) private Actividad actividad;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    protected Reserva() {}
    public Reserva(Cliente cliente, Actividad actividad, int numeroPlazas) {
        this.cliente = cliente; this.actividad = actividad; this.numeroPlazas = numeroPlazas;
        this.fechaReserva = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public int getNumeroPlazas() { return numeroPlazas; }
    public Actividad getActividad() { return actividad; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
}
