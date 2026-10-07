package com.example.dispatch.planner;
import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;

public class ProposedAssignment {
    @NotNull(message = "Request ID is required")
    private Long requestId;
    
    @NotNull(message = "Technician ID is required")
    private Long technicianId;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    public ProposedAssignment() {}

    public ProposedAssignment(Long requestId, Long technicianId, LocalTime startTime, LocalTime endTime) {
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
