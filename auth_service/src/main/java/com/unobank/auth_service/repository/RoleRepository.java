package com.unobank.auth_service.repository;

import com.unobank.auth_service.models.ERole;
import com.unobank.auth_service.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
