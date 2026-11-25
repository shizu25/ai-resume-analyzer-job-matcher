import React from 'react';
import { NavLink } from 'react-router-dom';
import { FileText, User, Briefcase, Sparkles, Send, BarChart2 } from 'lucide-react';

export default function Sidebar() {
  const member1Links = [
    { name: 'My Resumes', to: '/resumes', icon: FileText },
    { name: 'User Profile', to: '/profile', icon: User },
  ];

  const member2Links = [
    { name: 'Job Board', icon: Briefcase, disabled: true, tag: 'Member 2' },
    { name: 'Job Matcher', icon: Sparkles, disabled: true, tag: 'Member 2' },
    { name: 'Applications', icon: Send, disabled: true, tag: 'Member 2' },
    { name: 'Analytics', icon: BarChart2, disabled: true, tag: 'Member 2' },
  ];

  return (
    <aside className="w-64 bg-white border-r border-slate-200/80 min-h-[calc(100vh-4rem)] flex flex-col shrink-0">
      <div className="p-4 flex-1 space-y-6">
        <div>
          <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Resume Management
          </p>
          <nav className="space-y-1">
            {member1Links.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-indigo-50 text-indigo-700 font-semibold'
                      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                  }`
                }
              >
                <item.icon className="w-4 h-4 shrink-0" />
                <span>{item.name}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        <div>
          <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-2">
            Job Matcher (Integration)
          </p>
          <div className="space-y-1">
            {member2Links.map((item) => (
              <div
                key={item.name}
                className="flex items-center justify-between px-3 py-2 rounded-lg text-sm font-medium text-slate-400 opacity-60 cursor-not-allowed"
                title="Implemented independently by Member 2"
              >
                <div className="flex items-center gap-3">
                  <item.icon className="w-4 h-4 shrink-0" />
                  <span>{item.name}</span>
                </div>
                <span className="text-[10px] bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded font-mono">
                  {item.tag}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="p-4 border-t border-slate-100 text-xs text-slate-400">
        <p className="font-semibold text-slate-600">AI Resume Analyzer v1.0</p>
        <p className="text-[11px] text-slate-400 mt-0.5">Member 1 Active Workspace</p>
      </div>
    </aside>
  );
}
