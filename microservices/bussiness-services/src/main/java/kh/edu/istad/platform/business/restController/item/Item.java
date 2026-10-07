package kh.edu.istad.platform.business.restController.item;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/item")
public class Item {

    @GetMapping
    public List<Integer> getItem(){
        return List.of(1,4,53,5);
    }

}
