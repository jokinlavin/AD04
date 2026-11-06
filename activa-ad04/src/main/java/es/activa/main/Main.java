package es.activa.main;

import es.activa.config.Persistencia;
import es.activa.domain.Actividad;
import es.activa.service.ActivaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        try (Persistencia persistencia = new Persistencia()) {
            ActivaService servicio = new ActivaService(persistencia);
            boolean salir = false;
            while (!salir) {
                System.out.println("\n=== ACTIVA · Gestión de actividades ===");
                System.out.println("1. Crear monitor\n2. Crear actividad\n3. Buscar actividades disponibles\n4. Crear cliente\n5. Crear reserva\n6. Cancelar reserva\n7. Listar actividades\n8. Salir");
                try {
                    switch (entero("Opción: ")) {
                        case 1: monitor(servicio); break;
                        case 2: actividad(servicio); break;
                        case 3: buscar(servicio); break;
                        case 4: cliente(servicio); break;
                        case 5: reservar(servicio); break;
                        case 6: cancelar(servicio); break;
                        case 7: listarActividades(servicio); break;
                        case 8: salir = true; break;
                        default: System.out.println("Opción no válida.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Operación rechazada: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.out.println("Error de persistencia. No se ha confirmado la operación: " + e.getClass().getSimpleName());
                }
            }
            System.out.println("Hasta pronto.");
        } catch (RuntimeException e) {
            System.err.println("No se pudo iniciar la aplicación. Comprueba MySQL, variables de entorno y esquema dbactiva.");
            System.err.println(e.getMessage());
        }
    }
    private static String pedir(String mensaje) { System.out.print(mensaje); return IN.nextLine().trim(); }
    private static int entero(String mensaje) {
        try { return Integer.parseInt(pedir(mensaje)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Introduce un número entero válido."); }
    }
    private static long largo(String mensaje) {
        try { return Long.parseLong(pedir(mensaje)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Introduce un ID numérico válido."); }
    }
    private static LocalDateTime fechaHora() {
        try { return LocalDateTime.parse(pedir("Fecha y hora (dd/MM/yyyy HH:mm): "), FECHA_HORA); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Fecha u hora incorrecta. Formato dd/MM/yyyy HH:mm."); }
    }
    private static LocalDate dia() {
        try { return LocalDate.parse(pedir("Día (dd/MM/yyyy): "), DIA); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Día incorrecto. Formato dd/MM/yyyy."); }
    }
    // Creamos un monitor llamando al metodo creaMonitor de ActivaService
    private static void monitor(ActivaService s) {
        long id = s.crearMonitor(pedir("Nombre: "), pedir("Especialidad: "));
        System.out.println("Monitor creado. ID: " + id);
    }
    // Creamos una actividad llamando al metodo creaMonitor de ActivaService
    private static void actividad(ActivaService s) {
        String nombre = pedir("Nombre: "), localidad = pedir("Localidad: ");
        LocalDateTime fecha = fechaHora();
        int plazas = entero("Plazas totales: ");
        long monitorId = largo("ID del monitor: ");
        long id = s.crearActividad(nombre, localidad, fecha, plazas, monitorId);
        System.out.println("Actividad creada. ID: " + id);
    }
    // Buscamos una actividad por localiidad, fecha y plazas necesarias
    private static void buscar(ActivaService s) {
        String localidad = pedir("Localidad: "); LocalDate fecha = dia();
        int plazas = entero("Plazas necesarias: ");
        // Método auxiliar para mostrar listados
        mostrar(s.buscar(localidad, fecha, plazas), false);
    }
    // Alta de cliente
    private static void cliente(ActivaService s) {
        long id = s.crearCliente(pedir("Nombre: "), pedir("Email: "));
        System.out.println("Cliente creado. ID: " + id);
    }
    // Nueva reserva
    private static void reservar(ActivaService s) {
        long cliente = largo("ID cliente: "), actividad = largo("ID actividad: ");
        int plazas = entero("Número de plazas: ");
        ActivaService.ReservaCreada r = s.crearReserva(cliente, actividad, plazas);
        System.out.println("Reserva confirmada. ID: " + r.getId() + ". Plazas restantes: " + r.getRestantes());
    }
    // Cancelación de reserva
    private static void cancelar(ActivaService s) {
        ActivaService.Cancelacion c = s.cancelarReserva(largo("ID reserva: "));
        System.out.println("Reserva cancelada. Plazas recuperadas: " + c.getRecuperadas() + ". Disponibles: " + c.getDisponibles());
    }
    // Listamos las actividades
    private static void listarActividades(ActivaService s) {
        List<Actividad> actividades = s.listar();
        mostrar(actividades, true);
    }
    // Método auxiliar para listados
    private static void mostrar(List<Actividad> actividades, boolean incluirTotales) {
        if (actividades.isEmpty()) { System.out.println("No hay actividades para mostrar."); return; }
        for (Actividad a : actividades) {
            System.out.printf("ID %d | %s | %s | %s | Monitor: %s | %s%d%n",
                a.getId(), a.getNombre(), a.getLocalidad(), a.getFechaHora().format(FECHA_HORA),
                a.getMonitor().getNombre(), incluirTotales ? "Totales: " + a.getPlazasTotales() + " | Disponibles: " : "Disponibles: ",
                a.getPlazasDisponibles());
        }
    }
}
