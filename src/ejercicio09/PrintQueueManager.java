package ejercicio09;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Gestor de la cola de impresión administrativa.
 *
 * Dos niveles de seguridad ante concurrencia, que son cosas distintas:
 *  1) Creación de la instancia: se resuelve con el idiom "holder" (la JVM garantiza que la clase
 *     interna se inicializa una sola vez, de forma segura).
 *  2) Operaciones sobre la cola: se resuelven con métodos synchronized, porque el holder
 *     no protege el estado interno del objeto.
 */
public class PrintQueueManager {

    // Estado compartido: todos los accesos se hacen dentro de métodos synchronized
    private final Queue<PrintJob> queue = new ArrayDeque<>();
    private final Set<String> knownIds = new HashSet<>();
    private final List<String> enqueueOrder = new ArrayList<>();
    private final List<PrintJob> processedJobs = new ArrayList<>();

    // Constructor privado
    private PrintQueueManager() {
    }

    // La instancia se crea recién cuando se carga esta clase interna (primer getInstance())
    private static class Holder {
        private static final PrintQueueManager INSTANCE = new PrintQueueManager();
    }

    public static PrintQueueManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Encola un documento. Rechaza identificadores repetidos.
     */
    public synchronized void enqueue(PrintJob job) {
        if (job == null) {
            throw new IllegalArgumentException("El documento no puede ser nulo.");
        }
        if (!knownIds.add(job.id())) {
            throw new IllegalArgumentException("Ya existe un documento con el identificador " + job.id() + ".");
        }
        queue.add(job);
        enqueueOrder.add(job.id());
    }

    /**
     * Procesa (simula imprimir) el documento más antiguo de la cola.
     * Cada documento se extrae una sola vez, por lo que se procesa exactamente una vez.
     */
    public synchronized Optional<PrintJob> processNext() {
        PrintJob job = queue.poll();
        if (job == null) {
            return Optional.empty();
        }
        System.out.println("[Impresora] Imprimiendo " + job);
        processedJobs.add(job);
        return Optional.of(job);
    }

    // Procesa todos los documentos pendientes y devuelve la cantidad procesada
    public synchronized int processAll() {
        int count = 0;
        while (processNext().isPresent()) {
            count++;
        }
        return count;
    }

    public synchronized int getPendingCount() {
        return queue.size();
    }

    // Copia inmodificable de los trabajos procesados, en orden de procesamiento
    public synchronized List<PrintJob> getProcessedJobs() {
        return List.copyOf(processedJobs);
    }

    // Copia inmodificable de los identificadores en el orden en que fueron encolados
    public synchronized List<String> getEnqueueOrder() {
        return List.copyOf(enqueueOrder);
    }
}
