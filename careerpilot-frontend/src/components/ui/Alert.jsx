import React from 'react';

export const Alert = ({ type = 'error', message, details = [] }) => {
  if (!message && (!details || details.length === 0)) return null;

  const isError = type === 'error';
  const isSuccess = type === 'success';

  return (
    <div
      className={`p-4 rounded-xl text-sm border transition-all ${
        isError
          ? 'bg-rose-500/10 border-rose-500/30 text-rose-300'
          : isSuccess
          ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
          : 'bg-indigo-500/10 border-indigo-500/30 text-indigo-300'
      }`}
    >
      <div className="flex items-start gap-3">
        <div className="mt-0.5 shrink-0">
          {isError ? (
            <svg className="w-5 h-5 text-rose-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          ) : (
            <svg className="w-5 h-5 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          )}
        </div>
        <div className="flex-1">
          {message && <p className="font-semibold">{message}</p>}
          {details && details.length > 0 && (
            <ul className="mt-1 list-disc list-inside space-y-0.5 text-xs opacity-90">
              {details.map((detail, idx) => (
                <li key={idx}>{detail}</li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

