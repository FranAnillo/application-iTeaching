package iteaching.app.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {
    
    @GetMapping("/welcome")
    public String welcome() {
        System.out.println("LLEGA\n\n\n");
        return "index";
    }
}
