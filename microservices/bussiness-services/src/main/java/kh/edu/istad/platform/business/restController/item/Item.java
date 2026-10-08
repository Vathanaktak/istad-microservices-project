package kh.edu.istad.platform.business.restController.item;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/item")
public class Item {

    @GetMapping("/buffer")
    public Flux<Integer> getItem(){
        Flux<Integer> items = Flux.range(1, 25)
                .onBackpressureBuffer(10);
        return items;
    }
    @GetMapping("/drop")
    public Flux<Integer> getItemByDrop(){
        Flux<Integer> items = Flux.range(1, 25)
                .onBackpressureDrop();
        return items;
    }


}
