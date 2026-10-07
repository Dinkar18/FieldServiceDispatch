package com.example.dispatch.entity;

import com.example.dispatch.enums.ScheduleStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate workingDate;
    
    private Long currentVersion;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getWorkingDate() { return workingDate; }
    public void setWorkingDate(LocalDate workingDate) { this.workingDate = workingDate; }

    public Long getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Long currentVersion) { this.currentVersion = currentVersion; }

    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
}
