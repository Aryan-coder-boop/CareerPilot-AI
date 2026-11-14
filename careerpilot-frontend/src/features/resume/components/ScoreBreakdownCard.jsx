import React from 'react';

export const ScoreBreakdownCard = ({ analysis, fileName }) => {
  if (!analysis) return null;

  const score = analysis.qualityScore ?? 0;
  const breakdown = analysis.scoreBreakdown || {
    impactMetricsScore: 0,
    actionVerbsScore: 0,
    formattingClarityScore: 0,
    sectionCompletenessScore: 0,
    totalScore: score,
  };

  const getScoreColor = (val) => {
    if (val >= 80) return 'text-emerald-400 border-emerald-500/40 bg-emerald-500/10';
    if (val >= 60) return 'text-indigo-400 border-indigo-500/40 bg-indigo-500/10';
    return 'text-amber-400 border-amber-500/40 bg-amber-500/10';
  };

  return (
    <div className="p-6 sm:p-8 rounded-3xl bg-slate-900 border border-slate-800 shadow-2xl space-y-6">
      {/* Header & Candidate Summary */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-indigo-400">
              Resume Diagnostic Report
            </span>
            <span className="text-slate-600">&bull;</span>
            <span className="text-xs text-slate-400 truncate max-w-xs">{fileName}</span>
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            {analysis.candidateName || 'Candidate Profile'}
          </h2>
          <div className="flex flex-wrap items-center gap-3 mt-2 text-xs text-slate-300">
            {analysis.email && (
              <span className="flex items-center gap-1.5 bg-slate-800/80 px-2.5 py-1 rounded-lg">
                <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                {analysis.email}
              </span>
            )}
            {analysis.phone && (
              <span className="flex items-center gap-1.5 bg-slate-800/80 px-2.5 py-1 rounded-lg">
                <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                {analysis.phone}
              </span>
            )}
            {analysis.location && (
              <span className="flex items-center gap-1.5 bg-slate-800/80 px-2.5 py-1 rounded-lg">
                <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {analysis.location}
              </span>
            )}
          </div>
        </div>

        {/* Big Overall Quality Score */}
        <div className="flex items-center gap-4 bg-slate-950/70 border border-slate-800 p-4 rounded-2xl">
          <div className="text-right">
            <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              Resume Quality Score
            </div>
            <div className="text-xs text-slate-400">Diagnostic Estimate</div>
          </div>
          <div className={`w-16 h-16 rounded-2xl border-2 flex items-center justify-center font-extrabold text-2xl ${getScoreColor(score)}`}>
            {score}
          </div>
        </div>
      </div>

      {/* Mandatory Transparent Disclaimer Banner */}
      <div className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800/80 flex items-start gap-3 text-xs text-slate-400">
        <svg className="w-4 h-4 text-indigo-400 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p>
          <span className="font-semibold text-slate-300">Diagnostic Disclaimer:</span> {analysis.disclaimer || 'The CareerPilot Quality Score is an estimated diagnostic metric (0-100) based on action verbs, impact quantification, structure completeness, and clarity. It is not an official or proprietary ATS score.'}
        </p>
      </div>

      {/* 4-Pillar Score Breakdown */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800">
          <div className="text-xs font-semibold text-slate-400">Impact Metrics</div>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-xl font-bold text-white">{breakdown.impactMetricsScore}</span>
            <span className="text-xs text-slate-400">/ 25</span>
          </div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: `${(breakdown.impactMetricsScore / 25) * 100}%` }}></div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800">
          <div className="text-xs font-semibold text-slate-400">Action Verbs</div>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-xl font-bold text-white">{breakdown.actionVerbsScore}</span>
            <span className="text-xs text-slate-400">/ 25</span>
          </div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: `${(breakdown.actionVerbsScore / 25) * 100}%` }}></div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800">
          <div className="text-xs font-semibold text-slate-400">Formatting & Clarity</div>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-xl font-bold text-white">{breakdown.formattingClarityScore}</span>
            <span className="text-xs text-slate-400">/ 25</span>
          </div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: `${(breakdown.formattingClarityScore / 25) * 100}%` }}></div>
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800">
          <div className="text-xs font-semibold text-slate-400">Completeness</div>
          <div className="flex items-baseline gap-1 mt-1">
            <span className="text-xl font-bold text-white">{breakdown.sectionCompletenessScore}</span>
            <span className="text-xs text-slate-400">/ 25</span>
          </div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-indigo-500 h-1.5 rounded-full" style={{ width: `${(breakdown.sectionCompletenessScore / 25) * 100}%` }}></div>
          </div>
        </div>
      </div>

      {/* Executive Summary */}
      {analysis.summary && (
        <div className="p-4 rounded-2xl bg-slate-950/40 border border-slate-800/80">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
            Executive Summary
          </div>
          <p className="text-sm text-slate-300 leading-relaxed">{analysis.summary}</p>
        </div>
      )}
    </div>
  );
};

