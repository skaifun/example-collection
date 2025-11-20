package org.examples.restful.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link org.examples.restful.entity.UserAccount}
 */
public record UserAccountRequest(
    // 用户名：4-10位字母和数字
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 10, message = "用户名长度为 4 到 10 位")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "用户名由大小写字母和数字组成")
    String username,

    // 显示名：可选，最大24字符
    @Size(max = 24, message = "显示名最长 24 个字符")
    String displayName,

    // 密码：6-18位字母、数字和特殊符号
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 18, message = "密码长度为 6 到 18 位")
    @Pattern(regexp = "^[A-Za-z\\d!@#$%^&*]+$", message = "密码由大小写英文字母、数字和特殊符号（!@#$%^&*）组成")
    String password,

    // 邮箱：非空，必须符合邮箱格式
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请填写有效的邮箱地址")
    String email
) implements Serializable {
}