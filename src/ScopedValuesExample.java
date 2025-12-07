import java.util.concurrent.Executors;

public class ScopedValuesExample {

    private static final ScopedValue<String> scopeValue = ScopedValue.newInstance();

    static void main(String[] args) {
        var scopedValuesExample = new ScopedValuesExample();
        try (var service = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1; i++) {
                service.submit(() -> {
                    ScopedValue.where(scopeValue, Thread.currentThread().threadId() + ": Kaunas").run(() -> {
                        System.out.println("Main:" + scopeValue.get());
                        scopedValuesExample.doSomeWork();
                    });
                });
            }
        }

        System.out.println("Is still bound:  " + scopeValue.isBound());
    }

    void doSomeWork() {
        doSomeInnerWork();
    }

    void doSomeInnerWork() {
        System.out.println("Start:" + scopeValue.get());
        ScopedValue.where(scopeValue, Thread.currentThread().threadId() + ": JUG").run(() -> {
            System.out.println("Middle:" + scopeValue.get());
        });
        System.out.println("End:" + scopeValue.get());
    }
}
