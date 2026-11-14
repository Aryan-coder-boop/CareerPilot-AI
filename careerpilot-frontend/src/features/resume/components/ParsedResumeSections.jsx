import React, { useState } from 'react';

export const ParsedResumeSections = ({ analysis }) => {
  if (!analysis) return null;

  const [activeTab, setActiveTab] = useState('skills');

  const skillsByCategory = (analysis.skills || []).reduce((acc, skill) => {
    const cat = skill.category || 'OTHER';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(skill.name);
    return acc;
  }, {});

  return (
    <div className="p-6 sm:p-8 rounded-3xl bg-slate-900 border border-slate-800 shadow-2xl space-y-6">
      {/* Tab Switcher */}
      <div className="flex flex-wrap items-center gap-2 border-b border-slate-800 pb-4">
        <button
          onClick={() => setActiveTab('skills')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'skills'
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          Skills ({analysis.skills?.length || 0})
        </button>

        <button
          onClick={() => setActiveTab('experience')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'experience'
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          Experience ({analysis.experiences?.length || 0})
        </button>

        <button
          onClick={() => setActiveTab('education')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'education'
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          Education ({analysis.education?.length || 0})
        </button>

        <button
          onClick={() => setActiveTab('projects')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'projects'
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          Projects ({analysis.projects?.length || 0})
        </button>

        <button
          onClick={() => setActiveTab('certifications')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'certifications'
              ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
              : 'text-slate-400 hover:text-white hover:bg-slate-800'
          }`}
        >
          Certifications ({analysis.certifications?.length || 0})
        </button>
      </div>

      {/* Tab 1: Skills */}
      {activeTab === 'skills' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.keys(skillsByCategory).length > 0 ? (
              Object.entries(skillsByCategory).map(([category, skillList]) => (
                <div key={category} className="p-4 rounded-2xl bg-slate-950/50 border border-slate-800 space-y-2">
                  <span className="text-[11px] font-bold text-indigo-400 uppercase tracking-wider">
                    {category.replace('_', ' ')}
                  </span>
                  <div className="flex flex-wrap gap-2 pt-1">
                    {skillList.map((skill, idx) => (
                      <span
                        key={idx}
                        className="px-2.5 py-1 rounded-lg bg-slate-800/90 text-slate-200 text-xs font-medium border border-slate-700/80"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>
              ))
            ) : (
              <p className="text-xs text-slate-400 italic">No structured skills detected.</p>
            )}
          </div>

          {analysis.softSkills && analysis.softSkills.length > 0 && (
            <div className="p-4 rounded-2xl bg-slate-950/40 border border-slate-800 space-y-2">
              <span className="text-[11px] font-bold text-cyan-400 uppercase tracking-wider">
                Soft Skills & Interpersonal Competencies
              </span>
              <div className="flex flex-wrap gap-2 pt-1">
                {analysis.softSkills.map((softSkill, idx) => (
                  <span
                    key={idx}
                    className="px-2.5 py-1 rounded-lg bg-cyan-950/40 text-cyan-300 text-xs font-medium border border-cyan-800/40"
                  >
                    {softSkill}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Experience */}
      {activeTab === 'experience' && (
        <div className="space-y-4">
          {analysis.experiences && analysis.experiences.length > 0 ? (
            analysis.experiences.map((exp, idx) => (
              <div key={idx} className="p-5 rounded-2xl bg-slate-950/50 border border-slate-800 space-y-3">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1">
                  <div>
                    <h4 className="text-base font-bold text-white">{exp.role || 'Role'}</h4>
                    <p className="text-xs font-semibold text-indigo-400">{exp.company || 'Company'}</p>
                  </div>
                  <div className="text-xs text-slate-400 sm:text-right">
                    <span>{exp.duration || 'Duration'}</span>
                    {exp.location && <span> &bull; {exp.location}</span>}
                  </div>
                </div>

                {exp.highlights && exp.highlights.length > 0 && (
                  <ul className="space-y-1.5 text-xs text-slate-300 list-disc list-inside">
                    {exp.highlights.map((point, pIdx) => (
                      <li key={pIdx} className="leading-relaxed">{point}</li>
                    ))}
                  </ul>
                )}
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-400 italic">No work experience entries extracted.</p>
          )}
        </div>
      )}

      {/* Tab 3: Education */}
      {activeTab === 'education' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {analysis.education && analysis.education.length > 0 ? (
            analysis.education.map((edu, idx) => (
              <div key={idx} className="p-5 rounded-2xl bg-slate-950/50 border border-slate-800 space-y-1.5">
                <h4 className="text-sm font-bold text-white">{edu.institution || 'University'}</h4>
                <p className="text-xs font-semibold text-indigo-400">
                  {edu.degree} {edu.fieldOfStudy ? `in ${edu.fieldOfStudy}` : ''}
                </p>
                <div className="flex items-center gap-3 text-xs text-slate-400 pt-1">
                  {edu.graduationYear && <span>Class of {edu.graduationYear}</span>}
                  {edu.gpaOrHonors && <span>&bull; {edu.gpaOrHonors}</span>}
                </div>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-400 italic">No education entries extracted.</p>
          )}
        </div>
      )}

      {/* Tab 4: Projects */}
      {activeTab === 'projects' && (
        <div className="space-y-4">
          {analysis.projects && analysis.projects.length > 0 ? (
            analysis.projects.map((proj, idx) => (
              <div key={idx} className="p-5 rounded-2xl bg-slate-950/50 border border-slate-800 space-y-2">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-bold text-white">{proj.title}</h4>
                  {proj.link && (
                    <a
                      href={proj.link}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs text-indigo-400 hover:text-indigo-300 underline"
                    >
                      Project Link &rarr;
                    </a>
                  )}
                </div>
                {proj.description && <p className="text-xs text-slate-300">{proj.description}</p>}
                {proj.technologies && proj.technologies.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {proj.technologies.map((t, tIdx) => (
                      <span
                        key={tIdx}
                        className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px] font-mono"
                      >
                        {t}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-400 italic">
              No projects section explicitly detected in the resume text.
            </p>
          )}
        </div>
      )}

      {/* Tab 5: Certifications */}
      {activeTab === 'certifications' && (
        <div>
          {analysis.certifications && analysis.certifications.length > 0 ? (
            <ul className="space-y-2">
              {analysis.certifications.map((cert, idx) => (
                <li
                  key={idx}
                  className="p-3.5 rounded-xl bg-slate-950/50 border border-slate-800 text-xs text-slate-200 flex items-center gap-3"
                >
                  <svg className="w-4 h-4 text-emerald-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                  <span>{cert}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-xs text-slate-400 italic">
              No certifications explicitly detected in the resume text.
            </p>
          )}
        </div>
      )}
    </div>
  );
};

