import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import jobService from '../../services/jobService';
import applicationService from '../../services/applicationService';
import JobCard from '../../components/jobs/JobCard';
import JobFormModal from '../../components/jobs/JobFormModal';
import Button from '../../components/common/Button';
import Card from '../../components/common/Card';
import Input from '../../components/common/Input';
import EmptyState from '../../components/common/EmptyState';
import ErrorMessage from '../../components/common/ErrorMessage';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ToastNotification from '../../components/common/ToastNotification';
import {
  Briefcase,
  Search,
  MapPin,
  Sparkles,
  Plus,
  RotateCcw,
  FileText,
  Filter,
} from 'lucide-react';
import { getApiErrorMessage } from '../../utils/jobFormatters';

const INITIAL_FILTERS = {
  q: '',
  skill: '',
  location: '',
  workMode: '',
  jobType: '',
  experienceLevel: '',
};

const selectClass =
  'block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500';

export default function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const isMatcherView = searchParams.get('matched') === 'true';

  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(INITIAL_FILTERS);
  const [jobs, setJobs] = useState([]);
  const [matchesByJobId, setMatchesByJobId] = useState({});
  const [applicationsByJobId, setApplicationsByJobId] = useState({});
  const [loading, setLoading] = useState(true);
  const [matchingAll, setMatchingAll] = useState(false);
  const [error, setError] = useState(null);
  const [resumeNotice, setResumeNotice] = useState(null);
  const [toast, setToast] = useState(null);

  const [postModalOpen, setPostModalOpen] = useState(false);
  const [creatingJob, setCreatingJob] = useState(false);
  const [createError, setCreateError] = useState(null);

  const fetchContextData = useCallback(async (activeFilters) => {
    const [jobsResult, appsResult, matchesResult] = await Promise.allSettled([
      jobService.listJobs(activeFilters),
      applicationService.listApplications(),
      jobService.listMatches(),
    ]);
    return { jobsResult, appsResult, matchesResult };
  }, []);

  const applyContextResults = useCallback(({ jobsResult, appsResult, matchesResult }) => {
    let nextError = null;
    let nextNotice = null;

    if (jobsResult.status === 'fulfilled') {
      setJobs(jobsResult.value || []);
    } else {
      nextError = getApiErrorMessage(jobsResult.reason, 'Failed to load jobs.');
    }

    if (appsResult.status === 'fulfilled') {
      const appMap = {};
      (appsResult.value || []).forEach((app) => {
        appMap[app.jobId] = app;
      });
      setApplicationsByJobId(appMap);
    } else if (!nextError) {
      nextError = getApiErrorMessage(appsResult.reason, 'Failed to load your application status.');
    }

    if (matchesResult.status === 'fulfilled') {
      const matchMap = {};
      (matchesResult.value || []).forEach((m) => {
        matchMap[m.jobId] = m;
      });
      setMatchesByJobId(matchMap);
    } else {
      const status = matchesResult.reason?.response?.status;
      if (status === 400) {
        setMatchesByJobId({});
        nextNotice = getApiErrorMessage(matchesResult.reason);
      } else if (!nextError) {
        nextError = getApiErrorMessage(matchesResult.reason, 'Failed to load match scores.');
      }
    }

    setError(nextError);
    setResumeNotice(nextNotice);
    setLoading(false);
  }, []);

  const reloadJobsAndContext = async (activeFilters) => {
    setLoading(true);
    setError(null);
    setResumeNotice(null);
    const results = await fetchContextData(activeFilters);
    applyContextResults(results);
  };

  useEffect(() => {
    let active = true;
    fetchContextData(appliedFilters).then((results) => {
      if (active) {
        applyContextResults(results);
      }
    });
    return () => {
      active = false;
    };
  }, [appliedFilters, fetchContextData, applyContextResults]);

  const handleApplyFilters = (e) => {
    e.preventDefault();
    setLoading(true);
    setAppliedFilters({ ...filters });
  };

  const handleResetFilters = () => {
    setLoading(true);
    setFilters(INITIAL_FILTERS);
    setAppliedFilters(INITIAL_FILTERS);
  };

  const handleRunAllMatches = async () => {
    setMatchingAll(true);
    setError(null);
    setResumeNotice(null);
    try {
      const results = await jobService.runAllMatches();
      const matchMap = {};
      (results || []).forEach((m) => {
        matchMap[m.jobId] = m;
      });
      setMatchesByJobId(matchMap);
      setToast({
        message: `Calculated match scores for ${results?.length || 0} active jobs.`,
        type: 'success',
      });
      if (!isMatcherView) {
        setSearchParams({ matched: 'true' });
      }
    } catch (err) {
      if (err?.response?.status === 400) {
        setResumeNotice(getApiErrorMessage(err));
      } else {
        setError(getApiErrorMessage(err, 'Failed to run resume matching across jobs.'));
      }
    } finally {
      setMatchingAll(false);
    }
  };

  const handleCreateJob = async (payload) => {
    setCreatingJob(true);
    setCreateError(null);
    try {
      await jobService.createJob(payload);
      setPostModalOpen(false);
      setToast({ message: 'Job posting published successfully.', type: 'success' });
      reloadJobsAndContext(appliedFilters);
    } catch (err) {
      setCreateError(getApiErrorMessage(err, 'Failed to create job posting.'));
    } finally {
      setCreatingJob(false);
    }
  };

  const displayedJobs = useMemo(() => {
    if (!isMatcherView) return jobs;
    return [...jobs].sort((a, b) => {
      const scoreA = matchesByJobId[a.id]?.matchScore ?? -1;
      const scoreB = matchesByJobId[b.id]?.matchScore ?? -1;
      return scoreB - scoreA;
    });
  }, [jobs, isMatcherView, matchesByJobId]);

  const hasActiveFilters = Object.values(appliedFilters).some((val) => Boolean(val));

  return (
    <div className="space-y-6">
      {toast && (
        <ToastNotification
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            {isMatcherView ? 'Job Matcher' : 'Job Board'}
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            {isMatcherView
              ? 'Jobs ranked by compatibility with your active analyzed resume'
              : 'Browse opportunities, filter by skills, and evaluate your resume fit'}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Button
            variant="outline"
            onClick={() => setSearchParams(isMatcherView ? {} : { matched: 'true' })}
          >
            <Filter className="w-4 h-4 mr-1.5" />
            {isMatcherView ? 'Standard Order' : 'Sort by Match'}
          </Button>
          <Button
            variant="secondary"
            loading={matchingAll}
            onClick={handleRunAllMatches}
          >
            <Sparkles className="w-4 h-4 mr-1.5 text-indigo-600" />
            Match All Jobs
          </Button>
          <Button
            variant="primary"
            onClick={() => {
              setCreateError(null);
              setPostModalOpen(true);
            }}
          >
            <Plus className="w-4 h-4 mr-1.5" />
            Post Job
          </Button>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* Resume readiness notice (shown prominently in Matcher view or after running matches without analyzed active resume) */}
      {resumeNotice && (isMatcherView || Object.keys(matchesByJobId).length === 0) && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="text-sm text-amber-900">
            <p className="font-semibold">Active Resume Required for Matching</p>
            <p className="text-xs text-amber-800 mt-0.5">{resumeNotice}</p>
          </div>
          <Link to="/resumes" className="shrink-0">
            <Button variant="outline" size="sm" className="bg-white">
              <FileText className="w-3.5 h-3.5 mr-1.5" /> Go to My Resumes
            </Button>
          </Link>
        </div>
      )}

      {/* Search & Filters */}
      <Card>
        <form onSubmit={handleApplyFilters} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Input
              label="Search"
              value={filters.q}
              onChange={(e) => setFilters((prev) => ({ ...prev, q: e.target.value }))}
              placeholder="Title, company, or keywords"
              icon={Search}
            />
            <Input
              label="Required Skills"
              value={filters.skill}
              onChange={(e) => setFilters((prev) => ({ ...prev, skill: e.target.value }))}
              placeholder="e.g. Java, React, Docker"
              icon={Sparkles}
            />
            <Input
              label="Location"
              value={filters.location}
              onChange={(e) => setFilters((prev) => ({ ...prev, location: e.target.value }))}
              placeholder="e.g. Bengaluru, Remote"
              icon={MapPin}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 lg:grid-cols-4 gap-4 items-end">
            <div>
              <label className="block text-xs font-semibold text-slate-700 tracking-wide uppercase mb-1">
                Work Mode
              </label>
              <select
                value={filters.workMode}
                onChange={(e) => setFilters((prev) => ({ ...prev, workMode: e.target.value }))}
                className={selectClass}
              >
                <option value="">All Work Modes</option>
                <option value="ONSITE">On-site</option>
                <option value="HYBRID">Hybrid</option>
                <option value="REMOTE">Remote</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 tracking-wide uppercase mb-1">
                Job Type
              </label>
              <select
                value={filters.jobType}
                onChange={(e) => setFilters((prev) => ({ ...prev, jobType: e.target.value }))}
                className={selectClass}
              >
                <option value="">All Job Types</option>
                <option value="FULL_TIME">Full-time</option>
                <option value="PART_TIME">Part-time</option>
                <option value="CONTRACT">Contract</option>
                <option value="INTERNSHIP">Internship</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 tracking-wide uppercase mb-1">
                Experience Level
              </label>
              <select
                value={filters.experienceLevel}
                onChange={(e) => setFilters((prev) => ({ ...prev, experienceLevel: e.target.value }))}
                className={selectClass}
              >
                <option value="">All Levels</option>
                <option value="ENTRY">Entry Level</option>
                <option value="MID">Mid Level</option>
                <option value="SENIOR">Senior</option>
                <option value="LEAD">Lead</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <Button type="submit" variant="primary" className="flex-1">
                Apply Filters
              </Button>
              {hasActiveFilters && (
                <Button type="button" variant="ghost" onClick={handleResetFilters} title="Clear filters">
                  <RotateCcw className="w-4 h-4" />
                </Button>
              )}
            </div>
          </div>
        </form>
      </Card>

      {/* Results */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <LoadingSkeleton count={4} variant="card" />
        </div>
      ) : displayedJobs.length === 0 ? (
        <EmptyState
          icon={Briefcase}
          title="No matching jobs found"
          description={
            hasActiveFilters
              ? 'Try widening your search terms or clearing some filters.'
              : 'No active jobs are currently posted. Publish a job posting to get started.'
          }
          actionLabel={hasActiveFilters ? 'Clear Filters' : 'Post a Job'}
          onAction={hasActiveFilters ? handleResetFilters : () => setPostModalOpen(true)}
        />
      ) : (
        <div className="space-y-3">
          <p className="text-xs font-medium text-slate-500">
            Showing {displayedJobs.length} active {displayedJobs.length === 1 ? 'role' : 'roles'}
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {displayedJobs.map((job) => (
              <JobCard
                key={job.id}
                job={job}
                match={matchesByJobId[job.id]}
                application={applicationsByJobId[job.id]}
              />
            ))}
          </div>
        </div>
      )}

      <JobFormModal
        isOpen={postModalOpen}
        onClose={() => setPostModalOpen(false)}
        onSubmit={handleCreateJob}
        submitting={creatingJob}
        serverError={createError}
      />
    </div>
  );
}
