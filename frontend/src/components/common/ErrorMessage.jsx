import React from 'react';
import { AlertTriangle, X } from 'lucide-react';

export const ErrorMessage = ({ message, onDismiss, className = '' }) => {
  if (!message) return null;
  return (
    <div className={`flex items-start gap-3 rounded-lg border border-rose-200 bg-rose-50 p-4 text-rose-800 ${className}`}>
      <AlertTriangle className="h-5 w-5 shrink-0 text-rose-600 mt-0.5" />
      <div className="flex-1 text-sm">{message}</div>
      {onDismiss && (
        <button
          onClick={onDismiss}
          className="rounded p-1 text-rose-400 hover:bg-rose-100 hover:text-rose-600 cursor-pointer"
        >
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;
