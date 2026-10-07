export const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const API_ENDPOINTS = {
  TECHNICIANS: "/api/technicians",
  REQUESTS: "/api/requests",
  SCHEDULES: "/api/schedules",
  ASSIGNMENTS: "/api/assignments",
  AUDIT_LOGS: "/api/audit-logs",
  NOTIFICATIONS: "/api/notifications",
} as const;
