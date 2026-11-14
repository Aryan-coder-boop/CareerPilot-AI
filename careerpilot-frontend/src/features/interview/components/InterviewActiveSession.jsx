import React, { useState } from 'react';
import { interviewService } from '../../../services/interviewService';
import { Spinner } from '../../../components/ui/Spinner';
import { Alert } from '../../../components/ui/Alert';

export const InterviewActiveSession = ({ session, onSessionUpdated, onFinishSession }) => {
  const [currentIdx, setCurrentIdx] = useState(() => {
    const unanswered = session.questions.findIndex((q) => !q.answered);
    return unanswered !== -1 ? unanswered : 0;
  });

  const [answerText, setAnswerText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [lastEvaluation, setLastEvaluation] = useState(null);
  const [error, setError] = useState(null);

  const currentQuestion = session.questions[currentIdx] || null;
  const isAnswered = currentQuestion?.answered || false;

  const handleSubmitAnswer = async (e) => {
    e.preventDefault();
    if (!answerText.trim() || answerText.trim().length < 5) {
      setError('Please provide a substantive answer (at least 5 characters).');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const res = await interviewService.submitAnswer(session.id, {
        questionIndex: currentQuestion.questionIndex,
        answerText: answerText.trim()
      });

      if (res.success && res.data) {
        setLastEvaluation(res.data);

        // Fetch refreshed session detail
        const updated = await interviewService.getSessionDetail(session.id);
        if (updated.success && updated.data) {
          onSessionUpdated(updated.data);
          if (res.data.isSessionFinished) {
            onFinishSession(updated.data);
          }
        }
      } else {
        setError(res.message || 'Evaluation submission failed.');
      }
    } catch (err) {
      console.error('Answer submission error', err);
      const msg = err.response?.data?.message || err.message || 'Failed to submit answer.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleNext = () => {
    setLastEvaluation(null);
    setAnswerText('');
    if (currentIdx + 1 < session.questions.length) {
      setCurrentIdx(currentIdx + 1);
    } else {
      onFinishSession(session);
    }
  };

  const progressPct = Math.round(
    (session.questions.filter((q) => q.answered).length / session.totalQuestions) * 100
  );

  return (
    <div className="space-y-6">
      {/* Session Progress Header */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4 mb-4">
          <div>
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
              <span className="text-xs font-bold uppercase tracking-wider text-indigo-400">
                Mock Session in Progress
              </span>
            </div>
            <h3 className="text-xl font-extrabold text-white mt-1">
              {session.targetRole}
              <span className="ml-2 text-xs font-semibold px-2.5 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700">
                {session.difficulty}
              </span>
            </h3>
          </div>

          <div className="text-right">
            <span className="text-xs text-slate-400">
              Question {currentIdx + 1} of {session.totalQuestions}
            </span>
            <div className="text-sm font-bold text-white mt-0.5">{progressPct}% Completed</div>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="w-full bg-slate-950 rounded-full h-2 overflow-hidden border border-slate-800">
          <div
            className="bg-gradient-to-r from-indigo-500 to-emerald-400 h-full transition-all duration-500 rounded-full"
            style={{ width: `${progressPct}%` }}
          ></div>
        </div>

        {/* Question Selector Tabs */}
        <div className="flex flex-wrap gap-2 mt-4">
          {session.questions.map((q, idx) => (
            <button
              key={q.id || idx}
              type="button"
              onClick={() => {
                setCurrentIdx(idx);
                setLastEvaluation(null);
                setAnswerText(q.userAnswer || '');
              }}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold border transition-all ${
                currentIdx === idx
                  ? 'bg-indigo-600 text-white border-indigo-500 shadow-md shadow-indigo-600/20'
                  : q.answered
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                  : 'bg-slate-950 text-slate-400 border-slate-800 hover:text-white'
              }`}
            >
              Q{idx + 1} {q.answered ? '✓' : ''}
            </button>
          ))}
        </div>
      </div>

      {error && <Alert type="error" message={error} />}

      {/* Current Question Card */}
      {currentQuestion && (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl">
          <div>
            <div className="text-xs font-bold text-indigo-400 uppercase tracking-wider mb-2">
              Interviewer Prompt #{currentQuestion.questionIndex}
            </div>
            <h4 className="text-lg sm:text-xl font-bold text-white leading-relaxed">
              {currentQuestion.questionText}
            </h4>

            {currentQuestion.expectedConcepts && currentQuestion.expectedConcepts.length > 0 && (
              <div className="flex flex-wrap items-center gap-1.5 mt-4 pt-3 border-t border-slate-800/80">
                <span className="text-[11px] text-slate-500 font-semibold mr-1">Focus Concepts:</span>
                {currentQuestion.expectedConcepts.map((concept, i) => (
                  <span
                    key={i}
                    className="text-[11px] font-medium px-2.5 py-0.5 rounded-md bg-slate-950 text-slate-300 border border-slate-800"
                  >
                    {concept}
                  </span>
                ))}
              </div>
            )}
          </div>

          {/* Answer Input or Previous Answer View */}
          {!isAnswered ? (
            <form onSubmit={handleSubmitAnswer} className="space-y-4">
              <div>
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                    Your Technical Answer
                  </label>
                  <span className="text-xs text-slate-500">
                    {answerText.length} characters
                  </span>
                </div>
                <textarea
                  rows={6}
                  value={answerText}
                  onChange={(e) => setAnswerText(e.target.value)}
                  disabled={submitting}
                  placeholder="Articulate your technical response, trade-offs, architecture choices, and relevant operational considerations..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-4 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-sans"
                />
              </div>

              <div className="flex items-center justify-between">
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold text-sm shadow-lg shadow-indigo-600/30 transition-all disabled:opacity-50"
                >
                  {submitting ? (
                    <>
                      <Spinner size="sm" />
                      <span>Evaluating Answer with AI...</span>
                    </>
                  ) : (
                    <>
                      <span>Submit Answer & Evaluate</span>
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                      </svg>
                    </>
                  )}
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-4 pt-2">
              <div className="bg-slate-950/80 rounded-2xl p-4 border border-slate-800">
                <span className="text-xs font-semibold text-slate-400 block mb-1">Your Submitted Response:</span>
                <p className="text-sm text-slate-200 leading-relaxed whitespace-pre-wrap">
                  {currentQuestion.userAnswer}
                </p>
              </div>

              {/* Evaluation Card for Answered Question */}
              <div className="p-5 rounded-2xl bg-indigo-950/20 border border-indigo-500/30 space-y-4">
                <div className="flex items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="px-3 py-1.5 rounded-xl bg-indigo-500/20 border border-indigo-500/40 text-indigo-300 font-black text-lg">
                      {currentQuestion.score}/100
                    </div>
                    <div>
                      <span className="text-xs font-bold text-white block">AI Evaluation</span>
                      <span className="text-[11px] text-slate-400">Technical depth & clarity assessment</span>
                    </div>
                  </div>

                  {currentIdx + 1 < session.totalQuestions ? (
                    <button
                      type="button"
                      onClick={handleNext}
                      className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs transition-all shadow-md"
                    >
                      Next Question &rarr;
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => onFinishSession(session)}
                      className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition-all shadow-md"
                    >
                      View Final Report &rarr;
                    </button>
                  )}
                </div>

                {currentQuestion.feedback && (
                  <div className="text-xs sm:text-sm text-slate-300 leading-relaxed bg-slate-950/60 p-3.5 rounded-xl border border-slate-800">
                    💡 <span className="font-semibold text-indigo-300">Feedback: </span>
                    {currentQuestion.feedback}
                  </div>
                )}

                {currentQuestion.modelAnswer && (
                  <div className="text-xs sm:text-sm text-slate-300 leading-relaxed bg-slate-950/60 p-3.5 rounded-xl border border-slate-800">
                    🌟 <span className="font-semibold text-emerald-400">Exemplary Model Answer: </span>
                    {currentQuestion.modelAnswer}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

