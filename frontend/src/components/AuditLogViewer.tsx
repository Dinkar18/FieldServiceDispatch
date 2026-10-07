import { useEffect, useState } from 'react';
import { apiClient } from '../api/apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import { History } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface AuditLog {
  id: number;
  timestamp: string;
  details: string;
  action: string;
  actor: string;
  entityType: string;
}

export function AuditLogViewer() {
  const [logs, setLogs] = useState<AuditLog[]>([]);

  const fetchLogs = async () => {
    try {
      const response = await apiClient.get<AuditLog[]>(API_ENDPOINTS.AUDIT_LOGS);
      setLogs(response.data.reverse()); // Show newest first
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchLogs();
    const interval = setInterval(fetchLogs, 5000);
    return () => clearInterval(interval);
  }, []);

  if (logs.length === 0) return null;

  return (
    <div className="mt-8 bg-slate-800 p-6 rounded-2xl border border-slate-700 shadow-sm">
      <h3 className="text-lg font-bold text-white mb-5 flex items-center gap-2">
        <History className="text-brand-500" size={20} />
        System Audit & Version History
      </h3>
      <div className="space-y-3 max-h-60 overflow-y-auto pr-2">
        <AnimatePresence initial={false}>
          {logs.map((log) => (
            <motion.div 
              key={log.id} 
              initial={{ opacity: 0, height: 0, scale: 0.95 }}
              animate={{ opacity: 1, height: 'auto', scale: 1 }}
              transition={{ duration: 0.3, type: "spring", bounce: 0.3 }}
              className="flex gap-4 text-sm p-3 bg-slate-700/50 rounded-lg border border-slate-600"
            >
              <div className="text-slate-500 whitespace-nowrap font-mono text-xs mt-0.5 shrink-0">
                {new Date(log.timestamp).toLocaleTimeString()}
              </div>
              <div>
                <span className="font-semibold text-slate-300">{log.actor}</span>
                <span className="mx-2 text-slate-600">|</span>
                <span className="text-emerald-400 font-medium bg-emerald-900/30 px-2 py-0.5 rounded text-xs">{log.action}</span>
                <p className="text-slate-400 mt-1">{log.details}</p>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </div>
  );
}
