package org.j3lsmp.spotify_spreadsheet_app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

	@RequestMapping("/")
    public String forward() {
        return "forward:/index.html";
    }
}