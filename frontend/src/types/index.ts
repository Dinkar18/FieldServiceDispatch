export type Priority = "LOW" | "MEDIUM" | "HIGH" | "EMERGENCY";
export type RequestStatus = "PENDING" | "ASSIGNED" | "COMPLETED" | "UNASSIGNED" | "CANCELLED";
export type TechnicianStatus = "AVAILABLE" | "CANCELLED";
export type AssignmentStatus = "PROPOSED" | "CONFIRMED" | "COMPLETED" | "CANCELLED";
export type ScheduleStatus = "DRAFT" | "PENDING_APPROVAL" | "APPROVED";
export type ScheduleChangeReason = "INITIAL_PLAN" | "MANUAL_EDIT" | "TECHNICIAN_CANCELLED" | "EMERGENCY_REQUEST" | "REPLAN";

export interface Technician {
  id: number;
  name: string;
  skills: string[];
  assignedRegion: string;
  availabilityStart: string;
  availabilityEnd: string;
  maximumWorkloadMinutes: number;
  status: TechnicianStatus;
}

export interface ServiceRequest {
  id: number;
  title: string;
  region: string;
  requiredSkill: string;
  priority: Priority;
  estimatedDurationMinutes: number;
  preferredStartTime: string | null;
  preferredEndTime: string | null;
  status: RequestStatus;
  createdAt: string;
}

export interface Assignment {
  id: number;
  scheduleVersionId: number;
  requestId: number;
  technicianId: number;
  startTime: string;
  endTime: string;
  status: AssignmentStatus;
}

export interface ProposedAssignment {
  requestId: number;
  technicianId: number;
  startTime: string;
  endTime: string;
}

export interface UnassignedReason {
  requestId: number;
  reason: string;
}

export interface ScheduleProposal {
  assignments: ProposedAssignment[];
  unassignedRequests: UnassignedReason[];
  tradeoffs: string[];
  risks: string[];
}

export interface ScheduleVersion {
  id: number;
  scheduleId: number;
  versionNumber: number;
  createdAt: string;
  createdBy: string;
  reason: ScheduleChangeReason;
}

export interface Schedule {
  id: number;
  scheduleDate: string;
  status: ScheduleStatus;
  currentVersion: number;
}

