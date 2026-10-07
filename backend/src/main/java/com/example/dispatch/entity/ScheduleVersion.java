package com.example.dispatch.entity;

import com.example.dispatch.enums.ScheduleChangeReason;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_versions")
public class ScheduleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long scheduleId;
    private int versionNumber;
    
    private LocalDateTime createdAt;
    private String createdBy;

    @Enumerated(EnumType.STRING)
    private ScheduleChangeReason reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public ScheduleChangeReason getReason() { return reason; }
    public void setReason(ScheduleChangeReason reason) { this.reason = reason; }
}
