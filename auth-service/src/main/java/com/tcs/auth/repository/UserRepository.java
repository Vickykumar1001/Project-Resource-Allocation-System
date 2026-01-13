package com.tcs.auth.repository;

import com.tcs.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmailAndDeletedFalse(String email);
    Page<User> findAllByDeletedFalse(Pageable pageable);
    List<User> findAllByIdInAndDeletedFalse(List<Long> ids);
	Optional<User> findByEmail(String email);
}
