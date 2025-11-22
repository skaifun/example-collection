package org.examples.restful.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.examples.restful.dto.UserAccountDTO;
import org.examples.restful.dto.UserAccountRequest;
import org.examples.restful.entity.UserAccount;
import org.examples.restful.repository.UserAccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link UserAccountController} 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
class UserAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    /**
     * 每个测试方法执行后清理数据库
     */
    @AfterEach
    void tearDown() {
        userAccountRepository.deleteAll();
    }

    /**
     * 测试完整的 CRUD 流程
     */
    @Test
    @WithMockUser
    @DisplayName("完整流程测试")
    void testCompleteFlow() throws Exception {
        // 1. 创建用户
        UserAccountRequest createRequest = new UserAccountRequest(
            "test",
            "TestUser",
            "encodedPassword",
            "test@example.com"
        );

        MvcResult createResult = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").value("test"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.displayName").value("TestUser"))
            .andExpect(jsonPath("$.creationTime").exists())
            .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        UserAccountDTO createdUser = objectMapper.readValue(createResponse, UserAccountDTO.class);
        Long userId = createdUser.id();

        // 2. 通过 ID 查询用户
        mockMvc.perform(get("/api/accounts/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("test"));

        // 3. 通过关键字搜索用户
        mockMvc.perform(get("/api/accounts")
                .param("keyword", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].username").value("test"));

        // 4. 删除用户
        mockMvc.perform(delete("/api/accounts/{id}", userId))
            .andExpect(status().isOk());

        // 5. 验证用户已被删除
        mockMvc.perform(get("/api/accounts/{id}", userId))
            .andExpect(status().isBadRequest());

        assertThat(userAccountRepository.count()).isEqualTo(0);
    }

    /**
     * 测试创建用户账号 - 成功场景
     */
    @Test
    @WithMockUser
    @DisplayName("创建用户账号 - 成功应该返回创建的用户信息")
    void testCreateUserAccount_Success() throws Exception {
        // 准备测试数据
        UserAccountRequest request = new UserAccountRequest(
            "test",
            "TestUser",
            "pwd123",
            "test@example.com"
        );

        // 执行请求并验证响应
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").value("test"))
            .andExpect(jsonPath("$.displayName").value("TestUser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.creationTime").exists());

        // 验证数据库中的数据
        assertThat(userAccountRepository.count()).isEqualTo(1);
        UserAccount savedUser = userAccountRepository.findByUsername("test").orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("test");
        assertThat(savedUser.getDisplayName()).isEqualTo("TestUser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("encodedPassword"); // 密码应该被加密
    }

    /**
     * 测试创建用户账号 - 用户名校验场景
     */
    @ParameterizedTest(name = "创建用户账号 - 用户名不正确应该返回 400 参数：''{0}''")
    @ValueSource(strings = {"", "  ", "abc", "username_is_very_long", "中文用户名"})
    @WithMockUser
    @DisplayName("创建用户账号 - 用户名不正确应该返回 400")
    void testCreateUserAccount_InvalidUsername(String username) throws Exception {
        // 用户名太短
        UserAccountRequest request = new UserAccountRequest(
            username,
            "TestUser",
            "encodedPassword",
            "test@example.com"
        );

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.username").isNotEmpty());

        // 验证数据库中没有数据
        assertThat(userAccountRepository.count()).isEqualTo(0);
    }

    /**
     * 测试创建用户账号 - 显示名校验场景
     */
    @ParameterizedTest(name = "创建用户账号 - 显示名不正确应该返回 400 参数：''{0}''")
    @ValueSource(strings = {"display_name_is_very_long"})
    @WithMockUser
    @DisplayName("创建用户账号 - 显示名不正确应该返回 400")
    void testCreateUserAccount_InvalidDisplayName(String displayName) throws Exception {
        // 用户名太短
        UserAccountRequest request = new UserAccountRequest(
            "test",
            displayName,
            "encodedPassword",
            "test@example.com"
        );

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.displayName").isNotEmpty());

        // 验证数据库中没有数据
        assertThat(userAccountRepository.count()).isEqualTo(0);
    }

    /**
     * 测试创建用户账号 - 密码校验场景
     */
    @ParameterizedTest(name = "创建用户账号 - 密码不正确应该返回 400 参数：''{0}''")
    @ValueSource(strings = {"", "  ", "pwd", "password_is_very_very_very_long", "pwd中文密码"})
    @WithMockUser
    @DisplayName("创建用户账号 - 密码不正确应该返回 400")
    void testCreateUserAccount_InvalidPassword(String password) throws Exception {
        // 用户名太短
        UserAccountRequest request = new UserAccountRequest(
            "test",
            "TestUser",
            password,
            "test@example.com"
        );

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.password").isNotEmpty());

        // 验证数据库中没有数据
        assertThat(userAccountRepository.count()).isEqualTo(0);
    }

    /**
     * 测试创建用户账号 - 邮箱校验场景
     */
    @ParameterizedTest(name = "创建用户账号 - 邮箱不正确应该返回 400 参数：''{0}''")
    @ValueSource(strings = {"", "  ", "invalid", "@example.org"})
    @WithMockUser
    @DisplayName("创建用户账号 - 邮箱不正确应该返回 400")
    void testCreateUserAccount_InvalidEmail(String email) throws Exception {
        // 用户名太短
        UserAccountRequest request = new UserAccountRequest(
            "test",
            "TestUser",
            "encodedPassword",
            email
        );

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").isNotEmpty());

        // 验证数据库中没有数据
        assertThat(userAccountRepository.count()).isEqualTo(0);
    }

    /**
     * 测试创建用户账号 - 用户名重复场景
     */
    @Test
    @WithMockUser
    @DisplayName("创建用户账号 - 用户名已存在应该返回错误")
    void testCreateUserAccount_DuplicateUsername() throws Exception {
        // 先创建一个用户
        UserAccount existingUser = new UserAccount();
        existingUser.setUsername("test");
        existingUser.setDisplayName("ExistingUser");
        existingUser.setPassword("encodedPassword");
        existingUser.setEmail("existing@example.com");
        existingUser.setCreationTime(Instant.now());
        userAccountRepository.save(existingUser);

        // 尝试创建同名用户
        UserAccountRequest request = new UserAccountRequest(
            "test",  // 重复的用户名
            "NewUser",
            "encodedPassword",
            "new@example.com"
        );

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account already exists")));

        // 验证数据库中只有一条记录
        assertThat(userAccountRepository.count()).isEqualTo(1);
    }

    /**
     * 测试根据 ID 查询用户账号 - 成功场景
     */
    @Test
    @WithMockUser
    @DisplayName("根据 ID 查询用户账号 - 应该返回用户信息")
    void testFindUserAccountById_Success() throws Exception {
        // 准备测试数据
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername("test");
        userAccount.setDisplayName("TestUser");
        userAccount.setPassword("encodedPassword");
        userAccount.setEmail("test@example.com");
        userAccount.setCreationTime(Instant.now());
        UserAccount savedUser = userAccountRepository.save(userAccount);

        // 执行请求并验证响应
        mockMvc.perform(get("/api/accounts/{id}", savedUser.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.username").value("test"))
            .andExpect(jsonPath("$.displayName").value("TestUser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.creationTime").exists());
    }

    /**
     * 测试根据 ID 查询用户账号 - 用户不存在场景
     */
    @Test
    @WithMockUser
    @DisplayName("根据 ID 查询用户账号 - 用户不存在应该返回错误")
    void testFindUserAccountById_NotFound() throws Exception {
        // 查询不存在的用户
        mockMvc.perform(get("/api/accounts/{id}", 999L))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account not found")));
    }

    /**
     * 测试根据关键字搜索用户账号 - 成功场景
     */
    @Test
    @WithMockUser
    @DisplayName("根据关键字搜索用户账号 - 应该返回匹配的用户列表")
    void testFindUserAccountsByKeyword_Success() throws Exception {
        // 准备测试数据
        UserAccount user1 = new UserAccount();
        user1.setUsername("john123");
        user1.setDisplayName("John Doe");
        user1.setPassword("password");
        user1.setEmail("john@example.com");
        user1.setCreationTime(Instant.now());

        UserAccount user2 = new UserAccount();
        user2.setUsername("jane456");
        user2.setDisplayName("Jane Smith");
        user2.setPassword("password");
        user2.setEmail("jane@example.com");
        user2.setCreationTime(Instant.now());

        UserAccount user3 = new UserAccount();
        user3.setUsername("bob789");
        user3.setDisplayName("Bob Johnson");
        user3.setPassword("password");
        user3.setEmail("bob@example.com");
        user3.setCreationTime(Instant.now());

        userAccountRepository.save(user1);
        userAccountRepository.save(user2);
        userAccountRepository.save(user3);

        // 搜索包含 "john" 的用户
        mockMvc.perform(get("/api/accounts")
                .param("keyword", "john"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))  // 应该匹配 john123 和 Bob Johnson
            .andExpect(jsonPath("$[*].username", hasItem("john123")))
            .andExpect(jsonPath("$[*].displayName", hasItem("Bob Johnson")));
    }

    /**
     * 测试根据关键字搜索用户账号 - 无结果场景
     */
    @Test
    @WithMockUser
    @DisplayName("根据关键字搜索用户账号 - 无匹配结果应该返回空列表")
    void testFindUserAccountsByKeyword_NoResults() throws Exception {
        // 准备测试数据
        UserAccount user = new UserAccount();
        user.setUsername("test");
        user.setDisplayName("TestUser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setCreationTime(Instant.now());
        userAccountRepository.save(user);

        // 搜索不存在的关键字
        mockMvc.perform(get("/api/accounts")
                .param("keyword", "nonexistent"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * 测试根据关键字搜索用户账号 - 无关键词场景
     */
    @Test
    @WithMockUser
    @DisplayName("根据关键字搜索用户账号 - 无关键词应该全部账号列表")
    void testFindUserAccountsByKeyword_NoKeyword() throws Exception {
        // 准备测试数据
        UserAccount user1 = new UserAccount();
        user1.setUsername("john123");
        user1.setDisplayName("John Doe");
        user1.setPassword("password");
        user1.setEmail("john@example.com");
        user1.setCreationTime(Instant.now());

        UserAccount user2 = new UserAccount();
        user2.setUsername("jane456");
        user2.setDisplayName("Jane Smith");
        user2.setPassword("password");
        user2.setEmail("jane@example.com");
        user2.setCreationTime(Instant.now());

        UserAccount user3 = new UserAccount();
        user3.setUsername("bob789");
        user3.setDisplayName("Bob Johnson");
        user3.setPassword("password");
        user3.setEmail("bob@example.com");
        user3.setCreationTime(Instant.now());

        userAccountRepository.save(user1);
        userAccountRepository.save(user2);
        userAccountRepository.save(user3);

        // 搜索不存在的关键字
        mockMvc.perform(get("/api/accounts"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    /**
     * 测试删除用户账号 - 成功场景
     */
    @Test
    @WithMockUser
    @DisplayName("删除用户账号 - 应该成功删除用户")
    void testDeleteUserAccount_Success() throws Exception {
        // 准备测试数据
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername("test");
        userAccount.setDisplayName("TestUser");
        userAccount.setPassword("password");
        userAccount.setEmail("test@example.com");
        userAccount.setCreationTime(Instant.now());
        UserAccount savedUser = userAccountRepository.save(userAccount);

        assertThat(userAccountRepository.count()).isEqualTo(1);

        // 执行删除请求
        mockMvc.perform(delete("/api/accounts/{id}", savedUser.getId()))
            .andDo(print())
            .andExpect(status().isOk());

        // 验证数据库中的数据已被删除
        assertThat(userAccountRepository.count()).isEqualTo(0);
        assertThat(userAccountRepository.findById(savedUser.getId())).isEmpty();
    }

    /**
     * 测试删除用户账号 - 用户不存在场景
     */
    @Test
    @WithMockUser
    @DisplayName("删除用户账号 - 用户不存在应该返回错误")
    void testDeleteUserAccount_NotFound() throws Exception {
        // 删除不存在的用户
        mockMvc.perform(delete("/api/accounts/{id}", 999L))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail", containsString("user account not found")));
    }

    /**
     * 测试未认证访问 - 应该被拒绝
     */
    @Test
    @DisplayName("未认证访问 - 应该返回 401 或重定向到登录页")
    void testUnauthenticatedAccess() throws Exception {
        // 不使用 @WithMockUser，模拟未认证用户
        mockMvc.perform(get("/api/accounts")
                .param("keyword", "test"))
            .andDo(print())
            .andExpect(status().is3xxRedirection());  // Spring Security 默认会重定向到登录页
    }

}

