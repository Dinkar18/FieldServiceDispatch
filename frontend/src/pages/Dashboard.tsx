import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { scheduleApi } from '../api/scheduleApi';
import type { DailyScheduleResponse } from '../api/scheduleApi';
import type { ScheduleProposal } from '../types';
import { AlertCircle, CheckCircle2, Play, Calendar, Zap, AlertTriangle, ShieldCheck } from 'lucide-react';
import Timeline from '../components/Timeline';
import { AuditLogViewer } from '../components/AuditLogViewer';

export default function Dashboard() {
  const [proposal, setProposal] = useState<ScheduleProposal | null>(null);
  const [dailyData, setDailyData] = useState<DailyScheduleResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const targetDate = new Date().toLocaleDateString('en-CA');

  useEffect(() => {
    fetchSchedule();
  }, []);

  const fetchSchedule = async () => {
    try {
      const data = await scheduleApi.getDailySchedule(targetDate);
      setDailyData(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch schedule data');
    }
  };

  const handleGeneratePlan = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await scheduleApi.generatePlan(targetDate);
      setProposal(result);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to generate plan');
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!proposal || !dailyData?.schedule) return;
    try {
      await scheduleApi.approvePlan(
        dailyData.schedule.id,
        dailyData.schedule.currentVersion,
        proposal,
        'Dispatcher John',
        'INITIAL_PLAN'
      );
      setProposal(null);
      await fetchSchedule();
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to approve plan');
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 p-4 md:p-8 font-sans text-slate-100">
      <div className="max-w-7xl mx-auto">
        <header className="mb-6 md:mb-8 flex flex-col md:flex-row items-start md:items-center justify-between bg-slate-800 p-5 md:p-6 rounded-2xl shadow-sm border border-slate-700 gap-4 md:gap-0">
          <div>
            <h1 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2">
              <Zap className="text-brand-500" /> Dispatch Command Center
            </h1>
            <p className="text-slate-400 text-xs md:text-sm flex flex-wrap items-center gap-2 mt-2 font-medium">
              <Calendar size={16} />
              {targetDate} <span className="text-slate-600">|</span> <span className="bg-slate-700 text-slate-300 px-2 py-0.5 rounded-full text-xs">Version {dailyData?.schedule?.currentVersion || 1}</span>
            </p>
          </div>
          
          <div className="flex items-center gap-3 w-full md:w-auto">
            <motion.button 
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={handleGeneratePlan}
              disabled={isLoading || proposal !== null}
              className="flex-1 md:flex-none flex items-center justify-center gap-2 bg-brand-600 hover:bg-brand-500 text-white px-6 py-3 rounded-xl shadow-md transition-all font-semibold disabled:opacity-50 disabled:cursor-not-allowed border border-brand-500"
            >
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin shrink-0" />
              ) : (
                <Play size={18} fill="currentColor" className="shrink-0" />
              )}
              <span className="whitespace-nowrap">{isLoading ? 'AI is Planning...' : 'Generate AI Plan'}</span>
            </motion.button>
          </div>
        </header>

      <AnimatePresence>
        {error && (
          <motion.div 
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="mb-6 bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-r-lg flex items-center gap-3 shadow-sm"
          >
            <AlertCircle size={20} className="text-red-500" />
            <span className="font-medium">{error}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Timeline */}
      {dailyData ? (
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <Timeline 
            technicians={dailyData.technicians} 
            assignments={dailyData.assignments} 
            requests={dailyData.requests || []} 
          />
        </motion.div>
      ) : (
        <div className="mb-8 h-64 bg-slate-800 rounded-2xl animate-pulse flex items-center justify-center border border-slate-700">
          <span className="text-slate-400 font-medium flex items-center gap-2"><div className="w-4 h-4 border-2 border-slate-400 border-t-transparent rounded-full animate-spin" /> Loading Schedule Data...</span>
        </div>
      )}

      {/* AI Proposal Panel */}
      <AnimatePresence>
        {proposal && (
          <motion.div 
            initial={{ opacity: 0, y: 40 }} 
            animate={{ opacity: 1, y: 0 }} 
            exit={{ opacity: 0, y: 40 }}
            className="grid grid-cols-1 xl:grid-cols-3 gap-8"
          >
            <div className="xl:col-span-2 space-y-6">
              <div className="bg-slate-800 rounded-2xl shadow-sm border border-slate-700 overflow-hidden">
                <div className="bg-slate-700/50 px-6 py-4 border-b border-slate-700 flex items-center gap-2">
                  <ShieldCheck className="text-brand-500" size={20} />
                  <h2 className="text-lg font-bold text-white">AI Proposed Assignments</h2>
                </div>
                <div className="p-6">
                  {proposal.assignments.length === 0 ? (
                    <div className="text-center py-8 text-slate-400 bg-slate-800 rounded-lg border border-dashed border-slate-600">
                      No assignments could be made under current constraints.
                    </div>
                  ) : (
                    <ul className="space-y-3">
                      {proposal.assignments.map((a, idx) => {
                        const tech = dailyData?.technicians.find(t => t.id === a.technicianId);
                        const req = dailyData?.requests?.find(r => r.id === a.requestId);
                        return (
                          <motion.li 
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: idx * 0.1 }}
                            key={idx} 
                            className="flex flex-col sm:flex-row justify-between sm:items-center bg-slate-700/30 hover:bg-slate-700/50 p-4 rounded-lg border border-slate-600 shadow-sm transition-colors gap-3"
                          >
                            <div className="flex items-center gap-3">
                              <span className="bg-brand-900/50 text-brand-300 border border-brand-700/50 font-bold px-3 py-1 rounded-md text-sm">Req #{a.requestId}</span>
                              <span className="font-semibold text-slate-200">{req?.title}</span>
                            </div>
                            <div className="flex items-center gap-4 text-sm font-medium">
                              <span className="text-slate-400">Assign to <span className="text-white font-bold">{tech?.name.split(' ')[0]}</span></span>
                              <span className="bg-slate-900 text-slate-200 border border-slate-700 px-3 py-1 rounded-md tracking-wider">
                                {a.startTime.substring(0,5)} - {a.endTime.substring(0,5)}
                              </span>
                            </div>
                          </motion.li>
                        );
                      })}
                    </ul>
                  )}
                </div>
              </div>

              {proposal.unassignedRequests.length > 0 && (
                <div className="bg-slate-800 rounded-2xl shadow-sm border border-red-900/50 overflow-hidden">
                  <div className="bg-red-950/30 px-6 py-4 border-b border-red-900/30 flex items-center gap-2">
                    <AlertTriangle className="text-red-500" size={20} />
                    <h2 className="text-lg font-bold text-red-400">Unassigned Requests & Constraint Violations</h2>
                  </div>
                  <div className="p-6">
                    <ul className="space-y-3">
                      {proposal.unassignedRequests.map((u, idx) => (
                        <motion.li 
                          initial={{ opacity: 0 }}
                          animate={{ opacity: 1 }}
                          transition={{ delay: idx * 0.1 }}
                          key={idx} 
                          className="flex items-start gap-3 bg-red-950/20 p-4 rounded-lg border border-red-900/30"
                        >
                          <AlertCircle size={18} className="text-red-500 shrink-0 mt-0.5" />
                          <div className="text-sm">
                            <span className="font-bold text-white">Request #{u.requestId}</span>
                            <p className="text-red-300 mt-1">{u.reason}</p>
                          </div>
                        </motion.li>
                      ))}
                    </ul>
                  </div>
                </div>
              )}
            </div>

            <div className="space-y-6">
              <div className="bg-slate-800 rounded-2xl shadow-sm border border-slate-700 p-6">
                <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                  <Zap className="text-amber-500" /> AI Reasoning
                </h2>
                
                <div className="mb-6">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">Trade-offs Made</h3>
                  <ul className="space-y-2">
                    {proposal.tradeoffs.map((t, i) => (
                      <li key={i} className="text-sm text-slate-300 bg-slate-700/30 p-3 rounded-lg border border-slate-600 leading-relaxed">
                        {t}
                      </li>
                    ))}
                  </ul>
                </div>

                <div>
                  <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">Identified Risks</h3>
                  <ul className="space-y-2">
                    {proposal.risks.map((r, i) => (
                      <li key={i} className="text-sm text-amber-300 bg-amber-950/30 p-3 rounded-lg border border-amber-900/30 leading-relaxed">
                        {r}
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-3">
                <motion.button 
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={handleApprove}
                  className="col-span-2 bg-brand-600 hover:bg-brand-500 text-white font-bold py-4 rounded-xl shadow-md flex items-center justify-center gap-2 transition-colors border border-brand-500"
                >
                  <CheckCircle2 size={20} />
                  Approve & Confirm Plan
                </motion.button>
                
                <motion.button 
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => setProposal(null)}
                  className="col-span-2 bg-slate-700 hover:bg-slate-600 text-white font-bold py-3 rounded-xl shadow-sm border border-slate-600 transition-colors"
                >
                  Reject Proposal
                </motion.button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <AuditLogViewer />
      </div>
    </div>
  );
}
