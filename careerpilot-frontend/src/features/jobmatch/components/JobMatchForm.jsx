import React, { useState } from 'react';
import { jobMatchService } from '../../../services/jobMatchService';
import { Spinner } from '../../../components/ui/Spinner';
import { Alert } from '../../../components/ui/Alert';

const SAMPLE_JDS = [
  {
    title: 'Senior Java Backend Engineer',
    company: 'Fintech Solutions Inc',
    text: `We are looking for a Senior Java Backend Engineer to design and scale high-throughput payment systems.
Requirements:
- 4+ years of professional development experience with Java (Java 17+) and Spring Boot.
- Deep expertise in relational databases, SQL optimization, and PostgreSQL.
- Hands-on experience with Docker, Kubernetes, and containerized cloud deployments.
- Solid understanding of REST APIs, Microservices architecture, and Spring Security.
- Familiarity with event-driven streaming with Apache Kafka or RabbitMQ.
- Experience with CI/CD automation pipelines and Git.`
  },
  {
    title: 'Full Stack React & Cloud Engineer',
    company: 'CloudWave SaaS',
    text: `Seeking a skilled Full Stack Engineer to lead new feature development for our enterprise SaaS product.
Key Responsibilities & Qualifications:
- Strong frontend mastery in React, TypeScript, Vite, and modern CSS/Tailwind CSS.
- Robust backend API development in Java/Spring Boot or Node.js.
- Strong knowledge of PostgreSQL database schema design and indexing.
- Experience building and deploying cloud infrastructure on AWS or GCP.
- Familiarity with Redis caching and Docker containerization.
- Passion for writing clean, testable code with JUnit and Jest.`
  }
];

export const JobMatchForm = ({ resumes, onMatchSuccess, onSwitchToResumeTab }) => {
  const [selectedResumeId, setSelectedResumeId] = useState(
    resumes.length > 0 ? resumes[0].id : ''
  );
  const [jobTitle, setJobTitle] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [jobDescriptionText, setJobDescriptionText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSelectSample = (sample) => {
    setJobTitle(sample.title);
    setCompanyName(sample.company);
    setJobDescriptionText(sample.text);
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedResumeId) {
      setError('Please select an analyzed resume to match against.');
      return;
    }
    if (!jobTitle.trim()) {
      setError('Job title is required.');
      return;
    }
    if (!jobDescriptionText.trim() || jobDescriptionText.trim().length < 20) {
      setError('Please enter a comprehensive job description (at least 20 characters).');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await jobMatchService.matchJob({
        resumeId: selectedResumeId,
        jobTitle: jobTitle.trim(),
        companyName: companyName.trim() || null,
        jobDescriptionText: jobDescriptionText.trim()
      });

      if (res.success && res.data) {
        onMatchSuccess(res.data);
      } else {
        setError(res.message || 'Failed to complete job match analysis.');
      }
    } catch (err) {
      console.error('Job match error', err);
      const msg = err.response?.data?.message || err.message || 'Error executing AI job matching.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (!resumes || resumes.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 text-center space-y-4">
        <div className="w-16 h-16 mx-auto rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-white">No Analyzed Resumes Available</h3>
        <p className="text-sm text-slate-400 max-w-md mx-auto">
          To perform job matching and skill gap diagnostics, you need to upload and analyze at least one resume first.
        </p>
        <button
          onClick={onSwitchToResumeTab}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm transition-all shadow-lg shadow-indigo-600/30"
        >
          Go to Resume Analyzer &rarr;
        </button>
      </div>
    );
  }

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div>
          <h2 className="text-xl font-bold text-white">Compare Resume with Target Role</h2>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Analyze skill alignment, calculate match percentage, and discover critical gaps with actionable bridging advice.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400">Quick Samples:</span>
          {SAMPLE_JDS.map((s, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => handleSelectSample(s)}
              className="text-xs px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-indigo-300 border border-slate-700 transition-colors"
            >
              {idx === 0 ? 'Fintech Java' : 'Full Stack'}
            </button>
          ))}
        </div>
      </div>

      {error && <Alert type="error" message={error} />}

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Resume Selection */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
            Select Resume Profile
          </label>
          <select
            value={selectedResumeId}
            onChange={(e) => setSelectedResumeId(e.target.value)}
            disabled={loading}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
          >
            {resumes.map((r) => (
              <option key={r.id} value={r.id}>
                {r.fileName} (Quality Score: {r.qualityScore || 0}/100) — Uploaded on {new Date(r.createdAt).toLocaleDateString()}
              </option>
            ))}
          </select>
        </div>

        {/* Job Title and Company Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Target Job Title *
            </label>
            <input
              type="text"
              placeholder="e.g. Senior Java Backend Engineer"
              value={jobTitle}
              onChange={(e) => setJobTitle(e.target.value)}
              disabled={loading}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Company Name (Optional)
            </label>
            <input
              type="text"
              placeholder="e.g. Google, Stripe, Acme Corp"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              disabled={loading}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
            />
          </div>
        </div>

        {/* Job Description Textarea */}
        <div>
          <div className="flex justify-between items-center mb-2">
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
              Paste Job Description (JD) *
            </label>
            <span className="text-xs text-slate-500">
              {jobDescriptionText.length} characters
            </span>
          </div>
          <textarea
            rows={7}
            placeholder="Paste the role description, responsibilities, technical requirements, and preferred qualifications here..."
            value={jobDescriptionText}
            onChange={(e) => setJobDescriptionText(e.target.value)}
            disabled={loading}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl p-4 text-sm text-white placeholder-slate-500 font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold text-sm tracking-wide shadow-lg shadow-indigo-600/30 transition-all disabled:opacity-50"
        >
          {loading ? (
            <>
              <Spinner size="sm" />
              <span>Matching Skills & Requirements...</span>
            </>
          ) : (
            <>
              <svg className="w-5 h-5 text-indigo-200" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
              </svg>
              <span>Analyze Match & Skill Gaps</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
};

