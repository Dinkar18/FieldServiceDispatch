package com.example.dispatch.planner;

public class UnassignedReason {
    private Long requestId;
    private String reason;

    public UnassignedReason() {}

    public UnassignedReason(Long requestId, String reason) {
        this.requestId = requestId;
        this.reason = reason;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
