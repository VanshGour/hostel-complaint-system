package com.hostel.complaint.repository;

import com.hostel.complaint.model.Role;
import com.hostel.complaint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndActiveTrue(Role role);
    List<User> findByRoleAndTradeSpecialtyAndActiveTrue(Role role, String tradeSpecialty);
}
