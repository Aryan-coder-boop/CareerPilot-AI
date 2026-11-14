import React, { useState } from 'react';
import { interviewService } from '../../../services/interviewService';
import { Spinner } from '../../../components/ui/Spinner';
import { Alert } from '../../../components/ui/Alert';

const ROLE_PRESETS = [
  'Senior Java Backend Engineer',
  'Full Stack React & Node Engineer',
  'Cloud DevOps & Kubernetes Architect',
  'Distributed Systems Software Engineer'
];

export const InterviewSetup = ({ resumes, onInterviewStarted, initialRole = '' }) => {
  const [targetRole, setTargetRole] = useState(initialRole || 'Senior Java Backend Engineer');
  const [difficulty, setDifficulty] = useState('MID');
  const [totalQuestions, setTotalQuestions] = useState(3);
  const [selectedResumeId, setSelectedResumeId] = useState(
    resumes && resumes.length > 0 ? resumes[0].id : ''
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleStart = async (e) => {
    e.preventDefault();
    if (!targetRole.trim()) {
      setError('Please provide a target role for your mock interview.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await interviewService.startInterview({
        targetRole: targetRole.trim(),
        difficulty,
        totalQuestions,
        resumeId: selectedResumeId || null
      });

      if (res.success && res.data) {
        onInterviewStarted(res.data);
      } else {
        setError(res.message || 'Could not start interview session.');
      }
    } catch (err) {
      console.error('Failed to start interview', err);
      const msg = err.response?.data?.message || err.message || 'Failed to start interview.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl">
      <div className="border-b border-slate-800 pb-5">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold mb-2 bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
          🎙️ AI Mock Interview Setup
        </div>
        <h2 className="text-xl sm:text-2xl font-bold text-white">Start Technical Mock Interview</h2>
        <p className="text-xs sm:text-sm text-slate-400 mt-1">
          Practice scenario-based, role-grounded technical questions with instant grading, clarity scoring, and exemplary model answers.
        </p>
      </div>

      {error && <Alert type="error" message={error} />}

      <form onSubmit={handleStart} className="space-y-6">
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
            <span className="text-xs text-slate-500">Popular:</span>
            {ROLE_PRESETS.map((role) => (
              <button
                key={role}
                type="button"
                onClick={() => setTargetRole(role)}
                className="text-xs px-2.5 py-0.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-indigo-300 border border-slate-700 transition-colors"
              >
                {role}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Difficulty Level
            </label>
            <div className="grid grid-cols-4 gap-2">
              {['JUNIOR', 'MID', 'SENIOR', 'LEAD'].map((level) => (
                <button
                  key={level}
                  type="button"
                  onClick={() => setDifficulty(level)}
                  className={`py-2 text-xs font-bold rounded-xl border transition-all ${
                    difficulty === level
                      ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/30'
                      : 'bg-slate-950 text-slate-400 border-slate-800 hover:border-slate-700 hover:text-white'
                  }`}
                >
                  {level}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Number of Questions
            </label>
            <select
              value={totalQuestions}
              onChange={(e) => setTotalQuestions(Number(e.target.value))}
              disabled={loading}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            >
              <option value={3}>3 Questions (Express Practice)</option>
              <option value={5}>5 Questions (Standard Technical Round)</option>
              <option value={8}>8 Questions (In-Depth Assessment)</option>
            </select>
          </div>
        </div>

        {resumes && resumes.length > 0 && (
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Ground in Analyzed Resume (Optional)
            </label>
            <select
              value={selectedResumeId}
              onChange={(e) => setSelectedResumeId(e.target.value)}
              disabled={loading}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            >
              <option value="">None (Generic technical questions for role)</option>
              {resumes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.fileName} (Tailor questions to your listed skills & experience)
                </option>
              ))}
            </select>
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold text-sm tracking-wide shadow-lg shadow-indigo-600/30 transition-all disabled:opacity-50"
        >
          {loading ? (
            <>
              <Spinner size="sm" />
              <span>Generating Tailored Interview Questions...</span>
            </>
          ) : (
            <>
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Begin Mock Interview</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
};

