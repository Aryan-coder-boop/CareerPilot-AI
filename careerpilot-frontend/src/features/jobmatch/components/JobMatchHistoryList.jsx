import React from 'react';

export const JobMatchHistoryList = ({ matches, onSelectMatch, activeMatchId }) => {
  if (!matches || matches.length === 0) {
    return null;
  }

  const getScoreBadgeColor = (score) => {
    if (score >= 80) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
    if (score >= 65) return 'text-indigo-400 bg-indigo-500/10 border-indigo-500/20';
    if (score >= 50) return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/20';
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h3 className="text-lg font-bold text-white">Target Job Matches History</h3>
          <p className="text-xs text-slate-400">Past job description evaluations and match analyses</p>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-800 text-slate-400">
          {matches.length} analyzed
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {matches.map((item) => {
          const isSelected = activeMatchId === item.id;
          return (
            <div
              key={item.id}
              onClick={() => onSelectMatch(item.id)}
              className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                isSelected
                  ? 'bg-indigo-950/40 border-indigo-500/60 shadow-lg shadow-indigo-500/10'
                  : 'bg-slate-950/60 border-slate-800/80 hover:border-slate-700 hover:bg-slate-900/60'
              }`}
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <div className="truncate">
                  <h4 className="font-bold text-sm text-white truncate" title={item.jobTitle}>
                    {item.jobTitle}
                  </h4>
                  {item.companyName && (
                    <p className="text-xs text-slate-400 truncate">{item.companyName}</p>
                  )}
                </div>
                <span className={`text-xs font-black px-2 py-1 rounded-lg border shrink-0 ${getScoreBadgeColor(item.matchScore)}`}>
                  {item.matchScore}%
                </span>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500 mt-3 pt-2 border-t border-slate-900">
                <span className="truncate max-w-[140px]" title={item.resumeFileName}>
                  📄 {item.resumeFileName}
                </span>
                <span>{new Date(item.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

