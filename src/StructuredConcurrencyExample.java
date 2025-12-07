import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructuredConcurrencyExample {

    static void main(String[] args) throws InterruptedException, ExecutionException {
        var structuredConcurrencyExample = new StructuredConcurrencyExample();
        structuredConcurrencyExample.printProductsSequentially();
        structuredConcurrencyExample.printProductsConcurrently();
        structuredConcurrencyExample.printProductsWithStructuredConcurrency();
        structuredConcurrencyExample.printProductsWithStructuredConcurrencyWithCache();
    }

    public void printProductsSequentially() throws InterruptedException {
        System.out.println("printProductsSequentially()");
        long start = System.currentTimeMillis();

        List<String> products1 = getProductsFromWebService();
        List<String> products2 = getProductsFromDatabase();

        printTime(start);
        printResult(products1, products2);
        System.out.println();
    }

    public void printProductsConcurrently() throws ExecutionException, InterruptedException {
        System.out.println("printProductsConcurrently()");
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var future1 = executor.submit(this::getProductsFromWebService);
            var future2 = executor.submit(this::getProductsFromDatabase);

            printResult(future1.get(), future2.get());
            printTime(start);
            System.out.println();
        }
    }

    public void printProductsWithStructuredConcurrency() throws ExecutionException, InterruptedException {
        System.out.println("printProductsWithStructuredConcurrency()");
        long start = System.currentTimeMillis();

        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<List<String>>allSuccessfulOrThrow())) {
            var subtask1 = scope.fork(this::getProductsFromWebService);
            var subtask2 = scope.fork(this::getProductsFromDatabase);
            scope.join();

            printResult(subtask1.get(), subtask2.get());
            printTime(start);
            System.out.println();
        }
    }

    public void printProductsWithStructuredConcurrencyWithCache() throws ExecutionException, InterruptedException {
        System.out.println("printProductsWithStructuredConcurrencyWithCache()");
        long start = System.currentTimeMillis();

        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<List<String>>allSuccessfulOrThrow())) {
            var subtask1 = scope.fork(this::getProductsFromWebService);
            var subtask2 = scope.fork(this::getProductsFromDatabaseWithCache);
            scope.join();

            printResult(subtask1.get(), subtask2.get());
            printTime(start);
            System.out.println();
        }
    }

    private List<String> getProductsFromWebService() throws InterruptedException {
        Thread.sleep(2000);
        return List.of("Monitor", "Mouse", "Keyboard");
    }

    private List<String> getProductsFromDatabase() throws InterruptedException {
        Thread.sleep(500);
        return List.of("Chair", "Table", "Bed");
    }

    private List<String> getProductsFromCache() {
        return List.of("Chair", "Table");
    }

    private List<String> getProductsFromDatabaseWithCache() throws InterruptedException {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<List<String>>anySuccessfulResultOrThrow())) {
            scope.fork(this::getProductsFromDatabase);
            scope.fork(this::getProductsFromCache);
            var products = scope.join();

            return products;
        }
    }

    private void printTime(long start) {
        System.out.println("Time taken: " + (System.currentTimeMillis() - start));
    }

    private void printResult(List<String> remoteProducts, List<String> localProducts) {
        var result = Stream
                .concat(Stream.of(remoteProducts), Stream.of(localProducts))
                .flatMap(Collection::stream)
                .collect(Collectors.joining(", "));

        System.out.println(result);
    }
}
