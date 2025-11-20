package org.examples.restful.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.examples.restful.entity.UserAccount}
 */
public record UserAccountDTO(
    // ID
    Long id,
    // 用户名
    String username,
    // 邮箱
    String email,
    // 显示名
    String displayName,
    // 创建时间
    Instant creationTime
) implements Serializable {
}