package org.examples.restful.service;

import lombok.RequiredArgsConstructor;
import org.examples.restful.dto.UserAccountDTO;
import org.examples.restful.dto.UserAccountRequest;
import org.examples.restful.entity.UserAccount;
import org.examples.restful.exception.AppException;
import org.examples.restful.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAccountService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;

    /**
     * 根据用户名查找用户账号
     * <p>
     * 主要是给 Spring Security 使用，用于认证和授权
     *
     * @param username 用户名
     * @return 用户账号
     * @throws UsernameNotFoundException 如果用户账号不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAccount> matchedUserAccount = userAccountRepository.findByUsername(username);
        if (matchedUserAccount.isEmpty()) {
            throw new UsernameNotFoundException("user account not found with username: " + username);
        }
        return User.withUsername(username)
            .accountExpired(false)
            .accountLocked(false)
            .build();
    }

    /**
     * 根据关键字查找用户账号
     * <p>
     * 查找用户账号的 username 或 displayName 包含关键字的记录，忽略大小写
     *
     * @param keyword 关键字
     * @return 用户账号列表
     */
    @Transactional(readOnly = true)
    public List<UserAccountDTO> findUserAccountByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            // 查询所有的用户账号
            return userAccountRepository.findAll().stream()
                .map(userAccount -> new UserAccountDTO(
                    userAccount.getId(),
                    userAccount.getUsername(),
                    userAccount.getEmail(),
                    userAccount.getDisplayName(),
                    userAccount.getCreationTime()
                )).collect(Collectors.toList());
        }
        return userAccountRepository.findByKeyword(keyword).stream()
            .map(userAccount -> new UserAccountDTO(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getCreationTime()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 根据ID查找用户账号
     *
     * @param id 用户ID
     * @return 用户账号
     * @throws AppException 如果用户账号不存在
     */
    public UserAccountDTO findUserAccountById(Long id) {
        Optional<UserAccount> matchedUserAccount = userAccountRepository.findById(id);
        if (matchedUserAccount.isEmpty()) {
            throw new AppException("user account not found with id: " + id);
        }
        return matchedUserAccount.map(userAccount -> new UserAccountDTO(
            userAccount.getId(),
            userAccount.getUsername(),
            userAccount.getEmail(),
            userAccount.getDisplayName(),
            userAccount.getCreationTime()
        )).get();
    }

    /**
     * 保存用户账号
     *
     * @param userAccount 用户账号
     * @return 保存后的用户账号
     */
    @Transactional
    public UserAccountDTO saveUserAccount(UserAccountRequest userAccount) {
        if (userAccountRepository.findByUsername(userAccount.username()).isPresent()) {
            throw new AppException("user account already exists with username: " + userAccount.username());
        }

        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setUsername(userAccount.username());
        String encodedPassword = passwordEncoder.encode(userAccount.password());
        newUserAccount.setPassword(encodedPassword);
        newUserAccount.setDisplayName(userAccount.displayName());
        newUserAccount.setEmail(userAccount.email());
        newUserAccount.setCreationTime(Instant.now());

        UserAccount savedUserAccount = userAccountRepository.save(newUserAccount);
        return new UserAccountDTO(
            savedUserAccount.getId(),
            savedUserAccount.getUsername(),
            savedUserAccount.getEmail(),
            savedUserAccount.getDisplayName(),
            savedUserAccount.getCreationTime()
        );
    }

    /**
     * 根据ID删除用户账号
     *
     * @param id 用户ID
     * @throws AppException 如果用户账号不存在
     */
    @Transactional
    public void deleteById(Long id) {
        Optional<UserAccount> matchedUserAccount = userAccountRepository.findById(id);
        if (matchedUserAccount.isEmpty()) {
            throw new AppException("user account not found with id: " + id);
        }
        userAccountRepository.delete(matchedUserAccount.get());
    }

}
