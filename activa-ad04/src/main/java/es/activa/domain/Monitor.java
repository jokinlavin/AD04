package es.activa.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Mapeo ORM: Tabla monitor
@Entity @Table(name = "monitor")
public class Monitor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nombre", nullable = false, length = 100) private String nombre;
    @Column(name = "especialidad", nullable = false, length = 100) private String especialidad;
    @OneToMany(mappedBy = "monitor") private List<Actividad> actividades = new ArrayList<>();
    protected Monitor() {}
    public Monitor(String nombre, String especialidad) { this.nombre = nombre; this.especialidad = especialidad; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public List<Actividad> getActividades() { return actividades; }
    public void agregarActividad(Actividad actividad) { actividad.setMonitor(this); }
}
