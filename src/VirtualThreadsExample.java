public class VirtualThreadsExample {

    static void main(String[] args) throws InterruptedException {
        System.out.println("Is started on virtual thread: " + Thread.currentThread().isVirtual());

        Thread.startVirtualThread(() -> System.out.println("Is now running on virtual thread: " + Thread.currentThread().isVirtual()));

        var threadBuilder = Thread
                .ofVirtual()
                .name("Virtual Thread #2")
                .uncaughtExceptionHandler((t, ex) -> {
                    System.out.println("Uncaught exception caught: " + ex);
                });

        Runnable runnable = () -> System.out.println("Hello Kaunas JUG");
        threadBuilder.start(runnable);

        Thread.sleep(100);
    }
}
