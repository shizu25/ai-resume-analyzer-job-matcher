import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Layers, Plus, FileEdit, GitCompare, ArrowRight, Clock, AlertCircle, Briefcase, Check, Eye } from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import { simulatorService } from '../../services/simulatorService';
import { resumeService } from '../../services/resumeService';
import { jobService } from '../../services/jobService';
import TailoredResumeViewer from '../../components/simulator/TailoredResumeViewer';

export default function ResumeVersionsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const parentResumeIdQuery = searchParams.get('resumeId');

  const [versions, setVersions] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewVersion, setPreviewVersion] = useState(null);

  // Create Version Modal State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedParentResumeId, setSelectedParentResumeId] = useState(parentResumeIdQuery || '');
  const [versionName, setVersionName] = useState('');
  const [selectedJobId, setSelectedJobId] = useState('');
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
  }, [parentResumeIdQuery]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [versionsData, resumesData, jobsData] = await Promise.all([
        parentResumeIdQuery
          ? simulatorService.listVersions(parentResumeIdQuery)
          : simulatorService.listAllUserVersions(),
        resumeService.listResumes(),
        jobService.listJobs(),
      ]);

      setVersions(versionsData || []);
      setResumes(resumesData || []);
      const jobsList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);
      setJobs(jobsList);

      if (!selectedParentResumeId && resumesData?.length > 0) {
        setSelectedParentResumeId(resumesData[0].id);
      }
    } catch (err) {
      console.error('Failed to load versions data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateVersion = async (e) => {
    e.preventDefault();
    if (!selectedParentResumeId || !versionName.trim()) {
      setError('Please provide a parent resume and version name');
      return;
    }

    try {
      setCreating(true);
      setError(null);
      const payload = {
        versionName: versionName.trim(),
        targetJobId: selectedJobId || null,
      };
      const created = await simulatorService.createVersion(selectedParentResumeId, payload);
      setShowCreateModal(false);
      setVersionName('');
      navigate(`/resume-versions/${created.id}/edit`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create resume version');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-200/80 pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Layers className="w-6 h-6 text-indigo-600" />
            Resume Version Management
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Maintain tailored variants for different roles. Edit content, apply AI improvements, and simulate match score deltas without overwriting your original resume.
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <Button
            variant="outline"
            onClick={() => navigate('/resume-versions/compare')}
            className="flex items-center gap-2 text-indigo-600 border-indigo-200"
          >
            <GitCompare className="w-4 h-4" />
            Launch Comparison Simulator
          </Button>
          <Button onClick={() => setShowCreateModal(true)} className="flex items-center gap-2">
            <Plus className="w-4 h-4" />
            Create New Version
          </Button>
        </div>
      </div>

      {/* Version List */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-56 rounded-xl bg-slate-100 animate-pulse border border-slate-200" />
          ))}
        </div>
      ) : versions.length === 0 ? (
        <EmptyState
          icon={Layers}
          title="No Resume Versions Created Yet"
          description="Create your first tailored resume version from any uploaded resume to start optimizing and simulating improvements."
          actionText="Create Version V1"
          onAction={() => setShowCreateModal(true)}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {versions.map((ver) => (
            <Card
              key={ver.id}
              className="hover:border-indigo-300 hover:shadow-md transition-all flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <span className="inline-block px-2 py-0.5 rounded text-[11px] font-bold bg-indigo-100 text-indigo-800 mb-1">
                      Version {ver.versionNumber}
                    </span>
                    <h3 className="font-bold text-slate-900 line-clamp-1">{ver.versionName}</h3>
                  </div>
                  {ver.targetJobTitle && (
                    <span className="text-[11px] px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 font-medium truncate max-w-[120px]">
                      {ver.targetJobTitle}
                    </span>
                  )}
                </div>

                <div className="text-xs text-slate-500 space-y-1 bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                  <div className="flex items-center justify-between">
                    <span>Generated Suggestions:</span>
                    <strong className="text-indigo-600 font-semibold">{ver.suggestionCount || 0}</strong>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Last Updated:</span>
                    <span>{ver.updatedAt ? new Date(ver.updatedAt).toLocaleDateString() : 'Recently'}</span>
                  </div>
                </div>
              </div>

              <div className="pt-4 mt-3 border-t border-slate-100 flex items-center gap-2">
                <Button
                  onClick={() => navigate(`/resume-versions/${ver.id}/edit`)}
                  className="w-full justify-center flex items-center gap-2 text-xs"
                >
                  <FileEdit className="w-3.5 h-3.5" />
                  Edit & Review Suggestions
                </Button>
                <Button
                  variant="outline"
                  onClick={() => setPreviewVersion(ver)}
                  className="text-xs px-2.5 shrink-0 text-emerald-700 border-emerald-300 hover:bg-emerald-50"
                  title="Preview & Export Tailored Resume"
                >
                  <Eye className="w-3.5 h-3.5" />
                </Button>
                <Button
                  variant="outline"
                  onClick={() => navigate(`/resume-versions/compare?improvedId=${ver.id}`)}
                  className="text-xs px-2.5 shrink-0"
                  title="Compare with another version"
                >
                  <GitCompare className="w-3.5 h-3.5 text-slate-600" />
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Modal to Create New Version */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="bg-white rounded-xl shadow-xl border border-slate-200 max-w-lg w-full p-6 space-y-5 animate-in fade-in duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Layers className="w-5 h-5 text-indigo-600" />
                Create New Resume Version
              </h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-slate-400 hover:text-slate-600 text-lg font-semibold"
              >
                &times;
              </button>
            </div>

            {error && (
              <div className="p-3 text-xs bg-rose-50 border border-rose-200 text-rose-700 rounded-lg flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                {error}
              </div>
            )}

            <form onSubmit={handleCreateVersion} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Base Parent Resume
                </label>
                <select
                  value={selectedParentResumeId}
                  onChange={(e) => setSelectedParentResumeId(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                  required
                >
                  {resumes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.fileName} {r.isActive ? '(Active)' : ''}
                    </option>
                  ))}
                </select>
                <p className="text-[11px] text-slate-500 mt-1">
                  The original resume remains completely unchanged. A new version draft will be cloned.
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Version Name / Label
                </label>
                <input
                  type="text"
                  value={versionName}
                  onChange={(e) => setVersionName(e.target.value)}
                  placeholder="e.g. Tailored for Cloud Backend Engineer"
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Optional Target Job (For Tailored Guidance)
                </label>
                <select
                  value={selectedJobId}
                  onChange={(e) => setSelectedJobId(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="">-- General Purpose Version --</option>
                  {jobs.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.title} • {j.company}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowCreateModal(false)}
                  disabled={creating}
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={creating}>
                  {creating ? 'Cloning & Initializing...' : 'Create Version'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Tailored Resume Viewer Modal */}
      {previewVersion && (
        <TailoredResumeViewer
          isOpen={!!previewVersion}
          onClose={() => setPreviewVersion(null)}
          versionName={previewVersion.versionName}
          versionNumber={previewVersion.versionNumber}
          content={(() => {
            try {
              return JSON.parse(previewVersion.contentJson || '{}');
            } catch {
              return {};
            }
          })()}
          rawText={previewVersion.rawText}
        />
      )}
    </div>
  );
}
