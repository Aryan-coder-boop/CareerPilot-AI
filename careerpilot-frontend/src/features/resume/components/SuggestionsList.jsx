import React from 'react';

export const SuggestionsList = ({ strengths = [], suggestions = [] }) => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Strengths */}
      <div className="p-6 rounded-3xl bg-slate-900 border border-slate-800 space-y-4">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-white">Demonstrated Strengths</h3>
            <p className="text-xs text-slate-400">What stands out in this candidate profile</p>
          </div>
        </div>

        {strengths && strengths.length > 0 ? (
          <ul className="space-y-3 pt-2">
            {strengths.map((strength, index) => (
              <li key={index} className="flex items-start gap-3 text-sm text-slate-300">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mt-2 shrink-0"></span>
                <span>{strength}</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-xs text-slate-400 italic">No specific strengths highlighted.</p>
        )}
      </div>

      {/* Improvement Suggestions */}
      <div className="p-6 rounded-3xl bg-slate-900 border border-slate-800 space-y-4">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-xl bg-indigo-500/10 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-white">Targeted Improvements</h3>
            <p className="text-xs text-slate-400">Actionable steps to elevate interview callbacks</p>
          </div>
        </div>

        {suggestions && suggestions.length > 0 ? (
          <div className="space-y-4 pt-2">
            {suggestions.map((item, index) => (
              <div key={index} className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 rounded-md bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-[11px] font-semibold">
                    {item.category || 'Optimization'}
                  </span>
                </div>
                {item.issue && <p className="text-xs text-slate-400 font-medium">{item.issue}</p>}
                {item.recommendation && (
                  <p className="text-xs text-slate-200 leading-relaxed font-semibold">
                    &rarr; {item.recommendation}
                  </p>
                )}
                {item.example && (
                  <div className="p-2.5 rounded-xl bg-slate-900/90 border border-slate-800 text-[11px] text-indigo-300 font-mono">
                    <span className="text-slate-400 select-none">Example: </span>
                    {item.example}
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-400 italic">No critical improvements needed.</p>
        )}
      </div>
    </div>
  );
};

