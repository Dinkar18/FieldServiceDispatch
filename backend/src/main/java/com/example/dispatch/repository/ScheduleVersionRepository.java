package com.example.dispatch.repository;
import com.example.dispatch.entity.ScheduleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleVersionRepository extends JpaRepository<ScheduleVersion, Long> {}
