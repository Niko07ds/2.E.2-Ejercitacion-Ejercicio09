package ejercicio09;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final int THREADS = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Ejercicio 9: Cola compartida de impresión administrativa ===");

        Set<PrintQueueManager> observedInstances = ConcurrentHashMap.newKeySet();
        CountDownLatch startSignal = new CountDownLatch(1);
        Sector[] sectors = Sector.values();
        List<Thread> threads = new ArrayList<>();

        // Diez hilos, cada uno envía tres documentos (30 en total)
        for (int i = 1; i <= THREADS; i++) {
            Sector sector = sectors[(i - 1) % sectors.length];
            Thread thread = new Thread(
                    new SectorPrintClient(i, sector, startSignal, observedInstances),
                    "cliente-" + i);
            threads.add(thread);
            thread.start();
        }

        System.out.println("\n--- Enviando documentos desde " + THREADS + " hilos al mismo tiempo ---");
        startSignal.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        PrintQueueManager manager = PrintQueueManager.getInstance();
        System.out.println("Instancias distintas del gestor observadas por los hilos: " + observedInstances.size());
        System.out.println("Documentos pendientes en la cola: " + manager.getPendingCount());

        System.out.println("\n--- Intento de encolar un identificador repetido ---");
        try {
            manager.enqueue(new PrintJob("DOC-01-1", "Documento repetido", Sector.ARCHIVE, 2));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("Documentos pendientes en la cola: " + manager.getPendingCount());

        System.out.println("\n--- Procesamiento en orden de llegada ---");
        int processed = manager.processAll();

        System.out.println("\n--- Informe de trabajos procesados ---");
        List<PrintJob> jobs = manager.getProcessedJobs();
        Set<String> distinctIds = new HashSet<>();
        Map<Sector, Integer> documentsBySector = new EnumMap<>(Sector.class);
        Map<Sector, Integer> pagesBySector = new EnumMap<>(Sector.class);
        int totalPages = 0;
        for (PrintJob job : jobs) {
            distinctIds.add(job.id());
            documentsBySector.merge(job.sector(), 1, Integer::sum);
            pagesBySector.merge(job.sector(), job.pages(), Integer::sum);
            totalPages += job.pages();
        }
        System.out.println("Documentos procesados: " + processed);
        System.out.println("Identificadores distintos procesados: " + distinctIds.size());
        System.out.println("Documentos pendientes: " + manager.getPendingCount());
        System.out.println("Páginas impresas en total: " + totalPages);
        for (Sector sector : Sector.values()) {
            System.out.println("  " + sector.getDisplayName() + ": "
                    + documentsBySector.getOrDefault(sector, 0) + " documentos, "
                    + pagesBySector.getOrDefault(sector, 0) + " páginas");
        }

        List<String> processedOrder = jobs.stream().map(PrintJob::id).toList();
        boolean sameOrder = processedOrder.equals(manager.getEnqueueOrder());
        System.out.println("¿Se procesó en el mismo orden en que se encolaron? " + (sameOrder ? "Sí" : "No"));
        System.out.println("¿Cada documento se procesó exactamente una vez? "
                + (jobs.size() == distinctIds.size() && processed == THREADS * 3 ? "Sí" : "No"));
    }
}
