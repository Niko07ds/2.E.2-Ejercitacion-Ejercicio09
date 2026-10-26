package ejercicio09;

/**
 * Trabajo de impresión: identificador único, nombre del documento, sector y cantidad de páginas.
 */
public record PrintJob(String id, String name, Sector sector, int pages) {

    public PrintJob {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del documento no puede estar vacío.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del documento no puede estar vacío.");
        }
        if (sector == null) {
            throw new IllegalArgumentException("El sector no puede ser nulo.");
        }
        if (pages <= 0) {
            throw new IllegalArgumentException("La cantidad de páginas debe ser mayor a cero.");
        }
        id = id.trim();
        name = name.trim();
    }

    @Override
    public String toString() {
        return id + " '" + name + "' - " + sector.getDisplayName() + ", " + pages + (pages == 1 ? " página" : " páginas");
    }
}
