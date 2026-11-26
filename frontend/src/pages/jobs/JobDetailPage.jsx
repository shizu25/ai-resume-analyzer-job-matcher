import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import jobService from '../../services/jobService';
import applicationService from '../../services/applicationService';
import MatchBreakdownCard from '../../components/jobs/MatchBreakdownCard';
import JobFormModal from '../../components/jobs/JobFormModal';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ErrorMessage from '../../components/common/ErrorMessage';
import ToastNotification from '../../components/common/ToastNotification';
import {
  ArrowLeft,
  Building2,
  MapPin,
  IndianRupee,
  Briefcase,
  ExternalLink,
  Pencil,
  Trash2,
  Send,
  CheckCircle2,
} from 'lucide-react';
import {
  WORK_MODE_LABELS,
  JOB_TYPE_LABELS,
  EXPERIENCE_LEVEL_LABELS,
  APPLICATION_STATUS_CONFIG,
  formatSalary,
  getApiErrorMessage,
} from '../../utils/jobFormatters';

export default function JobDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [job, setJob] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  const [match, setMatch] = useState(null);
  const [matchLoading, setMatchLoading] = useState(true);
  const [calculatingMatch, setCalculatingMatch] = useState(false);
  const [resumeError, setResumeError] = useState(null);

  const [existingApp, setExistingApp] = useState(null);
  const [applyModalOpen, setApplyModalOpen] = useState(false);
  const [applyNotes, setApplyNotes] = useState('');
  const [applying, setApplying] = useState(false);
  const [applyError, setApplyError] = useState(null);

  const [editModalOpen, setEditModalOpen] = useState(false);
  const [updatingJob, setUpdatingJob] = useState(false);
  const [editError, setEditError] = useState(null);

  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deletingJob, setDeletingJob] = useState(false);

  useEffect(() => {
    let active = true;

    const load = async () => {
      try {
        const jobData = await jobService.getJob(id);
        if (!active) return;
        setJob(jobData);
        setNotFound(false);
        setError(null);
        setLoading(false);
      } catch (err) {
        if (!active) return;
        if (err?.response?.status === 404) {
          setNotFound(true);
        } else {
          setError(getApiErrorMessage(err, 'Failed to load job details.'));
        }
        setLoading(false);
        setMatchLoading(false);
        return;
      }

      const [matchResult, appsResult] = await Promise.allSettled([
        jobService.getJobMatch(id),
        applicationService.listApplications(),
      ]);
      if (!active) return;

      if (matchResult.status === 'fulfilled') {
        setMatch(matchResult.value);
        setResumeError(null);
      } else {
        const status = matchResult.reason?.response?.status;
        if (status === 404) {
          setMatch(null);
        } else if (status === 400) {
          setMatch(null);
          setResumeError(getApiErrorMessage(matchResult.reason));
        } else {
          setError((prev) => prev || getApiErrorMessage(matchResult.reason, 'Failed to load match result.'));
        }
      }

      if (appsResult.status === 'fulfilled') {
        const found = (appsResult.value || []).find((app) => app.jobId === id);
        setExistingApp(found || null);
      } else {
        setError((prev) => prev || getApiErrorMessage(appsResult.reason, 'Failed to check application status.'));
      }

      setMatchLoading(false);
    };

    load();
    return () => {
      active = false;
    };
  }, [id]);

  const handleRunMatch = async () => {
    setCalculatingMatch(true);
    setResumeError(null);
    setError(null);
    try {
      const result = await jobService.matchJob(id);
      setMatch(result);
      setToast({ message: `Match score updated: ${result.matchScore}%`, type: 'success' });
    } catch (err) {
      if (err?.response?.status === 400) {
        setResumeError(getApiErrorMessage(err));
      } else {
        setError(getApiErrorMessage(err, 'Failed to calculate job match.'));
      }
    } finally {
      setCalculatingMatch(false);
    }
  };

  const handleApply = async (e) => {
    e.preventDefault();
    setApplying(true);
    setApplyError(null);
    try {
      const created = await applicationService.applyToJob({
        jobId: id,
        notes: applyNotes.trim() || null,
      });
      setExistingApp(created);
      setApplyModalOpen(false);
      setApplyNotes('');
      setToast({ message: 'Application submitted and added to tracker.', type: 'success' });
    } catch (err) {
      if (err?.response?.status === 409) {
        const msg = getApiErrorMessage(err, 'You have already applied to this job.');
        setApplyModalOpen(false);
        setToast({ message: msg, type: 'info' });
        try {
          const apps = await applicationService.listApplications();
          const found = (apps || []).find((app) => app.jobId === id);
          if (found) setExistingApp(found);
        } catch {
          // Ignore secondary refresh error
        }
      } else {
        setApplyError(getApiErrorMessage(err, 'Failed to submit application.'));
      }
    } finally {
      setApplying(false);
    }
  };

  const handleUpdateJob = async (payload) => {
    setUpdatingJob(true);
    setEditError(null);
    try {
      const updated = await jobService.updateJob(id, payload);
      setJob(updated);
      setEditModalOpen(false);
      setToast({ message: 'Job posting updated.', type: 'success' });
    } catch (err) {
      setEditError(getApiErrorMessage(err, 'Failed to update job posting.'));
    } finally {
      setUpdatingJob(false);
    }
  };

  const handleDeactivateJob = async () => {
    setDeletingJob(true);
    setError(null);
    try {
      await jobService.deactivateJob(id);
      setDeleteModalOpen(false);
      navigate('/jobs', { replace: true });
    } catch (err) {
      setDeleteModalOpen(false);
      setError(getApiErrorMessage(err, 'Failed to deactivate job posting.'));
    } finally {
      setDeletingJob(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <LoadingSkeleton count={3} variant="card" />
      </div>
    );
  }

  if (notFound || !job) {
    return (
      <Card className="text-center py-12">
        <h2 className="text-lg font-semibold text-slate-900">Job not found</h2>
        <p className="text-sm text-slate-500 mt-1">
          This job posting does not exist or has been deactivated.
        </p>
        <Link to="/jobs" className="inline-block mt-4">
          <Button variant="primary" size="sm">
            <ArrowLeft className="w-4 h-4 mr-1.5" /> Back to Job Board
          </Button>
        </Link>
      </Card>
    );
  }

  const appStatusCfg = existingApp ? APPLICATION_STATUS_CONFIG[existingApp.status] : null;

  return (
    <div className="space-y-6">
      {toast && (
        <ToastNotification
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Top Navigation & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link
            to="/jobs"
            className="p-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 transition-colors"
            title="Back to Jobs"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{job.title}</h1>
            <p className="text-sm text-slate-600 flex items-center gap-1.5 mt-0.5">
              <Building2 className="w-4 h-4 text-slate-400" />
              {job.company}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {job.canEdit && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setEditError(null);
                  setEditModalOpen(true);
                }}
              >
                <Pencil className="w-3.5 h-3.5 mr-1.5" /> Edit Job
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={() => setDeleteModalOpen(true)}
              >
                <Trash2 className="w-3.5 h-3.5 mr-1.5" /> Deactivate
              </Button>
            </>
          )}

          {existingApp ? (
            <Link to="/applications">
              <Button variant="success" size="sm">
                <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
                {appStatusCfg?.label || existingApp.status} — View in Tracker
              </Button>
            </Link>
          ) : (
            <Button
              variant="primary"
              size="sm"
              onClick={() => {
                setApplyError(null);
                setApplyModalOpen(true);
              }}
            >
              <Send className="w-3.5 h-3.5 mr-1.5" /> Apply to Job
            </Button>
          )}
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* Job Details Card */}
      <Card>
        <div className="space-y-5">
          <div className="flex flex-wrap items-center gap-3 text-sm text-slate-600">
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-slate-400" /> {job.location}
            </span>
            <span className="text-slate-300">•</span>
            <span className="inline-flex items-center gap-1.5">
              <IndianRupee className="w-4 h-4 text-slate-400" />
              {formatSalary(job.salaryMin, job.salaryMax, job.salaryCurrency, job.salaryPeriod)}
            </span>
            <span className="text-slate-300">•</span>
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

          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
              Role Description
            </h2>
            <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-line">
              {job.description}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2 border-t border-slate-100">
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                Required Skills
              </h3>
              <div className="flex flex-wrap gap-1.5">
                {(job.requiredSkills || []).map((skill) => (
                  <Badge key={skill} variant="primary" size="sm">
                    {skill}
                  </Badge>
                ))}
              </div>
            </div>

            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                Preferred Skills
              </h3>
              {job.preferredSkills?.length > 0 ? (
                <div className="flex flex-wrap gap-1.5">
                  {job.preferredSkills.map((skill) => (
                    <Badge key={skill} variant="secondary" size="sm">
                      {skill}
                    </Badge>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-400">No optional preferred skills listed.</p>
              )}
            </div>
          </div>

          {job.applicationUrl && (
            <div className="pt-2 border-t border-slate-100">
              <a
                href={job.applicationUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 text-sm font-medium text-indigo-600 hover:text-indigo-700 hover:underline"
              >
                External Job Posting <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
          )}
        </div>
      </Card>

      {/* Match Breakdown */}
      <MatchBreakdownCard
        match={match}
        loading={matchLoading}
        calculating={calculatingMatch}
        resumeError={resumeError}
        onRunMatch={handleRunMatch}
      />

      {/* Apply Modal */}
      <Modal
        isOpen={applyModalOpen}
        onClose={() => {
          if (!applying) setApplyModalOpen(false);
        }}
        title={`Apply to ${job.title}`}
        footer={
          <>
            <Button variant="outline" onClick={() => setApplyModalOpen(false)} disabled={applying}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleApply} loading={applying}>
              Submit Application
            </Button>
          </>
        }
      >
        <form onSubmit={handleApply} className="space-y-4">
          <ErrorMessage message={applyError} />
          <p className="text-sm text-slate-600">
            Track your application for <span className="font-semibold text-slate-900">{job.title}</span> at{' '}
            <span className="font-semibold text-slate-900">{job.company}</span>. Your active resume and
            latest match score snapshot (if available) will be attached automatically.
          </p>
          <FormField label="Notes (Optional)">
            <textarea
              rows={4}
              value={applyNotes}
              onChange={(e) => setApplyNotes(e.target.value)}
              placeholder="e.g. Applied via company portal, referral contact, target interview date..."
              className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </FormField>
        </form>
      </Modal>

      {/* Owner Edit Modal */}
      <JobFormModal
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        onSubmit={handleUpdateJob}
        initialJob={job}
        submitting={updatingJob}
        serverError={editError}
      />

      {/* Owner Deactivate Confirmation Modal */}
      <Modal
        isOpen={deleteModalOpen}
        onClose={() => {
          if (!deletingJob) setDeleteModalOpen(false);
        }}
        title="Deactivate Job Posting"
        footer={
          <>
            <Button variant="outline" onClick={() => setDeleteModalOpen(false)} disabled={deletingJob}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDeactivateJob} loading={deletingJob}>
              Deactivate Job
            </Button>
          </>
        }
      >
        <p className="text-sm text-slate-600">
          Are you sure you want to deactivate <span className="font-semibold text-slate-900">{job.title}</span>?
          It will be hidden from the Job Board while preserving existing application history.
        </p>
      </Modal>
    </div>
  );
}
