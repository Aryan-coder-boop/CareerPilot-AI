import React, { useState } from 'react';
import { resumeService } from '../../../services/resumeService';
import { Spinner } from '../../../components/ui/Spinner';

export const ResumeHistoryList = ({ resumes = [], onSelectResume, activeResumeId, onResumeDeleted }) => {
  const [deletingId, setDeletingId] = useState(null);

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this resume analysis?')) {
      return;
    }

    try {
      setDeletingId(id);
      await resumeService.deleteResume(id);
      onResumeDeleted(id);
    } catch (err) {
      alert('Failed to delete resume: ' + (err.response?.data?.message || err.message));
    } finally {
      setDeletingId(null);
    }
  };

  const getScoreBadge = (score) => {
    if (score >= 80) return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30';
    if (score >= 60) return 'text-indigo-400 bg-indigo-500/10 border-indigo-500/30';
    return 'text-amber-400 bg-amber-500/10 border-amber-500/30';
  };

  if (!resumes || resumes.length === 0) {
    return (
      <div className="p-8 text-center rounded-3xl bg-slate-900 border border-slate-800 text-slate-400">
        <p className="text-sm">No resumes analyzed yet. Upload your first PDF resume above!</p>
      </div>
    );
  }

  return (
    <div className="p-6 sm:p-8 rounded-3xl bg-slate-900 border border-slate-800 shadow-2xl space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-bold text-white">Your Analyzed Resumes</h3>
          <p className="text-xs text-slate-400">Past upload history and quality diagnostic scores</p>
        </div>
        <span className="px-2.5 py-1 rounded-full bg-slate-800 text-slate-300 text-xs font-semibold">
          {resumes.length} {resumes.length === 1 ? 'Resume' : 'Resumes'}
        </span>
      </div>

      <div className="divide-y divide-slate-800/80">
        {resumes.map((resume) => {
          const isActive = resume.id === activeResumeId;
          const formattedDate = new Date(resume.createdAt).toLocaleDateString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          });

          return (
            <div
              key={resume.id}
              onClick={() => onSelectResume(resume.id)}
              className={`py-3.5 px-3 rounded-2xl flex items-center justify-between gap-4 cursor-pointer transition-colors ${
                isActive ? 'bg-indigo-600/10 border border-indigo-500/30' : 'hover:bg-slate-800/50'
              }`}
            >
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-slate-300 shrink-0">
                  <svg className="w-5 h-5 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-white truncate">{resume.fileName}</p>
                  <p className="text-[11px] text-slate-400">{formattedDate}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <span className={`px-2.5 py-1 rounded-lg border text-xs font-bold ${getScoreBadge(resume.qualityScore)}`}>
                  {resume.qualityScore}/100
                </span>

                <button
                  onClick={(e) => handleDelete(e, resume.id)}
                  disabled={deletingId === resume.id}
                  className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
                  title="Delete Resume"
                >
                  {deletingId === resume.id ? (
                    <Spinner size="sm" />
                  ) : (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  )}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

