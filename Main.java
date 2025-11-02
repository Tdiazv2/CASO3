import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        if (args.length < 1) {
            System.err.println("Uso: java MainSimulation <ruta_config.txt>");
            return;
        }

        // 1. Leer archivo de configuración
        String configPath = args[0];
        int numClients;
        int msgsPerClient;
        int numSpamFilters;
        int numDeliveryServers;
        int inputCapacity;
        int deliveryCapacity;

        try (Scanner sc = new Scanner(new File(configPath))) {
            numClients          = sc.nextInt();
            msgsPerClient       = sc.nextInt();
            numSpamFilters      = sc.nextInt();
            numDeliveryServers  = sc.nextInt();
            inputCapacity       = sc.nextInt();
            deliveryCapacity    = sc.nextInt();
        } catch (FileNotFoundException e) {
            System.err.println("No pude abrir el archivo de config: " + configPath);
            return;
        }

        System.out.println("=== Configuración cargada ===");
        System.out.println("Clientes emisores: " + numClients);
        System.out.println("Mensajes por cliente: " + msgsPerClient);
        System.out.println("Filtros de spam: " + numSpamFilters);
        System.out.println("Servidores de entrega: " + numDeliveryServers);
        System.out.println("Capacidad buzón entrada: " + inputCapacity);
        System.out.println("Capacidad buzón entrega: " + deliveryCapacity);
        System.out.println("=============================\n");

        // 2. Crear recursos compartidos
        InputMailbox inputMailbox = new InputMailbox(inputCapacity);
        QuarantineMailbox quarantineMailbox = new QuarantineMailbox();
        DeliveryMailbox deliveryMailbox = new DeliveryMailbox(deliveryCapacity);
        GlobalControl globalControl = new GlobalControl(numClients);

        // 3. Crear hilos
        List<Thread> threads = new ArrayList<>();

        // Clientes emisores
        for (int i = 0; i < numClients; i++) {
            Thread t = new ClientSender(i, msgsPerClient, inputMailbox);
            threads.add(t);
        }

        // Filtros de spam
        for (int i = 0; i < numSpamFilters; i++) {
            Thread t = new SpamFilter(i, inputMailbox, quarantineMailbox, deliveryMailbox, globalControl);
            threads.add(t);
        }

        // Manejador de cuarentena (solo uno en el enunciado)
        Thread quarantineManager = new QuarantineManager(quarantineMailbox, deliveryMailbox, globalControl);
        threads.add(quarantineManager);

        // Servidores de entrega
        for (int i = 0; i < numDeliveryServers; i++) {
            Thread t = new DeliveryServer(i, deliveryMailbox);
            threads.add(t);
        }

        // 4. Lanzar hilos
        for (Thread t : threads) {
            t.start();
        }

        // 5. Esperar a que todos terminen
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("\nSimulación finalizada correctamente.");
        System.out.println("Todos los hilos terminaron y los buzones deberían estar vacíos.");
    }
}
