import React from 'react';
import { Link } from 'react-router-dom';
import Card from '../common/Card';
import Badge from '../common/Badge';
import Button from '../common/Button';
import {
  Briefcase,
  MapPin,
  IndianRupee,
  Sparkles,
  CheckCircle2,
  ArrowRight,
  Building2,
} from 'lucide-react';
import {
  WORK_MODE_LABELS,
  JOB_TYPE_LABELS,
  EXPERIENCE_LEVEL_LABELS,
  formatSalary,
  getMatchBadgeVariant,
} from '../../utils/jobFormatters';

export default function JobCard({ job, match, application }) {
  const requiredSkills = job.requiredSkills || [];
  const visibleSkills = requiredSkills.slice(0, 5);
  const extraSkillCount = Math.max(0, requiredSkills.length - visibleSkills.length);

  return (
    <Card className="hover:border-slate-300 transition-all hover:shadow-md flex flex-col justify-between h-full">
      <div className="space-y-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3 min-w-0">
            <div className="w-11 h-11 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100 flex items-center justify-center shrink-0">
              <Building2 className="w-5 h-5" />
            </div>
            <div className="min-w-0">
              <Link
                to={`/jobs/${job.id}`}
                className="text-base font-semibold text-slate-900 hover:text-indigo-600 transition-colors line-clamp-1"
              >
                {job.title}
              </Link>
              <p className="text-sm font-medium text-slate-600 truncate">{job.company}</p>
            </div>
          </div>

          <div className="flex flex-col items-end gap-1.5 shrink-0">
            {match && (
              <Badge variant={getMatchBadgeVariant(match.matchScore)} size="sm">
                <Sparkles className="w-3 h-3" /> {match.matchScore}% Match
              </Badge>
            )}
            {application && (
              <Badge variant="success" size="sm">
                <CheckCircle2 className="w-3 h-3" /> Applied
              </Badge>
            )}
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-slate-500">
          <span className="inline-flex items-center gap-1">
            <MapPin className="w-3.5 h-3.5 text-slate-400" /> {job.location}
          </span>
          <span className="inline-flex items-center gap-1">
            <IndianRupee className="w-3.5 h-3.5 text-slate-400" />
            {formatSalary(job.salaryMin, job.salaryMax, job.salaryCurrency, job.salaryPeriod)}
          </span>
        </div>

        <div className="flex flex-wrap gap-1.5">
          <Badge variant="secondary" size="sm">
            {WORK_MODE_LABELS[job.workMode] || job.workMode}
          </Badge>
          <Badge variant="secondary" size="sm">
            <Briefcase className="w-3 h-3" />
            {JOB_TYPE_LABELS[job.jobType] || job.jobType}
          </Badge>
          <Badge variant="primary" size="sm">
            {EXPERIENCE_LEVEL_LABELS[job.experienceLevel] || job.experienceLevel}
          </Badge>
        </div>

        <p className="text-sm text-slate-600 line-clamp-2 leading-relaxed">{job.description}</p>

        {visibleSkills.length > 0 && (
          <div className="flex flex-wrap gap-1.5 pt-1">
            {visibleSkills.map((skill) => (
              <span
                key={skill}
                className="px-2 py-0.5 text-xs font-medium rounded-md bg-slate-100 text-slate-700 border border-slate-200/80"
              >
                {skill}
              </span>
            ))}
            {extraSkillCount > 0 && (
              <span className="px-2 py-0.5 text-xs font-medium rounded-md bg-slate-50 text-slate-500 border border-slate-200/60">
                +{extraSkillCount} more
              </span>
            )}
          </div>
        )}
      </div>

      <div className="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between gap-2">
        <span className="text-[11px] text-slate-400">
          {job.canEdit ? 'Posted by you' : 'Shared board'}
        </span>
        <Link to={`/jobs/${job.id}`}>
          <Button variant="outline" size="sm">
            View & Match <ArrowRight className="w-3.5 h-3.5 ml-1" />
          </Button>
        </Link>
      </div>
    </Card>
  );
}
