package es.activa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class Persistencia implements AutoCloseable {
    private final EntityManagerFactory fabrica;
    public Persistencia() {
        // Conexión a la BBDD
        String usuario = "root";
        String clave = "secret";
        if (usuario == null || usuario.isBlank() || clave == null)
            throw new IllegalStateException("Configura ACTIVA_DB_USER y ACTIVA_DB_PASSWORD (puede estar vacía).");
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", "jdbc:mysql://localhost:3306/dbactiva");
        propiedades.put("jakarta.persistence.jdbc.user", usuario);
        propiedades.put("jakarta.persistence.jdbc.password", clave);
        // Asociamos la unidad de persostencia a la facoria del EntityManager
        fabrica = Persistence.createEntityManagerFactory("activaPU", propiedades);
    }
    // Obtenmos el EntityManager correspondiente a cada operación sobre las entidades
    public EntityManager abrir() { return fabrica.createEntityManager(); }
    // Liberamos la factoria del entityManager
    @Override public void close() { fabrica.close(); }
}
