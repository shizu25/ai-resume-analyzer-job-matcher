import React from 'react';

export const Card = ({ children, className = '', title, subtitle, action }) => {
  return (
    <div className={`rounded-xl border border-slate-200/80 bg-white p-6 shadow-xs ${className}`}>
      {(title || subtitle || action) && (
        <div className="mb-4 flex items-center justify-between border-b border-slate-100 pb-3">
          <div>
            {title && <h3 className="text-base font-semibold text-slate-900">{title}</h3>}
            {subtitle && <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>}
          </div>
          {action && <div>{action}</div>}
        </div>
      )}
      {children}
    </div>
  );
};

export default Card;
