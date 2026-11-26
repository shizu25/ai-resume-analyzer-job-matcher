import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Compass, Plus, ArrowRight, CheckCircle2, AlertCircle, Clock, BookOpen, Layers } from 'lucide-react';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import { roadmapService } from '../../services/roadmapService';
import { jobService } from '../../services/jobService';
import { resumeService } from '../../services/resumeService';

export default function RoadmapsPage() {
  const navigate = useNavigate();
  const [roadmaps, setRoadmaps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [selectedJobId, setSelectedJobId] = useState('');
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchRoadmaps();
  }, []);

  const fetchRoadmaps = async () => {
    try {
      setLoading(true);
      const data = await roadmapService.listRoadmaps();
      setRoadmaps(data || []);
    } catch (err) {
      console.error('Failed to load roadmaps:', err);
    } finally {
      setLoading(false);
    }
  };

  const openGenerateModal = async () => {
    setShowModal(true);
    setError(null);
    try {
      const [resumesData, jobsData] = await Promise.all([
        resumeService.listResumes(),
        jobService.listJobs(),
      ]);
      setResumes(resumesData || []);
      const jobsList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);
      setJobs(jobsList);

      const activeResume = (resumesData || []).find((r) => r.isActive);
      if (activeResume) {
        setSelectedResumeId(activeResume.id);
      } else if (resumesData?.length > 0) {
        setSelectedResumeId(resumesData[0].id);
      }
      if (jobsList.length > 0) {
        setSelectedJobId(jobsList[0].id);
      }
    } catch (err) {
      setError('Failed to fetch available resumes or jobs');
    }
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!selectedResumeId || !selectedJobId) {
      setError('Please select both a resume and a job opportunity');
      return;
    }
    try {
      setGenerating(true);
      setError(null);
      const res = await roadmapService.generateRoadmap(selectedResumeId, selectedJobId);
      setShowModal(false);
      navigate(`/roadmaps/${res.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate roadmap. Please verify AI Service is active.');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-200/80 pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 flex items-center gap-2">
            <Compass className="w-6 h-6 text-indigo-600" />
            Skill-Gap Roadmaps
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Personalized, evidence-grounded learning trajectories mapping candidate skills to target job requirements.
          </p>
        </div>
        <Button onClick={openGenerateModal} className="flex items-center gap-2 shrink-0">
          <Plus className="w-4 h-4" />
          Create New Roadmap
        </Button>
      </div>

      {/* Roadmaps Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-64 rounded-xl bg-slate-100 animate-pulse border border-slate-200" />
          ))}
        </div>
      ) : roadmaps.length === 0 ? (
        <EmptyState
          icon={Compass}
          title="No Learning Roadmaps Yet"
          description="Select any target job position to generate a comprehensive skill-gap analysis and prioritized learning plan."
          actionText="Create First Roadmap"
          onAction={openGenerateModal}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {roadmaps.map((rm) => {
            const coverage = Math.round(rm.overallCoveragePct || 0);
            const totalTasks = rm.totalTasksCount || rm.items?.length || 0;
            const completedTasks = rm.completedTasksCount || 0;
            const progressPct = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

            return (
              <Card
                key={rm.id}
                className="hover:border-indigo-300 hover:shadow-md transition-all flex flex-col justify-between"
              >
                <div className="space-y-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <h3 className="font-semibold text-slate-900 line-clamp-1">{rm.jobTitle}</h3>
                      <p className="text-xs text-slate-500 font-medium">{rm.company}</p>
                    </div>
                    <span
                      className={`px-2.5 py-1 text-xs font-bold rounded-full ${
                        coverage >= 75
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : coverage >= 50
                          ? 'bg-amber-50 text-amber-700 border border-amber-200'
                          : 'bg-rose-50 text-rose-700 border border-rose-200'
                      }`}
                    >
                      {coverage}% Match
                    </span>
                  </div>

                  {/* Resume File */}
                  <div className="text-xs text-slate-500 flex items-center gap-1.5 bg-slate-50 p-2 rounded-md">
                    <Layers className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                    <span className="truncate">{rm.resumeFileName}</span>
                  </div>

                  {/* Skill Breakdown */}
                  <div className="grid grid-cols-3 gap-2 py-2 border-y border-slate-100 text-center">
                    <div>
                      <span className="block text-sm font-bold text-emerald-600">
                        {rm.demonstratedSkillsCount || 0}
                      </span>
                      <span className="text-[11px] text-slate-500">Demonstrated</span>
                    </div>
                    <div>
                      <span className="block text-sm font-bold text-amber-600">
                        {rm.mentionedSkillsCount || 0}
                      </span>
                      <span className="text-[11px] text-slate-500">Mentioned</span>
                    </div>
                    <div>
                      <span className="block text-sm font-bold text-rose-600">
                        {rm.missingSkillsCount || 0}
                      </span>
                      <span className="text-[11px] text-slate-500">Missing</span>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div className="space-y-1.5">
                    <div className="flex justify-between text-xs text-slate-500">
                      <span>Tasks Completed</span>
                      <span className="font-medium text-slate-700">
                        {completedTasks} / {totalTasks} ({progressPct}%)
                      </span>
                    </div>
                    <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-indigo-600 rounded-full transition-all duration-300"
                        style={{ width: `${progressPct}%` }}
                      />
                    </div>
                  </div>
                </div>

                <div className="pt-4 mt-2 border-t border-slate-100">
                  <Button
                    variant="outline"
                    className="w-full justify-center flex items-center gap-2 text-indigo-600 hover:bg-indigo-50 border-indigo-200"
                    onClick={() => navigate(`/roadmaps/${rm.id}`)}
                  >
                    View Roadmap Trajectory
                    <ArrowRight className="w-4 h-4" />
                  </Button>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Modal to Generate New Roadmap */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="bg-white rounded-xl shadow-xl border border-slate-200 max-w-lg w-full p-6 space-y-5 animate-in fade-in duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Compass className="w-5 h-5 text-indigo-600" />
                Generate Skill-Gap Roadmap
              </h2>
              <button
                onClick={() => setShowModal(false)}
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

            <form onSubmit={handleGenerate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Select Candidate Resume
                </label>
                <select
                  value={selectedResumeId}
                  onChange={(e) => setSelectedResumeId(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                  required
                >
                  {resumes.map((r) => (
                    <option key={r.id} value={r.id}>
                      {r.fileName} {r.isActive ? '(Active Resume)' : ''}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                  Select Target Job Position
                </label>
                <select
                  value={selectedJobId}
                  onChange={(e) => setSelectedJobId(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg bg-white focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                  required
                >
                  {jobs.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.title} • {j.company}
                    </option>
                  ))}
                </select>
              </div>

              <div className="p-3 bg-indigo-50/60 rounded-lg text-xs text-indigo-800 space-y-1">
                <p className="font-semibold flex items-center gap-1.5">
                  <BookOpen className="w-4 h-4 text-indigo-600" />
                  What the Engine Generates:
                </p>
                <p>
                  • Rigorous classification: Demonstrated vs Mentioned vs Missing vs Related skills.
                </p>
                <p>
                  • Priority-weighted milestones with practical tasks, mini-projects & durations.
                </p>
                <p>• Actionable progress tracker persistent to your candidate account.</p>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowModal(false)}
                  disabled={generating}
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={generating}>
                  {generating ? 'Analyzing & Building Roadmap...' : 'Generate Roadmap'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
