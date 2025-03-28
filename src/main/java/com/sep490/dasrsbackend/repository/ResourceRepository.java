package com.sep490.dasrsbackend.repository;

import com.sep490.dasrsbackend.model.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Optional<Resource> findByIdAndIsEnable(Long id, boolean enable);
    Page<Resource> findByIsEnable(boolean b, Pageable pageable);
}
