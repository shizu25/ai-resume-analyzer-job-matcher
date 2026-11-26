import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import applicationService from '../../services/applicationService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import FormField from '../../components/common/FormField';
import EmptyState from '../../components/common/EmptyState';
import ErrorMessage from '../../components/common/ErrorMessage';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ToastNotification from '../../components/common/ToastNotification';
import {
  Send,
  Building2,
  MapPin,
  Sparkles,
  Calendar,
  FileText,
  Pencil,
  Trash2,
} from 'lucide-react';
import {
  APPLICATION_STATUS_CONFIG,
  WORK_MODE_LABELS,
  formatSalary,
  getMatchBadgeVariant,
  getApiErrorMessage,
} from '../../utils/jobFormatters';

const STATUS_TABS = [
  { key: 'ALL', label: 'All' },
  { key: 'APPLIED', label: 'Awaiting Response' },
  { key: 'INTERVIEWING', label: 'Interviewing' },
  { key: 'OFFER', label: 'Offer' },
  { key: 'REJECTED', label: 'Rejected' },
];

export default function ApplicationsPage() {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  const [editingApp, setEditingApp] = useState(null);
  const [editStatus, setEditStatus] = useState('APPLIED');
  const [editNotes, setEditNotes] = useState('');
  const [savingEdit, setSavingEdit] = useState(false);
  const [editError, setEditError] = useState(null);

  const [deletingApp, setDeletingApp] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let active = true;
    applicationService
      .listApplications(statusFilter === 'ALL' ? null : statusFilter)
      .then((data) => {
        if (!active) return;
        setApplications(data || []);
        setError(null);
      })
      .catch((err) => {
        if (!active) return;
        setError(getApiErrorMessage(err, 'Failed to load applications.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [statusFilter]);

  const handleStatusTabChange = (nextStatus) => {
    if (nextStatus === statusFilter) return;
    setLoading(true);
    setError(null);
    setStatusFilter(nextStatus);
  };

  const handleQuickStatusChange = async (app, nextStatus) => {
    if (app.status === nextStatus) return;
    setUpdatingId(app.id);
    setError(null);
    try {
      const updated = await applicationService.updateApplication(app.id, { status: nextStatus });
      setApplications((prev) => {
        if (statusFilter !== 'ALL' && updated.status !== statusFilter) {
          return prev.filter((item) => item.id !== app.id);
        }
        return prev.map((item) => (item.id === app.id ? updated : item));
      });
      setToast({ message: 'Application status updated.', type: 'success' });
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to update application status.'));
    } finally {
      setUpdatingId(null);
    }
  };

  const openEditModal = (app) => {
    setEditingApp(app);
    setEditStatus(app.status);
    setEditNotes(app.notes || '');
    setEditError(null);
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    if (!editingApp) return;
    setSavingEdit(true);
    setEditError(null);
    try {
      const updated = await applicationService.updateApplication(editingApp.id, {
        status: editStatus,
        notes: editNotes,
      });
      setApplications((prev) => {
        if (statusFilter !== 'ALL' && updated.status !== statusFilter) {
          return prev.filter((item) => item.id !== editingApp.id);
        }
        return prev.map((item) => (item.id === editingApp.id ? updated : item));
      });
      setEditingApp(null);
      setToast({ message: 'Application updated successfully.', type: 'success' });
    } catch (err) {
      setEditError(getApiErrorMessage(err, 'Failed to save changes.'));
    } finally {
      setSavingEdit(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deletingApp) return;
    setDeleting(true);
    setError(null);
    try {
      await applicationService.deleteApplication(deletingApp.id);
      setApplications((prev) => prev.filter((item) => item.id !== deletingApp.id));
      setDeletingApp(null);
      setToast({ message: 'Application removed from tracker.', type: 'success' });
    } catch (err) {
      setDeletingApp(null);
      setError(getApiErrorMessage(err, 'Failed to delete application.'));
    } finally {
      setDeleting(false);
    }
  };

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
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Application Tracker</h1>
          <p className="text-sm text-slate-500 mt-1">
            Monitor your job applications, interview stages, and notes
          </p>
        </div>
        <Link to="/jobs">
          <Button variant="primary">Browse Jobs</Button>
        </Link>
      </div>

      {/* Status Filter Tabs */}
      <div className="border-b border-slate-200 flex gap-2 overflow-x-auto pb-0.5">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            onClick={() => handleStatusTabChange(tab.key)}
            className={`px-4 py-2.5 text-sm font-semibold border-b-2 whitespace-nowrap transition-colors cursor-pointer ${
              statusFilter === tab.key
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* Applications List */}
      {loading ? (
        <LoadingSkeleton count={3} variant="card" />
      ) : applications.length === 0 ? (
        <EmptyState
          icon={Send}
          title={
            statusFilter === 'ALL'
              ? 'No applications tracked yet'
              : `No applications in ${APPLICATION_STATUS_CONFIG[statusFilter]?.label || statusFilter}`
          }
          description="Apply to roles from the Job Board to track your progress, match snapshots, and interview notes."
          actionLabel={statusFilter === 'ALL' ? 'Explore Jobs' : 'Show All Applications'}
          onAction={
            statusFilter === 'ALL' ? undefined : () => handleStatusTabChange('ALL')
          }
        />
      ) : (
        <div className="space-y-4">
          {applications.map((app) => {
            const statusCfg = APPLICATION_STATUS_CONFIG[app.status] || {
              label: app.status,
              badgeVariant: 'secondary',
            };

            return (
              <Card key={app.id} className="hover:border-slate-300 transition-shadow hover:shadow-md">
                <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                  <div className="space-y-2.5 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      {app.jobActive ? (
                        <Link
                          to={`/jobs/${app.jobId}`}
                          className="text-base font-semibold text-slate-900 hover:text-indigo-600 transition-colors"
                        >
                          {app.jobTitle}
                        </Link>
                      ) : (
                        <span className="text-base font-semibold text-slate-900">
                          {app.jobTitle}
                        </span>
                      )}
                      <Badge variant={statusCfg.badgeVariant} size="sm">
                        {statusCfg.label}
                      </Badge>
                      {app.matchScoreSnapshot != null && (
                        <Badge variant={getMatchBadgeVariant(app.matchScoreSnapshot)} size="sm">
                          <Sparkles className="w-3 h-3" /> {app.matchScoreSnapshot}% Match Snapshot
                        </Badge>
                      )}
                      {!app.jobActive && (
                        <Badge variant="secondary" size="sm">Closed Job</Badge>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500">
                      <span className="inline-flex items-center gap-1 font-medium text-slate-700">
                        <Building2 className="w-3.5 h-3.5 text-slate-400" /> {app.company}
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <MapPin className="w-3.5 h-3.5 text-slate-400" />
                        {app.location} ({WORK_MODE_LABELS[app.workMode] || app.workMode})
                      </span>
                      <span>
                        {formatSalary(app.salaryMin, app.salaryMax, app.salaryCurrency, app.salaryPeriod)}
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5 text-slate-400" />
                        Applied {new Date(app.appliedAt).toLocaleDateString()}
                      </span>
                      {app.resumeFileName && (
                        <span className="inline-flex items-center gap-1">
                          <FileText className="w-3.5 h-3.5 text-slate-400" />
                          {app.resumeFileName}
                        </span>
                      )}
                    </div>

                    {app.notes && (
                      <div className="bg-slate-50 border border-slate-200/70 rounded-lg px-3 py-2 text-xs text-slate-700 whitespace-pre-line">
                        {app.notes}
                      </div>
                    )}
                  </div>

                  {/* Controls */}
                  <div className="flex flex-wrap items-center gap-2 shrink-0">
                    <select
                      aria-label={`Status for ${app.jobTitle}`}
                      value={app.status}
                      disabled={updatingId === app.id}
                      onChange={(e) => handleQuickStatusChange(app, e.target.value)}
                      className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50"
                    >
                      <option value="APPLIED">Awaiting Response</option>
                      <option value="INTERVIEWING">Interviewing</option>
                      <option value="OFFER">Offer</option>
                      <option value="REJECTED">Rejected</option>
                    </select>

                    <Button variant="outline" size="sm" onClick={() => openEditModal(app)}>
                      <Pencil className="w-3.5 h-3.5 mr-1" /> Notes
                    </Button>

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setDeletingApp(app)}
                      className="text-rose-500 hover:text-rose-700 hover:bg-rose-50"
                      title="Delete application"
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Edit Notes & Status Modal */}
      <Modal
        isOpen={!!editingApp}
        onClose={() => {
          if (!savingEdit) setEditingApp(null);
        }}
        title={`Update Application — ${editingApp?.jobTitle || ''}`}
        footer={
          <>
            <Button variant="outline" onClick={() => setEditingApp(null)} disabled={savingEdit}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleSaveEdit} loading={savingEdit}>
              Save Changes
            </Button>
          </>
        }
      >
        <form onSubmit={handleSaveEdit} className="space-y-4">
          <ErrorMessage message={editError} />

          <FormField label="Application Status">
            <select
              value={editStatus}
              onChange={(e) => setEditStatus(e.target.value)}
              className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="APPLIED">Awaiting Response (APPLIED)</option>
              <option value="INTERVIEWING">Interviewing</option>
              <option value="OFFER">Offer</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </FormField>

          <FormField
            label="Notes"
            helperText="Leave blank and save to clear existing notes."
          >
            <textarea
              rows={4}
              value={editNotes}
              onChange={(e) => setEditNotes(e.target.value)}
              placeholder="Interview dates, recruiter feedback, salary discussion..."
              className="block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-800 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </FormField>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!deletingApp}
        onClose={() => {
          if (!deleting) setDeletingApp(null);
        }}
        title="Delete Application"
        footer={
          <>
            <Button variant="outline" onClick={() => setDeletingApp(null)} disabled={deleting}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleConfirmDelete} loading={deleting}>
              Remove Application
            </Button>
          </>
        }
      >
        <p className="text-sm text-slate-600">
          Remove your application record for{' '}
          <span className="font-semibold text-slate-900">{deletingApp?.jobTitle}</span> at{' '}
          <span className="font-semibold text-slate-900">{deletingApp?.company}</span>? This will not
          affect the job posting or your saved match results.
        </p>
      </Modal>
    </div>
  );
}
