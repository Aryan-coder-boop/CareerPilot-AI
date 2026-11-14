import React, { useState, useEffect } from 'react';
import { useAuth } from '../../../context/AuthContext';
import { resumeService } from '../../../services/resumeService';
import { jobMatchService } from '../../../services/jobMatchService';
import { interviewService } from '../../../services/interviewService';
import { roadmapService } from '../../../services/roadmapService';

import { ResumeUploader } from '../../resume/components/ResumeUploader';
import { ScoreBreakdownCard } from '../../resume/components/ScoreBreakdownCard';
import { SuggestionsList } from '../../resume/components/SuggestionsList';
import { ParsedResumeSections } from '../../resume/components/ParsedResumeSections';
import { ResumeHistoryList } from '../../resume/components/ResumeHistoryList';

import { JobMatchForm } from '../../jobmatch/components/JobMatchForm';
import { JobMatchReportCard } from '../../jobmatch/components/JobMatchReportCard';
import { JobMatchHistoryList } from '../../jobmatch/components/JobMatchHistoryList';

import { InterviewSetup } from '../../interview/components/InterviewSetup';
import { InterviewActiveSession } from '../../interview/components/InterviewActiveSession';
import { InterviewSessionReport } from '../../interview/components/InterviewSessionReport';
import { InterviewHistoryList } from '../../interview/components/InterviewHistoryList';

import { RoadmapGeneratorForm } from '../../roadmap/components/RoadmapGeneratorForm';
import { RoadmapViewer } from '../../roadmap/components/RoadmapViewer';
import { RoadmapHistoryList } from '../../roadmap/components/RoadmapHistoryList';

import { Spinner } from '../../../components/ui/Spinner';
import { Alert } from '../../../components/ui/Alert';

export const DashboardPage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('resume'); // 'resume' | 'jobmatch' | 'interview' | 'roadmap'

  // Pre-filled state from cross-feature shortcuts
  const [prefilledInterviewRole, setPrefilledInterviewRole] = useState('');
  const [prefilledRoadmapRole, setPrefilledRoadmapRole] = useState('');

  // 1. Resume states
  const [resumes, setResumes] = useState([]);
  const [activeResume, setActiveResume] = useState(null);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [error, setError] = useState(null);

  // 2. Job Match states
  const [matches, setMatches] = useState([]);
  const [activeMatch, setActiveMatch] = useState(null);
  const [loadingMatchDetail, setLoadingMatchDetail] = useState(false);

  // 3. Interview states
  const [interviewSessions, setInterviewSessions] = useState([]);
  const [activeInterviewSession, setActiveInterviewSession] = useState(null);
  const [interviewViewMode, setInterviewViewMode] = useState('SETUP'); // 'SETUP' | 'ACTIVE' | 'REPORT'
  const [loadingInterviewDetail, setLoadingInterviewDetail] = useState(false);

  // 4. Roadmap states
  const [roadmaps, setRoadmaps] = useState([]);
  const [activeRoadmap, setActiveRoadmap] = useState(null);
  const [loadingRoadmapDetail, setLoadingRoadmapDetail] = useState(false);

  const fetchResumeHistory = async () => {
    try {
      setLoadingHistory(true);
      setError(null);
      const res = await resumeService.getResumes();
      if (res.success && res.data) {
        setResumes(res.data);
        if (res.data.length > 0 && !activeResume) {
          loadResumeDetail(res.data[0].id);
        }
      }
    } catch (err) {
      console.error('Failed to load resume history', err);
      setError('Could not fetch resume history.');
    } finally {
      setLoadingHistory(false);
    }
  };

  const fetchMatchHistory = async () => {
    try {
      const res = await jobMatchService.getMatches();
      if (res.success && res.data) {
        setMatches(res.data);
        if (res.data.length > 0 && !activeMatch) {
          loadMatchDetail(res.data[0].id);
        }
      }
    } catch (err) {
      console.error('Failed to load match history', err);
    }
  };

  const fetchInterviewHistory = async () => {
    try {
      const res = await interviewService.getSessions();
      if (res.success && res.data) {
        setInterviewSessions(res.data);
      }
    } catch (err) {
      console.error('Failed to load interview history', err);
    }
  };

  const fetchRoadmapHistory = async () => {
    try {
      const res = await roadmapService.getRoadmaps();
      if (res.success && res.data) {
        setRoadmaps(res.data);
        if (res.data.length > 0 && !activeRoadmap) {
          loadRoadmapDetail(res.data[0].id);
        }
      }
    } catch (err) {
      console.error('Failed to load roadmap history', err);
    }
  };

  useEffect(() => {
    fetchResumeHistory();
    fetchMatchHistory();
    fetchInterviewHistory();
    fetchRoadmapHistory();
  }, []);

  const loadResumeDetail = async (resumeId) => {
    try {
      setLoadingDetail(true);
      const res = await resumeService.getResumeDetail(resumeId);
      if (res.success && res.data) {
        setActiveResume(res.data);
      }
    } catch (err) {
      console.error('Failed to load resume detail', err);
    } finally {
      setLoadingDetail(false);
    }
  };

  const loadMatchDetail = async (matchId) => {
    try {
      setLoadingMatchDetail(true);
      const res = await jobMatchService.getMatchDetail(matchId);
      if (res.success && res.data) {
        setActiveMatch(res.data);
      }
    } catch (err) {
      console.error('Failed to load match detail', err);
    } finally {
      setLoadingMatchDetail(false);
    }
  };

  const loadInterviewDetail = async (sessionId) => {
    try {
      setLoadingInterviewDetail(true);
      const res = await interviewService.getSessionDetail(sessionId);
      if (res.success && res.data) {
        setActiveInterviewSession(res.data);
        setInterviewViewMode(res.data.status === 'COMPLETED' ? 'REPORT' : 'ACTIVE');
      }
    } catch (err) {
      console.error('Failed to load interview detail', err);
    } finally {
      setLoadingInterviewDetail(false);
    }
  };

  const loadRoadmapDetail = async (roadmapId) => {
    try {
      setLoadingRoadmapDetail(true);
      const res = await roadmapService.getRoadmapDetail(roadmapId);
      if (res.success && res.data) {
        setActiveRoadmap(res.data);
      }
    } catch (err) {
      console.error('Failed to load roadmap detail', err);
    } finally {
      setLoadingRoadmapDetail(false);
    }
  };

  // Cross-feature shortcut triggers
  const handleStartInterviewForRole = (role) => {
    setPrefilledInterviewRole(role);
    setInterviewViewMode('SETUP');
    setActiveTab('interview');
  };

  const handleGenerateRoadmapForRole = (role) => {
    setPrefilledRoadmapRole(role);
    setActiveTab('roadmap');
  };

  // Interview state transitions
  const handleInterviewStarted = (sessionDetail) => {
    setActiveInterviewSession(sessionDetail);
    setInterviewViewMode('ACTIVE');
    fetchInterviewHistory();
  };

  const handleInterviewFinished = (sessionDetail) => {
    setActiveInterviewSession(sessionDetail);
    setInterviewViewMode('REPORT');
    fetchInterviewHistory();
  };

  // Roadmap milestone updates
  const handleMilestoneUpdated = (milestoneId, updatedMilestone) => {
    if (activeRoadmap && activeRoadmap.milestones) {
      const updatedMilestones = activeRoadmap.milestones.map((m) =>
        m.id === milestoneId ? { ...m, ...updatedMilestone } : m
      );
      const completed = updatedMilestones.filter((m) => m.status === 'COMPLETED').length;
      const progress = Math.round((completed / updatedMilestones.length) * 100);

      setActiveRoadmap({
        ...activeRoadmap,
        milestones: updatedMilestones,
        progressPercentage: progress
      });
      fetchRoadmapHistory();
    }
  };

  const latestScore = resumes.length > 0 ? resumes[0].qualityScore : null;
  const latestMatchScore = matches.length > 0 ? matches[0].matchScore : null;
  const completedInterviews = interviewSessions.filter((s) => s.status === 'COMPLETED').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-indigo-950 via-slate-900 to-slate-900 border border-indigo-500/20 p-8 shadow-2xl">
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold mb-3">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
              Full Career Readiness Suite Active
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              Welcome back, <span className="text-indigo-400">{user?.fullName || 'Engineer'}</span>
            </h1>
            <p className="mt-2 text-slate-400 max-w-2xl text-sm sm:text-base">
              Analyze your resume, benchmark against real job postings, master technical mock interviews, and follow customized week-by-week learning roadmaps.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-4">
            <div className="text-right">
              <div className="text-xs text-slate-400">Resumes</div>
              <div className="text-2xl font-black text-white">{resumes.length}</div>
            </div>
            {latestScore !== null && (
              <div className="text-right border-l border-slate-800 pl-4">
                <div className="text-xs text-slate-400">Resume Quality</div>
                <div className="text-2xl font-black text-indigo-400">{latestScore}/100</div>
              </div>
            )}
            {latestMatchScore !== null && (
              <div className="text-right border-l border-slate-800 pl-4">
                <div className="text-xs text-slate-400">Latest Match</div>
                <div className="text-2xl font-black text-emerald-400">{latestMatchScore}%</div>
              </div>
            )}
            <div className="text-right border-l border-slate-800 pl-4">
              <div className="text-xs text-slate-400">Mock Sessions</div>
              <div className="text-2xl font-black text-white">{completedInterviews}</div>
            </div>
          </div>
        </div>
      </div>

      {error && <Alert type="error" message={error} />}

      {/* Primary Navigation Tabs */}
      <div className="flex items-center border-b border-slate-800 space-x-2 overflow-x-auto pb-1 scrollbar-none">
        {/* Tab 1: Resume */}
        <button
          onClick={() => setActiveTab('resume')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-bold border-b-2 transition-all whitespace-nowrap ${
            activeTab === 'resume'
              ? 'border-indigo-500 text-indigo-400 bg-slate-900/40 rounded-t-xl'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
          }`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          Resume Diagnostics
          {resumes.length > 0 && (
            <span className="px-2 py-0.5 text-xs rounded-full bg-slate-800 text-slate-300">
              {resumes.length}
            </span>
          )}
        </button>

        {/* Tab 2: Job Match */}
        <button
          onClick={() => setActiveTab('jobmatch')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-bold border-b-2 transition-all whitespace-nowrap ${
            activeTab === 'jobmatch'
              ? 'border-indigo-500 text-indigo-400 bg-slate-900/40 rounded-t-xl'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
          }`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
          </svg>
          Job Matching & Skill Gaps
          {matches.length > 0 && (
            <span className="px-2 py-0.5 text-xs rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              {matches.length}
            </span>
          )}
        </button>

        {/* Tab 3: Mock Interview */}
        <button
          onClick={() => setActiveTab('interview')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-bold border-b-2 transition-all whitespace-nowrap ${
            activeTab === 'interview'
              ? 'border-indigo-500 text-indigo-400 bg-slate-900/40 rounded-t-xl'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
          }`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 100-6 3 3 0 000 6z" />
          </svg>
          AI Mock Interview
          {interviewSessions.length > 0 && (
            <span className="px-2 py-0.5 text-xs rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              {interviewSessions.length}
            </span>
          )}
        </button>

        {/* Tab 4: Roadmap */}
        <button
          onClick={() => setActiveTab('roadmap')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-bold border-b-2 transition-all whitespace-nowrap ${
            activeTab === 'roadmap'
              ? 'border-indigo-500 text-indigo-400 bg-slate-900/40 rounded-t-xl'
              : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
          }`}
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
          </svg>
          Learning Roadmap
          {roadmaps.length > 0 && (
            <span className="px-2 py-0.5 text-xs rounded-full bg-slate-800 text-slate-300">
              {roadmaps.length}
            </span>
          )}
        </button>
      </div>

      {/* Tab 1: Resume Diagnostics */}
      {activeTab === 'resume' && (
        <div className="space-y-8 animate-fadeIn">
          <div>
            <h2 className="text-lg font-bold text-white mb-3">Upload & Analyze Resume</h2>
            <ResumeUploader onUploadSuccess={(data) => { setActiveResume(data); fetchResumeHistory(); }} />
          </div>

          {loadingDetail && (
            <div className="p-8 text-center bg-slate-900/50 rounded-3xl border border-slate-800 flex items-center justify-center gap-3 text-sm text-indigo-400">
              <Spinner size="md" />
              <span>Loading resume diagnostic report...</span>
            </div>
          )}

          {activeResume && activeResume.analysis && !loadingDetail && (
            <div className="space-y-8 animate-fadeIn">
              <ScoreBreakdownCard analysis={activeResume.analysis} fileName={activeResume.fileName} />
              <SuggestionsList strengths={activeResume.analysis.strengths} suggestions={activeResume.analysis.suggestions} />
              <ParsedResumeSections analysis={activeResume.analysis} />
            </div>
          )}

          <div>
            <ResumeHistoryList
              resumes={resumes}
              onSelectResume={loadResumeDetail}
              activeResumeId={activeResume?.id}
              onResumeDeleted={(deletedId) => {
                setResumes((prev) => prev.filter((r) => r.id !== deletedId));
                if (activeResume?.id === deletedId) setActiveResume(null);
              }}
            />
          </div>
        </div>
      )}

      {/* Tab 2: Job Description Matching & Missing Skills */}
      {activeTab === 'jobmatch' && (
        <div className="space-y-8 animate-fadeIn">
          <JobMatchForm
            resumes={resumes}
            onMatchSuccess={(data) => { setActiveMatch(data); fetchMatchHistory(); }}
            onSwitchToResumeTab={() => setActiveTab('resume')}
          />

          {loadingMatchDetail && (
            <div className="p-8 text-center bg-slate-900/50 rounded-3xl border border-slate-800 flex items-center justify-center gap-3 text-sm text-indigo-400">
              <Spinner size="md" />
              <span>Loading job match diagnostic report...</span>
            </div>
          )}

          {activeMatch && !loadingMatchDetail && (
            <div className="animate-fadeIn">
              <JobMatchReportCard
                report={activeMatch}
                onStartInterview={handleStartInterviewForRole}
                onGenerateRoadmap={handleGenerateRoadmapForRole}
              />
            </div>
          )}

          <JobMatchHistoryList
            matches={matches}
            onSelectMatch={loadMatchDetail}
            activeMatchId={activeMatch?.id}
          />
        </div>
      )}

      {/* Tab 3: AI Mock Interview */}
      {activeTab === 'interview' && (
        <div className="space-y-8 animate-fadeIn">
          {interviewViewMode === 'SETUP' && (
            <InterviewSetup
              resumes={resumes}
              initialRole={prefilledInterviewRole}
              onInterviewStarted={handleInterviewStarted}
            />
          )}

          {interviewViewMode === 'ACTIVE' && activeInterviewSession && (
            <InterviewActiveSession
              session={activeInterviewSession}
              onSessionUpdated={setActiveInterviewSession}
              onFinishSession={handleInterviewFinished}
            />
          )}

          {interviewViewMode === 'REPORT' && activeInterviewSession && (
            <InterviewSessionReport
              session={activeInterviewSession}
              onStartNewSession={() => setInterviewViewMode('SETUP')}
            />
          )}

          {loadingInterviewDetail && (
            <div className="p-8 text-center bg-slate-900/50 rounded-3xl border border-slate-800 flex items-center justify-center gap-3 text-sm text-indigo-400">
              <Spinner size="md" />
              <span>Loading mock interview transcript...</span>
            </div>
          )}

          <InterviewHistoryList
            sessions={interviewSessions}
            onSelectSession={loadInterviewDetail}
            activeSessionId={activeInterviewSession?.id}
          />
        </div>
      )}

      {/* Tab 4: Learning Roadmap */}
      {activeTab === 'roadmap' && (
        <div className="space-y-8 animate-fadeIn">
          <RoadmapGeneratorForm
            initialRole={prefilledRoadmapRole}
            matchReports={matches}
            onRoadmapGenerated={(data) => {
              setActiveRoadmap(data);
              fetchRoadmapHistory();
            }}
          />

          {loadingRoadmapDetail && (
            <div className="p-8 text-center bg-slate-900/50 rounded-3xl border border-slate-800 flex items-center justify-center gap-3 text-sm text-indigo-400">
              <Spinner size="md" />
              <span>Loading personalized learning curriculum...</span>
            </div>
          )}

          {activeRoadmap && !loadingRoadmapDetail && (
            <RoadmapViewer
              roadmap={activeRoadmap}
              onMilestoneUpdated={handleMilestoneUpdated}
            />
          )}

          <RoadmapHistoryList
            roadmaps={roadmaps}
            onSelectRoadmap={loadRoadmapDetail}
            activeRoadmapId={activeRoadmap?.id}
          />
        </div>
      )}
    </div>
  );
};
