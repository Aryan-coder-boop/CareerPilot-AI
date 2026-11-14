import React from 'react';

export const RoadmapHistoryList = ({ roadmaps, onSelectRoadmap, activeRoadmapId }) => {
  if (!roadmaps || roadmaps.length === 0) return null;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h3 className="text-lg font-bold text-white">Your Learning Roadmaps</h3>
          <p className="text-xs text-slate-400">Past generated curriculums and learning milestones</p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-800 text-slate-400">
          {roadmaps.length} roadmaps
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {roadmaps.map((item) => {
          const isSelected = activeRoadmapId === item.id;
          return (
            <div
              key={item.id}
              onClick={() => onSelectRoadmap(item.id)}
              className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                isSelected
                  ? 'bg-indigo-950/40 border-indigo-500/60 shadow-lg shadow-indigo-500/10'
                  : 'bg-slate-950/60 border-slate-800/80 hover:border-slate-700 hover:bg-slate-900/60'
              }`}
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <div className="truncate">
                  <h4 className="font-bold text-sm text-white truncate" title={item.targetRole}>
                    {item.targetRole}
                  </h4>
                  <div className="flex items-center gap-1.5 mt-1">
                    <span className="text-xs text-slate-400">
                      {item.estimatedWeeks} Weeks &bull; {item.completedMilestones}/{item.totalMilestones} Completed
                    </span>
                  </div>
                </div>

                <span
                  className={`text-xs font-black px-2 py-1 rounded-lg border shrink-0 ${
                    item.progressPercentage === 100
                      ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20'
                      : 'text-indigo-400 bg-indigo-500/10 border-indigo-500/20'
                  }`}
                >
                  {item.progressPercentage}%
                </span>
              </div>

              {/* Mini progress bar */}
              <div className="w-full bg-slate-900 rounded-full h-1.5 overflow-hidden mt-3">
                <div
                  className="bg-indigo-500 h-full rounded-full"
                  style={{ width: `${item.progressPercentage}%` }}
                ></div>
              </div>

              <div className="text-[11px] text-slate-500 mt-2.5 flex justify-between">
                <span>{item.status}</span>
                <span>{new Date(item.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

