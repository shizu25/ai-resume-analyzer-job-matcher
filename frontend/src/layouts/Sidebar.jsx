import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { FileText, User, Briefcase, Sparkles, Send, BarChart2 } from 'lucide-react';

export default function Sidebar() {
  const location = useLocation();
  const isMatchedView =
    location.pathname === '/jobs' && new URLSearchParams(location.search).get('matched') === 'true';

  const member1Links = [
    { name: 'My Resumes', to: '/resumes', icon: FileText },
    { name: 'User Profile', to: '/profile', icon: User },
  ];

  const member2Links = [
    {
      name: 'Job Board',
      to: '/jobs',
      icon: Briefcase,
      isActive: () =>
        (location.pathname === '/jobs' && !isMatchedView) ||
        location.pathname.startsWith('/jobs/'),
    },
    {
      name: 'Job Matcher',
      to: '/jobs?matched=true',
      icon: Sparkles,
      isActive: () => isMatchedView,
    },
    {
      name: 'Applications',
      to: '/applications',
      icon: Send,
      isActive: () => location.pathname === '/applications',
    },
    {
      name: 'Analytics',
      to: '/analytics',
      icon: BarChart2,
      isActive: () => location.pathname === '/analytics',
    },
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
            Job Matcher & Career
          </p>
          <nav className="space-y-1">
            {member2Links.map((item) => {
              const active = item.isActive();
              return (
                <NavLink
                  key={item.name}
                  to={item.to}
                  className={`flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    active
                      ? 'bg-indigo-50 text-indigo-700 font-semibold'
                      : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                  }`}
                >
                  <item.icon className="w-4 h-4 shrink-0" />
                  <span>{item.name}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>
      </div>

      <div className="p-4 border-t border-slate-100 text-xs text-slate-400">
        <p className="font-semibold text-slate-600">AI Resume Analyzer v1.0</p>
        <p className="text-[11px] text-slate-400 mt-0.5">Full-Stack Career Workspace</p>
      </div>
    </aside>
  );
}
