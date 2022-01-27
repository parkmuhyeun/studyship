package com.studyship.modules.main;

import com.studyship.modules.account.Account;
import com.studyship.modules.account.AccountRepository;
import com.studyship.modules.account.CurrentAccount;
import com.studyship.modules.event.Enrollment;
import com.studyship.modules.event.EnrollmentRepository;
import com.studyship.modules.event.EventRepository;
import com.studyship.modules.study.Study;
import com.studyship.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountRepository accountRepository;
    private final StudyRepository studyRepository;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute("account", accountLoaded);
            model.addAttribute("studyManagerOf",
                    studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("studyMemberOf",
                    studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("studyList", studyRepository.findByAccount(
                    accountLoaded.getTags(), accountLoaded.getZones()));
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            return "index-after-login";
        }

        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/search/study")
    String search(String keyword, Model model,
            @PageableDefault(size = 9, page = 0, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Page<Study> studyPage =  studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
