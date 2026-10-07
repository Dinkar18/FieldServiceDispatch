package com.example.dispatch.entity;

import com.example.dispatch.enums.TechnicianStatus;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "technicians")
public class Technician {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "technician_skills", joinColumns = @JoinColumn(name = "technician_id"))
    @Column(name = "skill")
    private List<String> skills;

    private String assignedRegion;

    private LocalTime availabilityStart;
    private LocalTime availabilityEnd;

    private int maximumWorkloadMinutes;

    @Enumerated(EnumType.STRING)
    private TechnicianStatus status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getAssignedRegion() { return assignedRegion; }
    public void setAssignedRegion(String assignedRegion) { this.assignedRegion = assignedRegion; }

    public LocalTime getAvailabilityStart() { return availabilityStart; }
    public void setAvailabilityStart(LocalTime availabilityStart) { this.availabilityStart = availabilityStart; }

    public LocalTime getAvailabilityEnd() { return availabilityEnd; }
    public void setAvailabilityEnd(LocalTime availabilityEnd) { this.availabilityEnd = availabilityEnd; }

    public int getMaximumWorkloadMinutes() { return maximumWorkloadMinutes; }
    public void setMaximumWorkloadMinutes(int maximumWorkloadMinutes) { this.maximumWorkloadMinutes = maximumWorkloadMinutes; }

    public TechnicianStatus getStatus() { return status; }
    public void setStatus(TechnicianStatus status) { this.status = status; }
}
