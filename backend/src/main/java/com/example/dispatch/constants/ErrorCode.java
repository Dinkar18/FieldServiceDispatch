package com.example.dispatch.constants;

public final class ErrorCode {
    private ErrorCode() {}

    public static final String ASSIGNMENT_CONFLICT = "ASSIGNMENT_CONFLICT";
    public static final String INVALID_TECHNICIAN = "INVALID_TECHNICIAN";
    public static final String SKILL_MISMATCH = "SKILL_MISMATCH";
    public static final String REGION_MISMATCH = "REGION_MISMATCH";
    public static final String OUTSIDE_AVAILABILITY = "OUTSIDE_AVAILABILITY";
    public static final String WORKLOAD_EXCEEDED = "WORKLOAD_EXCEEDED";
    public static final String STALE_SCHEDULE = "STALE_SCHEDULE";
    public static final String INVALID_STATE_TRANSITION = "INVALID_STATE_TRANSITION";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
}
