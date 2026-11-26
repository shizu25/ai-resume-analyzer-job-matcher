import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import analyticsService from '../../services/analyticsService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import ProgressIndicator from '../../components/common/ProgressIndicator';
import EmptyState from '../../components/common/EmptyState';
import ErrorMessage from '../../components/common/ErrorMessage';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import {
  Send,
  Clock,
  TrendingUp,
  Award,
  Sparkles,
  MapPin,
  ArrowRight,
  RefreshCw,
} from 'lucide-react';
import { getMatchBadgeVariant, getApiErrorMessage } from '../../utils/jobFormatters';

export default function AnalyticsPage() {
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refreshDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await analyticsService.getDashboard();
      setDashboard(data);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load analytics dashboard.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;
    analyticsService
      .getDashboard()
      .then((data) => {
        if (!active) return;
        setDashboard(data);
        setError(null);
      })
      .catch((err) => {
        if (!active) return;
        setError(getApiErrorMessage(err, 'Failed to load analytics dashboard.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Career Analytics</h1>
          <p className="text-sm text-slate-500 mt-1">Loading application and match insights...</p>
        </div>
        <LoadingSkeleton count={3} variant="card" />
      </div>
    );
  }

  const total = dashboard?.totalApplications ?? 0;
  const awaitingResponse = dashboard?.awaitingResponse ?? 0;
  const interviewingCount = dashboard?.interviewingCount ?? 0;
  const offerCount = dashboard?.offerCount ?? 0;
  const rejectedCount = dashboard?.rejectedCount ?? 0;
  const interviewRate = dashboard?.interviewRate ?? 0;
  const offerRate = dashboard?.offerRate ?? 0;
  const topMatchedRoles = dashboard?.topMatchedRoles ?? [];

  const kpiCards = [
    {
      label: 'Total Applications',
      value: total,
      subtext: total === 1 ? '1 role tracked' : `${total} roles tracked`,
      icon: Send,
      iconBg: 'bg-indigo-50 text-indigo-600 border-indigo-100',
    },
    {
      label: 'Awaiting Response',
      value: awaitingResponse,
      subtext: 'Submitted (APPLIED)',
      icon: Clock,
      iconBg: 'bg-blue-50 text-blue-600 border-blue-100',
    },
    {
      label: 'Interview Rate',
      value: `${interviewRate}%`,
      subtext: `${interviewingCount + offerCount} of ${total} reached interviews`,
      icon: TrendingUp,
      iconBg: 'bg-amber-50 text-amber-600 border-amber-100',
    },
    {
      label: 'Offers Received',
      value: offerCount,
      subtext: `${offerRate}% offer conversion`,
      icon: Award,
      iconBg: 'bg-emerald-50 text-emerald-600 border-emerald-100',
    },
  ];

  const pipelineStages = [
    { label: 'Awaiting Response', count: awaitingResponse },
    { label: 'Interviewing', count: interviewingCount },
    { label: 'Offer', count: offerCount },
    { label: 'Rejected', count: rejectedCount },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Career Analytics</h1>
          <p className="text-sm text-slate-500 mt-1">
            Track your application funnel, interview conversion, and top matched roles
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={refreshDashboard}>
          <RefreshCw className="w-4 h-4 mr-1.5" /> Refresh
        </Button>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {kpiCards.map((kpi) => (
          <Card key={kpi.label}>
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                  {kpi.label}
                </p>
                <p className="text-3xl font-extrabold text-slate-900 mt-2">{kpi.value}</p>
                <p className="text-xs text-slate-500 mt-1">{kpi.subtext}</p>
              </div>
              <div
                className={`w-10 h-10 rounded-xl border flex items-center justify-center shrink-0 ${kpi.iconBg}`}
              >
                <kpi.icon className="w-5 h-5" />
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Application Pipeline Breakdown */}
        <Card
          title="Application Pipeline"
          subtitle="Distribution across your tracked application stages"
          action={
            <Link to="/applications" className="text-xs font-semibold text-indigo-600 hover:underline">
              Open Tracker
            </Link>
          }
        >
          {total === 0 ? (
            <div className="py-6 text-center text-sm text-slate-500">
              Submit applications from the Job Board to see your stage breakdown.
            </div>
          ) : (
            <div className="space-y-4">
              {pipelineStages.map((stage) => {
                const pct = total > 0 ? (stage.count * 100) / total : 0;
                return (
                  <ProgressIndicator
                    key={stage.label}
                    progress={pct}
                    label={`${stage.label} (${stage.count})`}
                  />
                );
              })}
            </div>
          )}
        </Card>

        {/* Top Matched Job Roles */}
        <Card
          title="Top 5 Matched Job Roles"
          subtitle="Highest compatibility roles from your saved resume matches"
          action={
            <Link to="/jobs?matched=true" className="text-xs font-semibold text-indigo-600 hover:underline">
              View All Matches
            </Link>
          }
        >
          {topMatchedRoles.length === 0 ? (
            <EmptyState
              icon={Sparkles}
              title="No matched roles yet"
              description="Run matching against active jobs using your analyzed resume to see your top-ranked opportunities here."
              actionLabel="Go to Job Matcher"
              onAction={() => navigate('/jobs?matched=true')}
            />
          ) : (
            <div className="space-y-3">
              {topMatchedRoles.map((role, index) => (
                <Link
                  key={role.jobId}
                  to={`/jobs/${role.jobId}`}
                  className="block p-3.5 rounded-xl border border-slate-200/80 hover:border-indigo-300 hover:bg-slate-50/80 transition-colors"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-400 font-mono">
                          #{index + 1}
                        </span>
                        <h4 className="text-sm font-semibold text-slate-900 truncate">
                          {role.jobTitle}
                        </h4>
                      </div>
                      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-500 mt-1">
                        <span className="font-medium text-slate-700">{role.company}</span>
                        <span className="inline-flex items-center gap-1">
                          <MapPin className="w-3 h-3 text-slate-400" /> {role.location}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      <Badge variant={getMatchBadgeVariant(role.matchScore)} size="sm">
                        {role.matchScore}%
                      </Badge>
                      <ArrowRight className="w-4 h-4 text-slate-400" />
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
