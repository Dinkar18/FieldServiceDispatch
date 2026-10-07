package com.example.dispatch.planner;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

public class ScheduleProposal {
    @NotNull
    @Valid
    private List<ProposedAssignment> assignments = new ArrayList<>();
    @NotNull
    private List<UnassignedReason> unassignedRequests = new ArrayList<>();
    @NotNull
    private List<String> tradeoffs = new ArrayList<>();
    @NotNull
    private List<String> risks = new ArrayList<>();

    public ScheduleProposal() {}

    public List<ProposedAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<ProposedAssignment> assignments) { this.assignments = assignments; }
    
    public List<UnassignedReason> getUnassignedRequests() { return unassignedRequests; }
    public void setUnassignedRequests(List<UnassignedReason> unassignedRequests) { this.unassignedRequests = unassignedRequests; }
    
    public List<String> getTradeoffs() { return tradeoffs; }
    public void setTradeoffs(List<String> tradeoffs) { this.tradeoffs = tradeoffs; }
    
    public List<String> getRisks() { return risks; }
    public void setRisks(List<String> risks) { this.risks = risks; }
}
