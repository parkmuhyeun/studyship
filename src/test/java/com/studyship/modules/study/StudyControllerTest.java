package com.studyship.modules.study;

import com.studyship.modules.account.WithAccount;
import com.studyship.modules.account.AccountRepository;
import com.studyship.modules.account.AccountService;
import com.studyship.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class StudyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected StudyRepository studyRepository;
    @Autowired protected AccountRepository accountRepository;
    @Autowired protected StudyService studyService;
    @Autowired protected AccountService accountService;

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(view().name("study/form"));
    }

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 개설 - 완료")
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname("muto");
        assertTrue(study.getManagers().contains(account));
    }

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "wrong path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));

        Study study = studyRepository.findByPath("wrong path");
        assertNull(study);
    }

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account muto = accountRepository.findByNickname("muto");
        studyService.createNewStudy(study, muto);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 가입")
    void joinStudy() throws Exception {
        Account account = createAccount("test");
        Study study = createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account muto = accountRepository.findByNickname("muto");
        assertTrue(study.getMembers().contains(muto));
    }

    @Test
    @WithAccount("muto")
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account account = createAccount("test");
        Study study = createStudy("test-study", account);

        Account muto = accountRepository.findByNickname("muto");
        studyService.addMember(study, muto);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(muto));
    }

    protected Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

    protected Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname+ "@email.com");
        accountRepository.save(account);
        return account;
    }
}