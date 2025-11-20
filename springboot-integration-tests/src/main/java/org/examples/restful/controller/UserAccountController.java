package org.examples.restful.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.examples.restful.dto.UserAccountDTO;
import org.examples.restful.dto.UserAccountRequest;
import org.examples.restful.service.UserAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class UserAccountController {
    private final UserAccountService userAccountService;

    @GetMapping("")
    public List<UserAccountDTO> findByKeyword(@RequestParam(value = "keyword", required = false) String keyword) {
        return userAccountService.findUserAccountByKeyword(keyword);
    }

    @PostMapping("")
    public UserAccountDTO createNewUserAccount(@RequestBody @Valid UserAccountRequest userAccount) {
        return userAccountService.saveUserAccount(userAccount);
    }

    @DeleteMapping("/{id}")
    public void deleteUserAccountById(@PathVariable("id") Long id) {
        userAccountService.deleteById(id);
    }

    @GetMapping("/{id}")
    public UserAccountDTO findById(@PathVariable("id") Long id) {
        return userAccountService.findUserAccountById(id);
    }
}
