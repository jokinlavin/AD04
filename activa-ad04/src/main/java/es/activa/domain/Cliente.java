package es.activa.domain;

import jakarta.persistence.*;

// Mapeo ORM: Tabla cliente
@Entity @Table(name = "cliente")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nombre", nullable = false, length = 100) private String nombre;
    @Column(name = "email", nullable = false, length = 150, unique = true) private String email;
    protected Cliente() {}
    public Cliente(String nombre, String email) { this.nombre = nombre; this.email = email; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}
