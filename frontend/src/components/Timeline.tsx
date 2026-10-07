
import type { Technician, Assignment, ServiceRequest } from '../types';
import { differenceInMinutes, parse, getHours, getMinutes } from 'date-fns';
import { Clock, User } from 'lucide-react';

interface TimelineProps {
  technicians: Technician[];
  assignments: Assignment[];
  requests: ServiceRequest[];
}

const START_HOUR = 8;
const END_HOUR = 18;
const TOTAL_MINUTES = (END_HOUR - START_HOUR) * 60;

export default function Timeline({ technicians, assignments, requests }: TimelineProps) {
  
  const getPositionStyles = (startTime: string, endTime: string) => {
    // Assuming HH:MM:SS format
    const start = parse(startTime, 'HH:mm:ss', new Date());
    const end = parse(endTime, 'HH:mm:ss', new Date());
    
    const startMins = getHours(start) * 60 + getMinutes(start) - (START_HOUR * 60);
    const durationMins = differenceInMinutes(end, start);
    
    const leftPercent = Math.max(0, (startMins / TOTAL_MINUTES) * 100);
    const widthPercent = Math.min(100 - leftPercent, (durationMins / TOTAL_MINUTES) * 100);

    return {
      left: `${leftPercent}%`,
      width: `${widthPercent}%`,
    };
  };

  const hours = Array.from({ length: END_HOUR - START_HOUR + 1 }, (_, i) => START_HOUR + i);

  return (
    <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-700 overflow-x-auto transition-colors duration-500">
      <div className="min-w-[800px] p-6">
        <h2 className="text-lg font-bold text-slate-800 dark:text-white mb-6 border-b border-slate-200 dark:border-slate-700 pb-3 flex items-center gap-2">
          <Clock className="text-brand-500" size={20} /> Today's Schedule <span className="text-sm font-normal text-slate-500 dark:text-slate-400 ml-2">(8 AM - 6 PM)</span>
        </h2>
        
        <div className="relative">
          {/* Time Header */}
          <div className="flex ml-48 border-b border-slate-200 dark:border-slate-700 pb-3 mb-6">
            {hours.map(hour => (
              <div key={hour} className="flex-1 text-xs text-slate-400 dark:text-slate-500 font-semibold relative">
                <span className="absolute -left-3">{hour}:00</span>
              </div>
            ))}
          </div>

          {/* Technician Rows */}
          <div className="space-y-5">
            {technicians.map(tech => {
              const techAssignments = assignments.filter(a => a.technicianId === tech.id);

              return (
                <div key={tech.id} className="flex items-center h-16 group">
                  {/* Technician Info */}
                  <div className="w-48 shrink-0 pr-4 flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center shrink-0 transition-colors">
                      <User size={16} className="text-slate-500 dark:text-slate-400" />
                    </div>
                    <div>
                      <div className="font-semibold text-sm text-slate-800 dark:text-slate-200">{tech.name}</div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 font-medium">{tech.assignedRegion}</div>
                    </div>
                  </div>

                  {/* Timeline Track */}
                  <div className="flex-1 h-full bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-700 rounded-xl relative transition-colors duration-500">
                    {/* Vertical grid lines */}
                    {hours.map(hour => (
                      <div key={hour} className="absolute top-0 bottom-0 border-l border-slate-200 dark:border-slate-700" style={{ left: `${((hour - START_HOUR) / (END_HOUR - START_HOUR)) * 100}%` }} />
                    ))}

                    {/* Assignments */}
                    {techAssignments.map(assignment => {
                      const request = requests.find(r => r.id === assignment.requestId);
                      const isConfirmed = assignment.status === 'CONFIRMED';
                      
                      return (
                        <div
                          key={assignment.id}
                          className={`absolute top-1 bottom-1 rounded-lg px-3 py-1.5 text-xs overflow-hidden border shadow-sm transition-all hover:scale-[1.02] hover:shadow-md hover:z-10 cursor-pointer
                            ${isConfirmed 
                              ? 'bg-brand-100 border-brand-200 text-brand-900 dark:bg-brand-900/40 dark:border-brand-700 dark:text-brand-100' 
                              : 'bg-amber-100 border-amber-200 text-amber-900 dark:bg-amber-900/40 dark:border-amber-700 dark:text-amber-100'}
                          `}
                          style={getPositionStyles(assignment.startTime, assignment.endTime)}
                        >
                          <div className="font-bold whitespace-nowrap truncate">{request?.title || `Req #${assignment.requestId}`}</div>
                          <div className="text-[10px] opacity-80 mt-0.5 font-medium">{assignment.startTime.substring(0, 5)} - {assignment.endTime.substring(0, 5)}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
