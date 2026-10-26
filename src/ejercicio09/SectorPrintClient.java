package ejercicio09;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Cliente que simula un hilo de un sector enviando documentos a la cola de impresión.
 */
public class SectorPrintClient implements Runnable {

    private static final int DOCUMENTS_PER_THREAD = 3;

    private final int threadNumber;
    private final Sector sector;
    private final CountDownLatch startSignal;
    private final Set<PrintQueueManager> observedInstances;

    public SectorPrintClient(int threadNumber,
                             Sector sector,
                             CountDownLatch startSignal,
                             Set<PrintQueueManager> observedInstances) {
        this.threadNumber = threadNumber;
        this.sector = sector;
        this.startSignal = startSignal;
        this.observedInstances = observedInstances;
    }

    @Override
    public void run() {
        try {
            // Todos los hilos esperan la señal para arrancar al mismo tiempo
            startSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        for (int k = 1; k <= DOCUMENTS_PER_THREAD; k++) {
            PrintQueueManager manager = PrintQueueManager.getInstance();
            observedInstances.add(manager);
            String id = String.format("DOC-%02d-%d", threadNumber, k);
            String name = "Documento " + sector.getDisplayName() + " " + threadNumber + "." + k;
            int pages = 1 + (threadNumber * DOCUMENTS_PER_THREAD + k) % 10;
            manager.enqueue(new PrintJob(id, name, sector, pages));
        }
    }
}
