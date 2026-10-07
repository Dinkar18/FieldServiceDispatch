package com.example.dispatch.dto.request;

import com.example.dispatch.planner.ScheduleProposal;
import com.example.dispatch.enums.ScheduleChangeReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

public class ApprovalRequest {
    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    @NotNull(message = "Expected Base Version is required for idempotency")
    private Long expectedBaseVersion;

    @NotNull(message = "Proposal cannot be null")
    @Valid
    private ScheduleProposal proposal;

    @NotBlank(message = "Actor is required")
    private String actor;

    @NotNull(message = "Reason is required")
    private ScheduleChangeReason reason;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getExpectedBaseVersion() { return expectedBaseVersion; }
    public void setExpectedBaseVersion(Long expectedBaseVersion) { this.expectedBaseVersion = expectedBaseVersion; }

    public ScheduleProposal getProposal() { return proposal; }
    public void setProposal(ScheduleProposal proposal) { this.proposal = proposal; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public ScheduleChangeReason getReason() { return reason; }
    public void setReason(ScheduleChangeReason reason) { this.reason = reason; }
}
