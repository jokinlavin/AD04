package es.activa.service;

import es.activa.config.Persistencia;
import es.activa.domain.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class ActivaService {
    private final Persistencia persistencia;

    public ActivaService(Persistencia persistencia) {
        this.persistencia = persistencia;
    }

    private static String texto(String valor, String campo, int max) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty() || limpio.length() > max) {
            throw new IllegalArgumentException(campo + " debe contener entre 1 y " + max + " caracteres.");
        }
        return limpio;
    }

    private static void positivo(int n) {
        if (n <= 0) throw new IllegalArgumentException("Las plazas deben ser positivas.");
    }

    private static void idValido(long id) {
        if (id <= 0) throw new IllegalArgumentException("El ID debe ser positivo.");
    }

    private static void deshacer(EntityTransaction tx) {
        if (tx.isActive()) tx.rollback();
    }

    // Creamos una entidad monitor y la hacemos persistente
    // Siempre empleamos transacciones
    public long crearMonitor(String nombre, String especialidad) {
        String n = texto(nombre, "Nombre", 100);
        String e = texto(especialidad, "Especialidad", 100);
        EntityManager em = persistencia.abrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Monitor monitor = new Monitor(n, e);
            em.persist(monitor);
            tx.commit();
            return monitor.getId();
        } catch (RuntimeException ex) {
            deshacer(tx);
            throw ex;
        } finally {
            em.close();
        }
    }

    // Creamos una entidad actividad y la hacemos persistente
    // Siempre empleamos transacciones
    public long crearActividad(String nombre, String localidad, LocalDateTime fecha, int plazas, long monitorId) {
        String n = texto(nombre, "Nombre", 120);
        String l = texto(localidad, "Localidad", 100);
        if (fecha == null) throw new IllegalArgumentException("Fecha obligatoria.");
        positivo(plazas);
        idValido(monitorId);
        EntityManager em = persistencia.abrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Buscamos el monitor
            Monitor monitor = em.find(Monitor.class, monitorId);
            if (monitor == null) throw new IllegalArgumentException("No existe el monitor.");
            Actividad actividad = new Actividad(n, l, fecha, plazas);
            monitor.agregarActividad(actividad);
            em.persist(actividad);
            tx.commit();
            return actividad.getId();
        } catch (RuntimeException ex) {
            deshacer(tx);
            throw ex;
        } finally {
            em.close();
        }
    }

    // Creamos una entidad cliente y la hacemos persistente
    // Siempre empleamos transacciones
    public long crearCliente(String nombre, String email) {
        String n = texto(nombre, "Nombre", 100);
        String correo = texto(email, "Email", 150).toLowerCase(Locale.ROOT);
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Formato de correo incorrecto.");
        }
        EntityManager em = persistencia.abrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Comprobamos que el email es único
            Long existentes = em.createQuery(
                    "select count(c) from Cliente c where lower(c.email) = :correo", Long.class)
                    .setParameter("correo", correo).getSingleResult();
            if (existentes > 0) throw new IllegalArgumentException("El correo ya está registrado.");
            Cliente cliente = new Cliente(n, correo);
            em.persist(cliente);
            tx.commit();
            return cliente.getId();
        } catch (RuntimeException ex) {
            deshacer(tx);
            throw ex;
        } finally {
            em.close();
        }
    }

    // Empleamos una sentencia JPQL para obtener buscar las actividades
    public List<Actividad> buscar(String localidad, LocalDate dia, int plazas) {
        String l = texto(localidad, "Localidad", 100).toLowerCase(Locale.ROOT);
        if (dia == null) throw new IllegalArgumentException("Día obligatorio.");
        positivo(plazas);
        EntityManager em = persistencia.abrir();
        try {
            return em.createQuery(
                    "select a from Actividad a join fetch a.monitor " +
                    "where lower(a.localidad) = :localidad and a.fechaHora >= :inicio " +
                    "and a.fechaHora < :fin and a.plazasDisponibles >= :plazas " +
                    "order by a.fechaHora", Actividad.class)
                    .setParameter("localidad", l)
                    .setParameter("inicio", dia.atStartOfDay())
                    .setParameter("fin", dia.plusDays(1).atStartOfDay())
                    .setParameter("plazas", plazas)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    // Empleamos una sentencia JPQL para obtener buscar TODAS las  actividades
    public List<Actividad> listar() {
        EntityManager em = persistencia.abrir();
        try {
            return em.createQuery(
                    "select a from Actividad a join fetch a.monitor order by a.fechaHora",
                    Actividad.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static class ReservaCreada {
        private final long id;
        private final int restantes;
        public ReservaCreada(long id, int restantes) {
            this.id = id;
            this.restantes = restantes;
        }
        public long getId() { return id; }
        public int getRestantes() { return restantes; }
    }

    // Creamos la reserva
    // Empleamos trnassacciones para asegurar la Integridad de todas las tablas/entidades actulizadas
    public ReservaCreada crearReserva(long clienteId, long actividadId, int plazas) {
        idValido(clienteId);
        idValido(actividadId);
        positivo(plazas);
        EntityManager em = persistencia.abrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Buscamos el cliente
            Cliente cliente = em.find(Cliente.class, clienteId);
            // Buscamos la actividad
            Actividad actividad = em.find(Actividad.class, actividadId);
            if (cliente == null) throw new IllegalArgumentException("No existe el cliente.");
            if (actividad == null) throw new IllegalArgumentException("No existe la actividad.");
            // Actualizo las plazas de la actividad
            actividad.reservar(plazas);
            // Creamos la reserva
            Reserva reserva = new Reserva(cliente, actividad, plazas);
            em.persist(reserva);
            // Nos aseguramos que todos los datos estén actualizados en la BBDD
            em.flush();
            tx.commit();
            return new ReservaCreada(reserva.getId(), actividad.getPlazasDisponibles());
        } catch (RuntimeException ex) {
            deshacer(tx);
            throw ex;
        } finally {
            em.close();
        }
    }

    // Clase auxiliar para guardar los datos de la cancelación
    public static class Cancelacion {
        private final int recuperadas;
        private final int disponibles;
        public Cancelacion(int recuperadas, int disponibles) {
            this.recuperadas = recuperadas;
            this.disponibles = disponibles;
        }
        public int getRecuperadas() { return recuperadas; }
        public int getDisponibles() { return disponibles; }
    }

    // Método que cancela la reserva
    // Empleamos transacciones
    public Cancelacion cancelarReserva(long reservaId) {
        idValido(reservaId);
        EntityManager em = persistencia.abrir();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reserva reserva = em.find(Reserva.class, reservaId);
            if (reserva == null) throw new IllegalArgumentException("No existe esa reserva.");
            Actividad actividad = reserva.getActividad();
            int plazas = reserva.getNumeroPlazas();
            // Devilvemos las plazas a la activiadad
            actividad.devolver(plazas);
            // Borramos la reserva
            em.remove(reserva);
            // Nos aseguramos que todos los datos estén actualizados en la BBDD
            em.flush();
            tx.commit();
            return new Cancelacion(plazas, actividad.getPlazasDisponibles());
        } catch (RuntimeException ex) {
            deshacer(tx);
            throw ex;
        } finally {
            em.close();
        }
    }
}
