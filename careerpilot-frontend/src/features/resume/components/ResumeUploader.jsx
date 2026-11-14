import React, { useState, useRef } from 'react';
import { resumeService } from '../../../services/resumeService';
import { Alert } from '../../../components/ui/Alert';
import { Spinner } from '../../../components/ui/Spinner';

export const ResumeUploader = ({ onUploadSuccess }) => {
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [statusText, setStatusText] = useState('');
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const validateFile = (file) => {
    if (!file) return 'Please select a file.';
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      return 'Only PDF documents (.pdf) are supported.';
    }
    if (file.size > 5 * 1024 * 1024) {
      return 'File size exceeds maximum allowed limit of 5 MB.';
    }
    return null;
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      processFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      processFile(e.target.files[0]);
    }
  };

  const processFile = async (file) => {
    const validationError = validateFile(file);
    if (validationError) {
      setError({ message: validationError });
      return;
    }

    try {
      setError(null);
      setUploading(true);
      setProgress(10);
      setStatusText('Uploading PDF document...');

      const result = await resumeService.uploadResume(file, (percent) => {
        setProgress(Math.min(90, Math.max(10, percent)));
        if (percent >= 100) {
          setStatusText('Extracting text & running Gemini AI analysis...');
        }
      });

      setProgress(100);
      setStatusText('Analysis complete!');
      if (result.success && result.data) {
        onUploadSuccess(result.data);
      }
    } catch (err) {
      const responseData = err.response?.data;
      if (responseData) {
        setError({
          message: responseData.message || 'Upload failed',
          details: responseData.data?.details || [],
        });
      } else {
        setError({ message: err.message || 'Network error occurred during resume analysis.' });
      }
    } finally {
      setUploading(false);
      setProgress(0);
      setStatusText('');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  return (
    <div className="w-full space-y-4">
      {error && <Alert type="error" message={error.message} details={error.details} />}

      <div
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        className={`relative border-2 border-dashed rounded-3xl p-8 sm:p-12 text-center transition-all ${
          dragActive
            ? 'border-indigo-400 bg-indigo-500/10 scale-[1.01]'
            : 'border-slate-700 hover:border-slate-600 bg-slate-900/60'
        } ${uploading ? 'pointer-events-none opacity-80' : ''}`}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,application/pdf"
          onChange={handleChange}
          className="hidden"
          id="resume-file-input"
          disabled={uploading}
        />

        {uploading ? (
          <div className="py-6 flex flex-col items-center justify-center space-y-4">
            <Spinner size="lg" className="text-indigo-400" />
            <div className="space-y-1">
              <p className="text-base font-semibold text-white">{statusText}</p>
              <p className="text-xs text-slate-400">
                Evaluating skills, quantifying impact, and generating recommendations...
              </p>
            </div>
            <div className="w-64 max-w-full bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-indigo-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              ></div>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-indigo-600/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 shadow-inner">
              <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="1.5"
                  d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>

            <div>
              <p className="text-lg font-bold text-white tracking-tight">
                Drop your PDF resume here, or{' '}
                <label
                  htmlFor="resume-file-input"
                  className="text-indigo-400 hover:text-indigo-300 underline cursor-pointer"
                >
                  browse files
                </label>
              </p>
              <p className="mt-1 text-xs text-slate-400">
                Supports selectable PDF documents up to 5 MB.
              </p>
            </div>

            <div className="flex items-center gap-4 text-[11px] text-slate-400 pt-2">
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
                PDFBox Extraction
              </span>
              <span>&bull;</span>
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                Gemini AI Diagnostic
              </span>
              <span>&bull;</span>
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4 text-cyan-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                Private & Secure
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

