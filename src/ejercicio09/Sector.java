package ejercicio09;

/**
 * Sectores del hospital que envían documentos a la impresora administrativa.
 */
public enum Sector {
    RECEPTION("Recepción"),
    BILLING("Facturación"),
    ARCHIVE("Archivo");

    private final String displayName;

    Sector(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
