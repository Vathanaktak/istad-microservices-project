package kh.edu.istad.platform.business.restController.item.butter;

import reactor.core.publisher.Flux;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Flux<Integer> publisher = Flux.range(1, 10);
        Flux.range(1, 100)
                .limitRate(10)
                .subscribe(
                        item -> System.out.println(item),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("Completed"),
                        subscribe -> {
                            subscribe.cancel();
                        }
                        );
    }
}