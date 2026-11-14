import React from 'react';

export const InterviewSessionReport = ({ session, onStartNewSession }) => {
  if (!session) return null;

  const getScoreBadgeColor = (score) => {
    if (score >= 80) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    if (score >= 65) return 'text-indigo-400 bg-indigo-500/10 border-indigo-500/30';
    if (score >= 50) return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
    return 'text-rose-400 bg-rose-500/10 border-rose-500/30';
  };

  const getReadinessText = (score) => {
    if (score >= 80) return 'Interview Ready (Strong Candidate)';
    if (score >= 65) return 'Solid Foundation (Minor Refinements Needed)';
    if (score >= 50) return 'Moderate Alignment (Needs Core Review)';
    return 'Preparation Recommended (Focus on Gaps)';
  };

  return (
    <div className="space-y-6">
      {/* Hero Overview Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-slate-800 pb-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold mb-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              ✓ Interview Completed
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white">
              {session.targetRole}
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-1">
              Level: <span className="text-slate-200 font-semibold">{session.difficulty}</span> &bull; {session.totalQuestions} Questions Evaluated
            </p>
          </div>

          <div className="flex items-center gap-4">
            <div className={`p-4 rounded-2xl border text-center min-w-[140px] ${getScoreBadgeColor(session.overallScore || 0)}`}>
              <div className="text-xs font-semibold uppercase tracking-wider mb-1">Overall Score</div>
              <div className="text-4xl font-black">{session.overallScore || 0}/100</div>
              <div className="text-[11px] font-medium mt-1">
                {getReadinessText(session.overallScore || 0)}
              </div>
            </div>

            <button
              onClick={onStartNewSession}
              className="px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs transition-all shadow-lg shadow-indigo-600/30"
            >
              Start New Mock
            </button>
          </div>
        </div>

        {/* Executive Feedback Synthesis */}
        {session.overallFeedback && (
          <div className="mt-6 bg-slate-950/60 rounded-2xl p-4 sm:p-5 border border-slate-800/80">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Executive Performance Synthesis
            </h4>
            <p className="text-sm sm:text-base text-slate-300 leading-relaxed">
              {session.overallFeedback}
            </p>
          </div>
        )}
      </div>

      {/* Transcript of Questions & Answers */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-white">Detailed Question Breakdown</h3>

        {session.questions && session.questions.map((q, idx) => (
          <div
            key={q.id || idx}
            className="bg-slate-900 border border-slate-800 rounded-3xl p-6 space-y-4 shadow-lg"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider block mb-1">
                  Question #{q.questionIndex}
                </span>
                <h4 className="text-base font-bold text-white">{q.questionText}</h4>
              </div>
              <span className={`px-3 py-1 rounded-xl text-xs font-black border shrink-0 ${getScoreBadgeColor(q.score || 0)}`}>
                {q.score || 0}/100
              </span>
            </div>

            {q.userAnswer && (
              <div className="bg-slate-950/70 p-3.5 rounded-2xl border border-slate-800/80">
                <span className="text-xs font-semibold text-slate-400 block mb-1">Candidate Answer:</span>
                <p className="text-xs sm:text-sm text-slate-300 whitespace-pre-wrap">{q.userAnswer}</p>
              </div>
            )}

            {q.feedback && (
              <div className="text-xs sm:text-sm text-slate-300 bg-indigo-950/20 p-3.5 rounded-2xl border border-indigo-500/20">
                💡 <span className="font-semibold text-indigo-300">Feedback: </span>
                {q.feedback}
              </div>
            )}

            {q.modelAnswer && (
              <div className="text-xs sm:text-sm text-slate-300 bg-emerald-950/20 p-3.5 rounded-2xl border border-emerald-500/20">
                🌟 <span className="font-semibold text-emerald-400">Model Answer: </span>
                {q.modelAnswer}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

