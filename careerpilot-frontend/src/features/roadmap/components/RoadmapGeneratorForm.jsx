import React, { useState } from 'react';
import { roadmapService } from '../../../services/roadmapService';
import { Spinner } from '../../../components/ui/Spinner';
import { Alert } from '../../../components/ui/Alert';

const ROADMAP_PRESETS = [
  { role: 'Senior Java Backend Engineer', weeks: 6 },
  { role: 'Full Stack React & Cloud Engineer', weeks: 8 },
  { role: 'DevOps & Kubernetes Cloud Specialist', weeks: 6 },
  { role: 'Distributed Systems & Microservices Architect', weeks: 10 }
];

export const RoadmapGeneratorForm = ({ onRoadmapGenerated, initialRole = '', matchReports = [] }) => {
  const [targetRole, setTargetRole] = useState(initialRole || 'Senior Java Backend Engineer');
  const [targetWeeks, setTargetWeeks] = useState(6);
  const [selectedMatchReportId, setSelectedMatchReportId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!targetRole.trim()) {
      setError('Please provide a target role for your learning roadmap.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await roadmapService.generateRoadmap({
        targetRole: targetRole.trim(),
        targetWeeks,
        matchReportId: selectedMatchReportId || null
      });

      if (res.success && res.data) {
        onRoadmapGenerated(res.data);
      } else {
        setError(res.message || 'Failed to generate learning roadmap.');
      }
    } catch (err) {
      console.error('Roadmap generation error', err);
      const msg = err.response?.data?.message || err.message || 'Could not generate roadmap.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl">
      <div className="border-b border-slate-800 pb-5">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold mb-2 bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
          🗺️ AI Personalized Learning Roadmap
        </div>
        <h2 className="text-xl sm:text-2xl font-bold text-white">Generate Structured Learning Curriculum</h2>
        <p className="text-xs sm:text-sm text-slate-400 mt-1">
          Bridge your technical gaps with an actionable week-by-week roadmap complete with official documentation, hands-on tasks, and GitHub repositories.
        </p>
      </div>

      {error && <Alert type="error" message={error} />}

      <form onSubmit={handleGenerate} className="space-y-6">
        <div>
          <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
            Target Engineering Role *
          </label>
          <input
            type="text"
            value={targetRole}
            onChange={(e) => setTargetRole(e.target.value)}
            disabled={loading}
            placeholder="e.g. Senior Java Backend Engineer"
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
          />
          <div className="flex flex-wrap gap-2 mt-2.5">
            <span className="text-xs text-slate-500">Quick Roles:</span>
            {ROADMAP_PRESETS.map((p, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => {
                  setTargetRole(p.role);
                  setTargetWeeks(p.weeks);
                }}
                className="text-xs px-2.5 py-0.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-indigo-300 border border-slate-700 transition-colors"
              >
                {p.role} ({p.weeks}w)
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Curriculum Duration (Weeks)
            </label>
            <select
              value={targetWeeks}
              onChange={(e) => setTargetWeeks(Number(e.target.value))}
              disabled={loading}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            >
              <option value={4}>4 Weeks (Accelerated Bootcamp)</option>
              <option value={6}>6 Weeks (Standard Recommended Pace)</option>
              <option value={8}>8 Weeks (Comprehensive Mastery)</option>
              <option value={12}>12 Weeks (Full Senior Transition)</option>
            </select>
          </div>

          {matchReports && matchReports.length > 0 && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Ground in Job Match Diagnostic (Optional)
              </label>
              <select
                value={selectedMatchReportId}
                onChange={(e) => setSelectedMatchReportId(e.target.value)}
                disabled={loading}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              >
                <option value="">General roadmap for role</option>
                {matchReports.map((mr) => (
                  <option key={mr.id} value={mr.id}>
                    Target {mr.jobTitle} (Bridge diagnosed missing skills)
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold text-sm tracking-wide shadow-lg shadow-indigo-600/30 transition-all disabled:opacity-50"
        >
          {loading ? (
            <>
              <Spinner size="sm" />
              <span>Synthesizing Personalized Curriculum...</span>
            </>
          ) : (
            <>
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
              <span>Generate Roadmap</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
};

