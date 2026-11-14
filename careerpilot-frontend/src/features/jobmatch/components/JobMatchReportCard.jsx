import React from 'react';

export const JobMatchReportCard = ({ report, onStartInterview, onGenerateRoadmap }) => {
  if (!report) return null;

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-emerald-400 border-emerald-500/30 bg-emerald-500/10';
    if (score >= 65) return 'text-indigo-400 border-indigo-500/30 bg-indigo-500/10';
    if (score >= 50) return 'text-amber-400 border-amber-500/30 bg-amber-500/10';
    return 'text-rose-400 border-rose-500/30 bg-rose-500/10';
  };

  const getScoreBadgeText = (score) => {
    if (score >= 80) return 'Strong Fit';
    if (score >= 65) return 'Good Match';
    if (score >= 50) return 'Moderate Alignment';
    return 'Substantial Skill Gaps';
  };

  return (
    <div className="space-y-6">
      {/* Top Header & Score Metrics Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-slate-800 pb-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold mb-2 bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              Matched against {report.resumeFileName || 'Resume'}
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              {report.jobTitle}
            </h2>
            {report.companyName && (
              <p className="text-sm font-medium text-slate-400 mt-1">
                Company: <span className="text-slate-200">{report.companyName}</span>
              </p>
            )}
          </div>

          <div className="flex items-center gap-4">
            {/* Overall Match Circle Card */}
            <div className={`p-4 rounded-2xl border text-center min-w-[130px] ${getScoreColor(report.matchScore)}`}>
              <div className="text-xs font-semibold uppercase tracking-wider mb-1">
                Match Score
              </div>
              <div className="text-4xl font-black">{report.matchScore}%</div>
              <div className="text-[11px] font-medium mt-1">
                {getScoreBadgeText(report.matchScore)}
              </div>
            </div>

            {/* Sub-scores */}
            <div className="space-y-2 border-l border-slate-800 pl-4">
              <div>
                <div className="text-xs text-slate-400">Skills Alignment</div>
                <div className="text-lg font-bold text-white">{report.skillsScore}%</div>
              </div>
              <div>
                <div className="text-xs text-slate-400">Experience Alignment</div>
                <div className="text-lg font-bold text-white">{report.experienceScore}%</div>
              </div>
            </div>
          </div>
        </div>

        {/* Executive Summary */}
        {report.matchSummary && (
          <div className="mt-6 bg-slate-950/60 rounded-2xl p-4 sm:p-5 border border-slate-800/80">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Executive Evaluation
            </h4>
            <p className="text-sm sm:text-base text-slate-300 leading-relaxed">
              {report.matchSummary}
            </p>
          </div>
        )}
      </div>

      {/* Skills Matrix Grid: Matched Skills vs Missing Skills */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Matched Skills */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400"></span>
              <h3 className="text-lg font-bold text-white">
                Matched Skills ({report.matchedSkills?.length || 0})
              </h3>
            </div>
            <span className="text-xs text-emerald-400/80 font-medium">Present in Resume</span>
          </div>

          {report.matchedSkills && report.matchedSkills.length > 0 ? (
            <div className="flex flex-wrap gap-2 pt-2">
              {report.matchedSkills.map((skill, idx) => (
                <span
                  key={idx}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold bg-emerald-500/10 text-emerald-300 border border-emerald-500/30"
                >
                  <svg className="w-3.5 h-3.5 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                  </svg>
                  {skill}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-500 italic py-4">
              No direct skill overlaps identified between the parsed resume and this job description.
            </p>
          )}
        </div>

        {/* Missing Skills & Gap Diagnostics */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-rose-400"></span>
              <h3 className="text-lg font-bold text-white">
                Missing Skills & Gaps ({report.missingSkills?.length || 0})
              </h3>
            </div>
            <span className="text-xs text-rose-400/80 font-medium">Required by Role</span>
          </div>

          {report.missingSkills && report.missingSkills.length > 0 ? (
            <div className="space-y-2.5 pt-2 max-h-96 overflow-y-auto pr-1">
              {report.missingSkills.map((gap, idx) => {
                const isRequired = gap.importance === 'REQUIRED';
                return (
                  <div
                    key={idx}
                    className="p-3 rounded-2xl bg-slate-950/70 border border-slate-800/80 hover:border-slate-700 transition-colors"
                  >
                    <div className="flex items-center justify-between gap-2 mb-1">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white">{gap.skillName}</span>
                        {gap.category && (
                          <span className="text-[10px] px-2 py-0.5 rounded-md bg-slate-800 text-slate-400 font-medium uppercase">
                            {gap.category}
                          </span>
                        )}
                      </div>
                      <span
                        className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                          isRequired
                            ? 'bg-rose-500/15 text-rose-400 border border-rose-500/30'
                            : 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                        }`}
                      >
                        {isRequired ? 'Must Have' : 'Preferred'}
                      </span>
                    </div>
                    {gap.recommendation && (
                      <p className="text-xs text-slate-400 leading-normal">
                        💡 {gap.recommendation}
                      </p>
                    )}
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="p-6 text-center text-emerald-400 font-medium text-sm">
              ✨ Great news! No critical missing technical skills detected for this role.
            </div>
          )}
        </div>
      </div>

      {/* Strategic Bridging Recommendations */}
      {report.recommendations && report.recommendations.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-indigo-400"></span>
            <h3 className="text-lg font-bold text-white">Strategic Preparation Next Steps</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-1">
            {report.recommendations.map((rec, idx) => (
              <div
                key={idx}
                className="flex items-start gap-3 p-4 rounded-2xl bg-indigo-950/20 border border-indigo-500/20 text-slate-300 text-sm"
              >
                <span className="w-6 h-6 rounded-lg bg-indigo-600/30 text-indigo-300 flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
                  {idx + 1}
                </span>
                <span className="leading-relaxed">{rec}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Action Shortcuts */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 p-6 rounded-3xl bg-gradient-to-r from-slate-900 via-indigo-950/40 to-slate-900 border border-indigo-500/20 shadow-xl">
        <div>
          <h4 className="text-sm font-bold text-white">Next Steps for {report.jobTitle}</h4>
          <p className="text-xs text-slate-400 mt-0.5">Accelerate your readiness using our specialized AI tools</p>
        </div>
        <div className="flex flex-wrap items-center gap-3 w-full sm:w-auto">
          {onStartInterview && (
            <button
              onClick={() => onStartInterview(report.jobTitle)}
              className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs shadow-md transition-all"
            >
              🎙️ Practice Mock Interview
            </button>
          )}
          {onGenerateRoadmap && (
            <button
              onClick={() => onGenerateRoadmap(report.jobTitle, report.id)}
              className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-md transition-all"
            >
              🗺️ Generate Learning Roadmap
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
