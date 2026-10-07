package com.example.franquicias.repository;

import com.example.franquicias.entity.Branch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByFranchiseId(Long franchiseId);

    boolean existsByIdAndFranchiseId(Long id, Long franchiseId);
}
