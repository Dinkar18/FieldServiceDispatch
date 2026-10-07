package com.example.dispatch.repository;
import com.example.dispatch.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByScheduleVersionId(Long scheduleVersionId);
    List<Assignment> findByTechnicianIdAndScheduleVersionId(Long technicianId, Long scheduleVersionId);
}
