import React from 'react';

export const LoadingSkeleton = ({ variant = 'card', count = 1, className = '' }) => {
  const items = Array.from({ length: count });

  if (variant === 'text') {
    return (
      <div className={`space-y-2.5 animate-pulse ${className}`}>
        {items.map((_, i) => (
          <div key={i} className="h-4 bg-slate-200 rounded w-full" style={{ width: `${85 - (i % 3) * 15}%` }} />
        ))}
      </div>
    );
  }

  return (
    <div className={`space-y-4 ${className}`}>
      {items.map((_, i) => (
        <div key={i} className="rounded-xl border border-slate-200 bg-white p-6 shadow-xs animate-pulse space-y-4">
          <div className="h-5 bg-slate-200 rounded w-1/3" />
          <div className="space-y-2">
            <div className="h-3.5 bg-slate-100 rounded w-full" />
            <div className="h-3.5 bg-slate-100 rounded w-5/6" />
          </div>
        </div>
      ))}
    </div>
  );
};

export default LoadingSkeleton;
