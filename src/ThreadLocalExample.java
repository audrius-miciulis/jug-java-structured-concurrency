import java.util.concurrent.Executors;

public class ThreadLocalExample {

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    static void main(String[] args) throws InterruptedException {
        try (var service = Executors.newFixedThreadPool(5)) {
            for (int i = 0; i < 5; i++) {
                service.submit(() -> {
                    threadLocal.set(Thread.currentThread().getName());
                    System.out.println("First loop: " + threadLocal.get());
                    //threadLocal.remove();
                });
            }

            Thread.sleep(100);
            System.out.println();

            for (int i = 0; i < 5; i++) {
                service.submit(() -> System.out.println("Second loop: " + threadLocal.get()));
            }
        }
    }
}
