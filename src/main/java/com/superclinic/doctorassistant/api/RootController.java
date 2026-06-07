package com.superclinic.doctorassistant.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootController {

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/swagger-ui.html");
    }
}
