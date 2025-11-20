package org.examples.restful.repository;

import org.examples.restful.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long>, JpaSpecificationExecutor<UserAccount> {

    /**
     * 通过关键字查找用户账号
     * <p>
     * 查找用户账号的 username 或 displayName 包含关键字的记录，忽略大小写
     *
     * @param keyword 关键字
     * @return 匹配的用户账号列表
     */
    @Query("select ua from UserAccount ua where ua.username ilike %:keyword% or ua.displayName ilike %:keyword%")
    List<UserAccount> findByKeyword(@Param("keyword") String keyword);

    /**
     * 通过用户名查找是否有匹配的用户账号
     *
     * @param username 用户名
     * @return 匹配的用户账号
     */
    Optional<UserAccount> findByUsername(String username);
}
