package com.unobank.auth_service.database.repos;

import com.unobank.auth_service.database.models.ERole;
import com.unobank.auth_service.database.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
