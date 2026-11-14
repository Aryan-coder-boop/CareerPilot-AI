import React, { useState } from 'react';
import { roadmapService } from '../../../services/roadmapService';
import { Spinner } from '../../../components/ui/Spinner';

export const RoadmapViewer = ({ roadmap, onMilestoneUpdated }) => {
  const [updatingId, setUpdatingId] = useState(null);

  if (!roadmap) return null;

  const handleStatusChange = async (milestoneId, newStatus) => {
    try {
      setUpdatingId(milestoneId);
      const res = await roadmapService.updateMilestoneStatus(milestoneId, newStatus);
      if (res.success && res.data) {
        onMilestoneUpdated(milestoneId, res.data);
      }
    } catch (err) {
      console.error('Failed to update milestone status', err);
    } finally {
      setUpdatingId(null);
    }
  };

  const completedCount = roadmap.milestones
    ? roadmap.milestones.filter((m) => m.status === 'COMPLETED').length
    : 0;
  const totalCount = roadmap.milestones ? roadmap.milestones.length : 0;
  const progressPct = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

  return (
    <div className="space-y-6">
      {/* Roadmap Header & Progress Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-slate-800 pb-6 mb-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold mb-2 bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              Active Learning Path
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white">
              {roadmap.targetRole}
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Estimated Duration: <span className="text-slate-200 font-semibold">{roadmap.estimatedWeeks} Weeks</span> &bull; {completedCount} of {totalCount} Milestones Completed
            </p>
          </div>

          <div className="flex items-center gap-4">
            <div className="p-4 rounded-2xl bg-indigo-950/30 border border-indigo-500/30 text-center min-w-[130px]">
              <div className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Progress
              </div>
              <div className="text-3xl font-black text-indigo-400">{progressPct}%</div>
              <div className="text-[11px] text-slate-400 font-medium mt-1">
                {progressPct === 100 ? 'Mastered 🎉' : `${completedCount}/${totalCount} Done`}
              </div>
            </div>
          </div>
        </div>

        {/* Global Progress Bar */}
        <div className="w-full bg-slate-950 rounded-full h-2.5 overflow-hidden border border-slate-800">
          <div
            className="bg-gradient-to-r from-indigo-500 via-indigo-400 to-emerald-400 h-full transition-all duration-500 rounded-full"
            style={{ width: `${progressPct}%` }}
          ></div>
        </div>
      </div>

      {/* Week-by-Week Milestone Timeline */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-white">Curriculum Timeline</h3>

        <div className="space-y-4">
          {roadmap.milestones && roadmap.milestones.map((m) => {
            const isCompleted = m.status === 'COMPLETED';
            const isInProgress = m.status === 'IN_PROGRESS';
            const isPending = m.status === 'PENDING';
            const isBusy = updatingId === m.id;

            return (
              <div
                key={m.id}
                className={`bg-slate-900 border rounded-3xl p-6 sm:p-7 transition-all ${
                  isCompleted
                    ? 'border-emerald-500/40 bg-slate-900/90'
                    : isInProgress
                    ? 'border-indigo-500/50 shadow-lg shadow-indigo-500/5'
                    : 'border-slate-800'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 border-b border-slate-800/80 pb-4">
                  <div className="flex items-start gap-3">
                    <span
                      className={`w-9 h-9 rounded-2xl flex items-center justify-center font-black text-xs shrink-0 mt-0.5 border ${
                        isCompleted
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                          : isInProgress
                          ? 'bg-indigo-600 text-white border-indigo-400 shadow-md shadow-indigo-600/30'
                          : 'bg-slate-950 text-slate-400 border-slate-800'
                      }`}
                    >
                      W{m.weekNumber}
                    </span>
                    <div>
                      <h4 className="text-base sm:text-lg font-bold text-white">{m.title}</h4>
                      <span className="text-xs text-slate-400">Week {m.weekNumber} Milestone</span>
                    </div>
                  </div>

                  {/* Status Toggle Buttons */}
                  <div className="flex items-center gap-1.5 self-start">
                    {isBusy ? (
                      <Spinner size="sm" />
                    ) : (
                      <>
                        <button
                          type="button"
                          onClick={() => handleStatusChange(m.id, 'PENDING')}
                          className={`text-xs px-2.5 py-1 rounded-lg border font-semibold transition-all ${
                            isPending
                              ? 'bg-slate-800 text-slate-300 border-slate-700'
                              : 'bg-slate-950/60 text-slate-500 border-slate-800 hover:text-slate-300'
                          }`}
                        >
                          To-Do
                        </button>
                        <button
                          type="button"
                          onClick={() => handleStatusChange(m.id, 'IN_PROGRESS')}
                          className={`text-xs px-2.5 py-1 rounded-lg border font-semibold transition-all ${
                            isInProgress
                              ? 'bg-indigo-600 text-white border-indigo-500'
                              : 'bg-slate-950/60 text-slate-500 border-slate-800 hover:text-indigo-400'
                          }`}
                        >
                          In Progress
                        </button>
                        <button
                          type="button"
                          onClick={() => handleStatusChange(m.id, 'COMPLETED')}
                          className={`text-xs px-2.5 py-1 rounded-lg border font-semibold transition-all ${
                            isCompleted
                              ? 'bg-emerald-600 text-white border-emerald-500 shadow-md shadow-emerald-600/20'
                              : 'bg-slate-950/60 text-slate-500 border-slate-800 hover:text-emerald-400'
                          }`}
                        >
                          Completed ✓
                        </button>
                      </>
                    )}
                  </div>
                </div>

                <p className="text-sm text-slate-300 mt-4 leading-relaxed whitespace-pre-wrap">
                  {m.description}
                </p>

                {/* Curated Resources */}
                {m.resources && m.resources.length > 0 && (
                  <div className="mt-5 pt-4 border-t border-slate-800/80">
                    <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-2">
                      Curated Learning Resources
                    </span>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      {m.resources.map((res, rIdx) => (
                        <a
                          key={res.id || rIdx}
                          href={res.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center justify-between p-3 rounded-xl bg-slate-950 border border-slate-800 hover:border-indigo-500/60 hover:bg-slate-900/60 text-xs transition-all group"
                        >
                          <span className="font-semibold text-slate-200 group-hover:text-indigo-300 truncate mr-2">
                            🔗 {res.title}
                          </span>
                          <span className="text-[10px] px-2 py-0.5 rounded-md bg-slate-800 text-slate-400 uppercase font-mono shrink-0">
                            {res.resourceType}
                          </span>
                        </a>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

