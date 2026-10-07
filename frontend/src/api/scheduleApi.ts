import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import type { ScheduleProposal, ScheduleVersion, ScheduleChangeReason, Schedule, Assignment, Technician, ServiceRequest } from '../types';

export interface DailyScheduleResponse {
  schedule: Schedule;
  assignments: Assignment[];
  technicians: Technician[];
  requests: ServiceRequest[];
}

export const scheduleApi = {
  getDailySchedule: async (date: string): Promise<DailyScheduleResponse> => {
    const response = await apiClient.get<DailyScheduleResponse>(`${API_ENDPOINTS.SCHEDULES}/${date}`);
    return response.data;
  },

  generatePlan: async (date: string): Promise<ScheduleProposal> => {
    const response = await apiClient.post<ScheduleProposal>(`${API_ENDPOINTS.SCHEDULES}/${date}/plan`);
    return response.data;
  },

  approvePlan: async (
    scheduleId: number, 
    expectedBaseVersion: number, 
    proposal: ScheduleProposal, 
    actor: string, 
    reason: ScheduleChangeReason
  ): Promise<ScheduleVersion> => {
    const response = await apiClient.post<ScheduleVersion>(`${API_ENDPOINTS.SCHEDULES}/approve`, {
      scheduleId,
      expectedBaseVersion,
      proposal,
      actor,
      reason,
    });
    return response.data;
  }
};
