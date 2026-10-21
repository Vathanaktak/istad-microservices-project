package kh.edu.istad.platform.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class WebFluxController {

    @GetMapping("/flux")
    public Flux<String> getText(){
        Flux<String> flux = Flux.just("apple","banana","mango")
                .log();
        return flux;
    }

}
