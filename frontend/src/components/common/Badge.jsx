import React from 'react';

const variants = {
  primary: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  secondary: 'bg-slate-100 text-slate-700 border-slate-200',
  success: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  warning: 'bg-amber-50 text-amber-700 border-amber-200',
  danger: 'bg-rose-50 text-rose-700 border-rose-200',
  high: 'bg-rose-100 text-rose-800 border-rose-200',
  medium: 'bg-amber-100 text-amber-800 border-amber-200',
  low: 'bg-blue-100 text-blue-800 border-blue-200',
  COMPLETED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  PROCESSING: 'bg-blue-50 text-blue-700 border-blue-200 animate-pulse',
  UPLOADED: 'bg-slate-100 text-slate-700 border-slate-200',
  FAILED: 'bg-rose-50 text-rose-700 border-rose-200',
};

const sizes = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-2.5 py-1 text-xs font-medium',
  lg: 'px-3 py-1.5 text-sm font-medium',
};

export const Badge = ({ children, variant = 'primary', size = 'md', className = '' }) => {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border font-medium ${
        variants[variant] || variants.primary
      } ${sizes[size] || sizes.md} ${className}`}
    >
      {children}
    </span>
  );
};

export default Badge;
