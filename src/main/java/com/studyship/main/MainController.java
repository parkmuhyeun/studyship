package com.studyship.main;

import com.studyship.account.CurrentUser;
import com.studyship.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        return "index";
    }
}
