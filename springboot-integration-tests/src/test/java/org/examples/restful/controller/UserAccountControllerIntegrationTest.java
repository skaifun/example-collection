package org.examples.restful.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.examples.restful.dto.UserAccountDTO;
import org.examples.restful.dto.UserAccountRequest;
import org.examples.restful.entity.UserAccount;
import org.examples.restful.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link UserAccountController} 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@WithMockUser
class UserAccountControllerIntegrationTest {

    private static final String API_ACCOUNTS = "/api/accounts";
    private static final String VALID_USERNAME = "test";
    private static final String VALID_DISPLAY_NAME = "TestUser";
    private static final String VALID_PASSWORD = "Password1!";
    private static final String VALID_EMAIL = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

    @BeforeEach
    void clearDatabase() {
        userAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("完整流程测试")
    void shouldCompleteCrudFlow() throws Exception {
        MvcResult createResult = createAccount(validRequest())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").value(VALID_USERNAME))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.email").value(VALID_EMAIL))
            .andExpect(jsonPath("$.displayName").value(VALID_DISPLAY_NAME))
            .andExpect(jsonPath("$.creationTime").exists())
            .andReturn();

        UserAccountDTO createdUser = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            UserAccountDTO.class
        );
        Long userId = createdUser.id();

        mockMvc.perform(get(API_ACCOUNTS + "/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(VALID_USERNAME));

        mockMvc.perform(get(API_ACCOUNTS).param("keyword", VALID_USERNAME))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].username").value(VALID_USERNAME));

        mockMvc.perform(delete(API_ACCOUNTS + "/{id}", userId))
            .andExpect(status().isOk());

        mockMvc.perform(get(API_ACCOUNTS + "/{id}", userId))
            .andExpect(status().isBadRequest());

        assertThat(userAccountRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("创建用户账号 - 成功应该返回创建的用户信息")
    void shouldCreateUserAccount() throws Exception {
        createAccount(validRequest())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").value(VALID_USERNAME))
            .andExpect(jsonPath("$.displayName").value(VALID_DISPLAY_NAME))
            .andExpect(jsonPath("$.email").value(VALID_EMAIL))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.creationTime").exists());

        UserAccount savedUser = userAccountRepository.findByUsername(VALID_USERNAME).orElseThrow();
        assertThat(savedUser)
            .extracting(UserAccount::getUsername, UserAccount::getDisplayName, UserAccount::getEmail)
            .containsExactly(VALID_USERNAME, VALID_DISPLAY_NAME, VALID_EMAIL);
        assertThat(savedUser.getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(passwordEncoder.matches(VALID_PASSWORD, savedUser.getPassword())).isTrue();
    }

    @ParameterizedTest(name = "创建用户账号 - 用户名不正确应该返回 400 参数：''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "abc", "username_is_very_long", "中文用户名"})
    @DisplayName("创建用户账号 - 用户名不正确应该返回 400")
    void shouldRejectInvalidUsername(String username) throws Exception {
        UserAccountRequest request = new UserAccountRequest(
            username,
            VALID_DISPLAY_NAME,
            VALID_PASSWORD,
            VALID_EMAIL
        );

        createAccount(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.username").isNotEmpty());

        assertDatabaseIsEmpty();
    }

    @Test
    @DisplayName("创建用户账号 - 显示名不正确应该返回 400")
    void shouldRejectInvalidDisplayName() throws Exception {
        UserAccountRequest request = new UserAccountRequest(
            VALID_USERNAME,
            "display_name_is_very_long",
            VALID_PASSWORD,
            VALID_EMAIL
        );

        createAccount(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.displayName").isNotEmpty());

        assertDatabaseIsEmpty();
    }

    @ParameterizedTest(name = "创建用户账号 - 密码不正确应该返回 400 参数：''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "pwd", "password_is_very_very_very_long", "pwd中文密码"})
    @DisplayName("创建用户账号 - 密码不正确应该返回 400")
    void shouldRejectInvalidPassword(String password) throws Exception {
        UserAccountRequest request = new UserAccountRequest(
            VALID_USERNAME,
            VALID_DISPLAY_NAME,
            password,
            VALID_EMAIL
        );

        createAccount(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.password").isNotEmpty());

        assertDatabaseIsEmpty();
    }

    @ParameterizedTest(name = "创建用户账号 - 邮箱不正确应该返回 400 参数：''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "invalid", "@example.org"})
    @DisplayName("创建用户账号 - 邮箱不正确应该返回 400")
    void shouldRejectInvalidEmail(String email) throws Exception {
        UserAccountRequest request = new UserAccountRequest(
            VALID_USERNAME,
            VALID_DISPLAY_NAME,
            VALID_PASSWORD,
            email
        );

        createAccount(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").isNotEmpty());

        assertDatabaseIsEmpty();
    }

    @Test
    @DisplayName("创建用户账号 - 用户名已存在应该返回错误")
    void shouldRejectDuplicateUsername() throws Exception {
        saveUser(VALID_USERNAME, "ExistingUser", "existing@example.com");
        UserAccountRequest request = new UserAccountRequest(
            VALID_USERNAME,
            "NewUser",
            VALID_PASSWORD,
            "new@example.com"
        );

        createAccount(request)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account already exists")));

        assertThat(userAccountRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("根据 ID 查询用户账号 - 应该返回用户信息")
    void shouldFindUserAccountById() throws Exception {
        UserAccount savedUser = saveUser(VALID_USERNAME, VALID_DISPLAY_NAME, VALID_EMAIL);

        mockMvc.perform(get(API_ACCOUNTS + "/{id}", savedUser.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.username").value(VALID_USERNAME))
            .andExpect(jsonPath("$.displayName").value(VALID_DISPLAY_NAME))
            .andExpect(jsonPath("$.email").value(VALID_EMAIL))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.creationTime").exists());
    }

    @Test
    @DisplayName("根据 ID 查询用户账号 - 用户不存在应该返回错误")
    void shouldReturnErrorWhenUserAccountIsNotFoundById() throws Exception {
        mockMvc.perform(get(API_ACCOUNTS + "/{id}", 999L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account not found")));
    }

    @Test
    @DisplayName("根据关键字搜索用户账号 - 应该返回匹配的用户列表")
    void shouldFindUserAccountsByKeyword() throws Exception {
        saveSearchUsers();

        mockMvc.perform(get(API_ACCOUNTS).param("keyword", "john"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].username", hasItem("john123")))
            .andExpect(jsonPath("$[*].displayName", hasItem("Bob Johnson")));
    }

    @Test
    @DisplayName("根据关键字搜索用户账号 - 无匹配结果应该返回空列表")
    void shouldReturnEmptyListWhenKeywordDoesNotMatch() throws Exception {
        saveUser(VALID_USERNAME, VALID_DISPLAY_NAME, VALID_EMAIL);

        mockMvc.perform(get(API_ACCOUNTS).param("keyword", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("根据关键字搜索用户账号 - 无关键词应该全部账号列表")
    void shouldReturnAllUserAccountsWithoutKeyword() throws Exception {
        saveSearchUsers();

        mockMvc.perform(get(API_ACCOUNTS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("删除用户账号 - 应该成功删除用户")
    void shouldDeleteUserAccount() throws Exception {
        UserAccount savedUser = saveUser(VALID_USERNAME, VALID_DISPLAY_NAME, VALID_EMAIL);

        mockMvc.perform(delete(API_ACCOUNTS + "/{id}", savedUser.getId()))
            .andExpect(status().isOk());

        assertThat(userAccountRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("删除用户账号 - 用户不存在应该返回错误")
    void shouldReturnErrorWhenDeletingMissingUserAccount() throws Exception {
        mockMvc.perform(delete(API_ACCOUNTS + "/{id}", 999L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account not found")));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("未认证访问 - 应该重定向到登录页")
    void shouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get(API_ACCOUNTS).param("keyword", VALID_USERNAME))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(status().is3xxRedirection());
    }

    private UserAccountRequest validRequest() {
        return new UserAccountRequest(
            VALID_USERNAME,
            VALID_DISPLAY_NAME,
            VALID_PASSWORD,
            VALID_EMAIL
        );
    }

    private ResultActions createAccount(UserAccountRequest request) throws Exception {
        return mockMvc.perform(post(API_ACCOUNTS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    private UserAccount saveUser(String username, String displayName, String email) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPassword("encoded-password");
        user.setEmail(email);
        user.setCreationTime(Instant.now());
        return userAccountRepository.save(user);
    }

    private void saveSearchUsers() {
        saveUser("john123", "John Doe", "john@example.com");
        saveUser("jane456", "Jane Smith", "jane@example.com");
        saveUser("bob789", "Bob Johnson", "bob@example.com");
    }

    private void assertDatabaseIsEmpty() {
        assertThat(userAccountRepository.count()).isZero();
    }

}

