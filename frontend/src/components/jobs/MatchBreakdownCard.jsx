import React from 'react';
import { Link } from 'react-router-dom';
import Card from '../common/Card';
import Badge from '../common/Badge';
import Button from '../common/Button';
import ProgressIndicator from '../common/ProgressIndicator';
import ErrorMessage from '../common/ErrorMessage';
import { Sparkles, CheckCircle2, AlertCircle, Lightbulb, FileText } from 'lucide-react';
import { getMatchBadgeVariant } from '../../utils/jobFormatters';

export default function MatchBreakdownCard({
  match,
  loading = false,
  calculating = false,
  resumeError = null,
  onRunMatch,
}) {
  return (
    <Card
      title="Resume Match Analysis"
      subtitle={
        match
          ? `Evaluated using active resume: ${match.resumeFileName}`
          : 'Compare your active analyzed resume against this role'
      }
      action={
        <Button
          variant="primary"
          size="sm"
          loading={calculating}
          onClick={onRunMatch}
          disabled={loading}
        >
          <Sparkles className="w-3.5 h-3.5 mr-1.5" />
          {match ? 'Refresh Match' : 'Calculate Match'}
        </Button>
      }
    >
      {resumeError && (
        <div className="space-y-3">
          <ErrorMessage message={resumeError} />
          <Link to="/resumes" className="inline-flex">
            <Button variant="outline" size="sm">
              <FileText className="w-3.5 h-3.5 mr-1.5" /> Manage & Analyze Resumes
            </Button>
          </Link>
        </div>
      )}

      {!resumeError && loading && (
        <div className="animate-pulse space-y-3 py-2">
          <div className="h-4 bg-slate-200 rounded w-1/3" />
          <div className="h-2.5 bg-slate-100 rounded w-full" />
          <div className="h-20 bg-slate-100 rounded w-full" />
        </div>
      )}

      {!resumeError && !loading && !match && (
        <div className="text-center py-6 space-y-3">
          <div className="w-11 h-11 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <p className="text-sm font-semibold text-slate-800">No match calculated yet</p>
            <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
              Run resume matching to view your compatibility percentage, skill gaps, and personalized recommendations.
            </p>
          </div>
        </div>
      )}

      {!resumeError && !loading && match && (
        <div className="space-y-5">
          {/* Score Overview */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center bg-slate-50/80 p-4 rounded-xl border border-slate-200/70">
            <div className="space-y-1">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                Overall Match Score
              </span>
              <div className="flex items-center gap-2.5">
                <span className="text-3xl font-extrabold text-slate-900">
                  {match.matchScore}%
                </span>
                <Badge variant={getMatchBadgeVariant(match.matchScore)} size="sm">
                  {match.matchScore >= 80
                    ? 'Strong Fit'
                    : match.matchScore >= 50
                    ? 'Moderate Fit'
                    : 'Skill Gap'}
                </Badge>
              </div>
            </div>

            <div className="md:col-span-2 space-y-3">
              <ProgressIndicator
                progress={match.requiredSkillMatchPct || 0}
                label="Required Skills Alignment (80% weight)"
              />
              <ProgressIndicator
                progress={match.preferredSkillMatchPct || 0}
                label="Preferred Skills Alignment (20% weight)"
              />
            </div>
          </div>

          {/* Skill Breakdown */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl border border-emerald-200/80 bg-emerald-50/30 space-y-2.5">
              <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-emerald-800">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Matched Required Skills ({match.matchedRequiredSkills?.length || 0})
              </div>
              {match.matchedRequiredSkills?.length > 0 ? (
                <div className="flex flex-wrap gap-1.5">
                  {match.matchedRequiredSkills.map((skill) => (
                    <Badge key={skill} variant="success" size="sm">
                      {skill}
                    </Badge>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-500">No required skills matched yet.</p>
              )}

              {match.matchedPreferredSkills?.length > 0 && (
                <div className="pt-2 border-t border-emerald-200/60 space-y-1.5">
                  <p className="text-[11px] font-semibold uppercase tracking-wider text-emerald-700">
                    Matched Preferred Skills ({match.matchedPreferredSkills.length})
                  </p>
                  <div className="flex flex-wrap gap-1.5">
                    {match.matchedPreferredSkills.map((skill) => (
                      <Badge key={skill} variant="primary" size="sm">
                        {skill}
                      </Badge>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="p-4 rounded-xl border border-rose-200/80 bg-rose-50/30 space-y-2.5">
              <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-rose-800">
                <AlertCircle className="w-4 h-4 text-rose-600" />
                Missing Required Skills ({match.missingRequiredSkills?.length || 0})
              </div>
              {match.missingRequiredSkills?.length > 0 ? (
                <div className="flex flex-wrap gap-1.5">
                  {match.missingRequiredSkills.map((skill) => (
                    <Badge key={skill} variant="danger" size="sm">
                      {skill}
                    </Badge>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-emerald-700 font-medium">
                  All required skills matched!
                </p>
              )}

              {match.missingPreferredSkills?.length > 0 && (
                <div className="pt-2 border-t border-rose-200/60 space-y-1.5">
                  <p className="text-[11px] font-semibold uppercase tracking-wider text-amber-800">
                    Missing Preferred Skills ({match.missingPreferredSkills.length})
                  </p>
                  <div className="flex flex-wrap gap-1.5">
                    {match.missingPreferredSkills.map((skill) => (
                      <Badge key={skill} variant="warning" size="sm">
                        {skill}
                      </Badge>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Recommendations */}
          {match.recommendations?.length > 0 && (
            <div className="p-4 rounded-xl border border-indigo-100 bg-indigo-50/40 space-y-2">
              <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-indigo-800">
                <Lightbulb className="w-4 h-4 text-indigo-600" />
                Recommendations
              </div>
              <ul className="space-y-1.5 text-sm text-slate-700">
                {match.recommendations.map((rec, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 mt-2 shrink-0" />
                    <span>{rec}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </Card>
  );
}
