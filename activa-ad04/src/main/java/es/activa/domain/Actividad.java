package es.activa.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Mapeo ORM: Tabla actividad
@Entity @Table(name = "actividad")
public class Actividad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nombre", nullable = false, length = 120) private String nombre;
    @Column(name = "localidad", nullable = false, length = 100) private String localidad;
    @Column(name = "fecha_hora", nullable = false) private LocalDateTime fechaHora;
    @Column(name = "plazas_totales", nullable = false) private int plazasTotales;
    @Column(name = "plazas_disponibles", nullable = false) private int plazasDisponibles;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "monitor_id", nullable = false) private Monitor monitor;
    protected Actividad() {}
    public Actividad(String nombre, String localidad, LocalDateTime fechaHora, int plazasTotales) {
        this.nombre = nombre; this.localidad = localidad; this.fechaHora = fechaHora;
        this.plazasTotales = plazasTotales; this.plazasDisponibles = plazasTotales;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getPlazasTotales() { return plazasTotales; }
    public int getPlazasDisponibles() { return plazasDisponibles; }
    public Monitor getMonitor() { return monitor; }
    public void setMonitor(Monitor nuevo) {
        if (monitor == nuevo) return;
        if (monitor != null) monitor.getActividades().remove(this);
        monitor = nuevo;
        if (nuevo != null && !nuevo.getActividades().contains(this)) nuevo.getActividades().add(this);
    }
    public void reservar(int plazas) {
        if (plazas <= 0 || plazas > plazasDisponibles) throw new IllegalArgumentException("No hay plazas suficientes o la cantidad no es válida.");
        plazasDisponibles -= plazas;
    }
    public void devolver(int plazas) {
        if (plazas <= 0 || plazasDisponibles + plazas > plazasTotales) throw new IllegalArgumentException("No se pueden devolver esas plazas.");
        plazasDisponibles += plazas;
    }
}
