import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Compass,
  ArrowLeft,
  RotateCw,
  CheckCircle2,
  Clock,
  AlertCircle,
  HelpCircle,
  Sparkles,
  BookOpen,
  Target,
  Code2,
  Calendar,
  Layers,
  Check,
  PlusCircle,
  GitCompare,
  X,
  FileEdit,
  ArrowRight,
} from 'lucide-react';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import { roadmapService } from '../../services/roadmapService';
import { simulatorService } from '../../services/simulatorService';

export default function RoadmapDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [roadmap, setRoadmap] = useState(null);
  const [loading, setLoading] = useState(true);
  const [regenerating, setRegenerating] = useState(false);
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [updatingItemId, setUpdatingItemId] = useState(null);

  // Integration to Resume Simulator state
  const [integratingItem, setIntegratingItem] = useState(null);
  const [versions, setVersions] = useState([]);
  const [selectedVersionId, setSelectedVersionId] = useState('NEW');
  const [newVersionName, setNewVersionName] = useState('');
  const [integrating, setIntegrating] = useState(false);
  const [integrationSuccess, setIntegrationSuccess] = useState(null);
  const [integrateError, setIntegrateError] = useState(null);

  useEffect(() => {
    fetchRoadmap();
  }, [id]);

  const fetchRoadmap = async () => {
    try {
      setLoading(true);
      const data = await roadmapService.getRoadmap(id);
      setRoadmap(data);
    } catch (err) {
      console.error('Failed to load roadmap:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRegenerate = async () => {
    if (!roadmap) return;
    try {
      setRegenerating(true);
      const updated = await roadmapService.regenerateRoadmap(roadmap.resumeId, roadmap.jobId);
      setRoadmap(updated);
    } catch (err) {
      console.error('Failed to regenerate roadmap:', err);
    } finally {
      setRegenerating(false);
    }
  };

  const handleProgressChange = async (itemId, newStatus) => {
    try {
      setUpdatingItemId(itemId);
      const updatedItem = await roadmapService.updateProgress(roadmap.id, itemId, newStatus);
      setRoadmap((prev) => {
        const nextItems = prev.items.map((i) => (i.id === itemId ? updatedItem : i));
        const completed = nextItems.filter((i) => i.progressStatus === 'COMPLETED').length;
        return {
          ...prev,
          items: nextItems,
          completedTasksCount: completed,
        };
      });
      return updatedItem;
    } catch (err) {
      console.error('Failed to update item progress:', err);
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleOpenIntegrateModal = async (item) => {
    setIntegratingItem(item);
    setIntegrationSuccess(null);
    setIntegrateError(null);
    setNewVersionName(`Tailored with ${item.skillName}`);
    try {
      const vList = await simulatorService.listVersions(roadmap.resumeId);
      setVersions(vList || []);
      if (vList && vList.length > 0) {
        setSelectedVersionId(vList[vList.length - 1].id);
      } else {
        setSelectedVersionId('NEW');
      }
    } catch (e) {
      setSelectedVersionId('NEW');
    }
  };

  const handleIntegrateSkill = async (e) => {
    e.preventDefault();
    if (!integratingItem) return;
    try {
      setIntegrating(true);
      setIntegrateError(null);
      let targetVersionId = selectedVersionId;

      if (selectedVersionId === 'NEW') {
        const newVer = await simulatorService.createVersion(roadmap.resumeId, {
          versionName: newVersionName.trim() || `Tailored for ${roadmap.jobTitle} with ${integratingItem.skillName}`,
          targetJobId: roadmap.jobId,
        });
        targetVersionId = newVer.id;
      }

      const updated = await simulatorService.integrateSkill(targetVersionId, {
        skillName: integratingItem.skillName,
        practicalTasks: integratingItem.practicalTasks,
        suggestedProject: integratingItem.suggestedProject,
      });

      setIntegrationSuccess({
        versionId: targetVersionId,
        versionName: updated.versionName,
        skillName: integratingItem.skillName,
      });
    } catch (err) {
      setIntegrateError(err.response?.data?.message || 'Failed to integrate skill into simulator version.');
    } finally {
      setIntegrating(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="h-8 w-48 bg-slate-100 rounded-md animate-pulse" />
        <div className="h-44 bg-slate-100 rounded-xl animate-pulse" />
        <div className="h-96 bg-slate-100 rounded-xl animate-pulse" />
      </div>
    );
  }

  if (!roadmap) {
    return (
      <div className="text-center py-16 space-y-4">
        <AlertCircle className="w-12 h-12 text-rose-500 mx-auto" />
        <h2 className="text-lg font-bold text-slate-800">Roadmap Not Found</h2>
        <Button onClick={() => navigate('/roadmaps')} variant="outline">
          Back to Roadmaps
        </Button>
      </div>
    );
  }

  const items = roadmap.items || [];
  const filteredItems = items.filter((item) => {
    if (priorityFilter !== 'ALL' && item.priority !== priorityFilter) return false;
    if (statusFilter !== 'ALL' && item.progressStatus !== statusFilter) return false;
    return true;
  });

  const totalTasks = roadmap.totalTasksCount || items.length;
  const completedTasks = roadmap.completedTasksCount || items.filter((i) => i.progressStatus === 'COMPLETED').length;
  const progressPct = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
  const coverage = Math.round(roadmap.overallCoveragePct || 0);

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-12">
      {/* Navigation Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/roadmaps')}
          className="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Roadmaps
        </button>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            onClick={() => navigate('/resume-versions')}
            className="flex items-center gap-1.5 text-xs text-indigo-600 border-indigo-200"
          >
            <Layers className="w-3.5 h-3.5" />
            Resume Simulator
          </Button>
          <Button
            variant="outline"
            onClick={handleRegenerate}
            disabled={regenerating}
            className="flex items-center gap-2 text-xs"
          >
            <RotateCw className={`w-3.5 h-3.5 ${regenerating ? 'animate-spin' : ''}`} />
            {regenerating ? 'Re-evaluating...' : 'Regenerate Roadmap'}
          </Button>
        </div>
      </div>

      {/* Target Job Overview Card */}
      <Card className="bg-gradient-to-br from-indigo-900 via-indigo-950 to-slate-900 text-white border-0 shadow-lg p-6 sm:p-8">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase tracking-wider bg-indigo-500/30 text-indigo-200 border border-indigo-400/30">
                Target Opportunity
              </span>
              <span className="text-xs text-indigo-200/80">• Resume: {roadmap.resumeFileName}</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              {roadmap.jobTitle}
            </h1>
            <p className="text-sm font-medium text-indigo-200">{roadmap.company}</p>
          </div>

          {/* Coverage Gauge Badge */}
          <div className="bg-white/10 backdrop-blur-md rounded-xl p-4 sm:px-6 sm:py-4 border border-white/15 flex items-center gap-5 shrink-0">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-indigo-200">
                Overall Skill Coverage
              </p>
              <div className="flex items-baseline gap-2 mt-1">
                <span className="text-3xl font-extrabold text-white">{coverage}%</span>
                <span className="text-xs text-indigo-200">
                  {coverage >= 75 ? 'Strong Match' : coverage >= 50 ? 'Moderate Fit' : 'High Growth Needed'}
                </span>
              </div>
            </div>
            <div className="w-12 h-12 rounded-full border-4 border-indigo-400/30 border-t-emerald-400 flex items-center justify-center font-bold text-xs text-white">
              {coverage}%
            </div>
          </div>
        </div>

        {/* 4-Quadrant Status Bar */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-8 pt-6 border-t border-indigo-800/40 text-center">
          <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-3">
            <span className="text-xl font-bold text-emerald-300 block">
              {roadmap.demonstratedSkillsCount || 0}
            </span>
            <span className="text-xs font-medium text-emerald-200">Demonstrated (Evidence)</span>
          </div>
          <div className="bg-amber-500/10 border border-amber-500/20 rounded-lg p-3">
            <span className="text-xl font-bold text-amber-300 block">
              {roadmap.mentionedSkillsCount || 0}
            </span>
            <span className="text-xs font-medium text-amber-200">Mentioned (Unsubstantiated)</span>
          </div>
          <div className="bg-rose-500/10 border border-rose-500/20 rounded-lg p-3">
            <span className="text-xl font-bold text-rose-300 block">
              {roadmap.missingSkillsCount || 0}
            </span>
            <span className="text-xs font-medium text-rose-200">Missing Gaps</span>
          </div>
          <div className="bg-purple-500/10 border border-purple-500/20 rounded-lg p-3">
            <span className="text-xl font-bold text-purple-300 block">
              {items.filter((i) => i.status === 'RELATED').length}
            </span>
            <span className="text-xs font-medium text-purple-200">Related Supporting</span>
          </div>
        </div>
      </Card>

      {/* Progress & Milestone Tracking Bar */}
      <Card className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="space-y-1 flex-1">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
            <span>Overall Roadmap Completion</span>
            <span className="text-indigo-600">
              {completedTasks} of {totalTasks} milestones completed ({progressPct}%)
            </span>
          </div>
          <div className="h-2.5 w-full bg-slate-100 rounded-full overflow-hidden">
            <div
              className="h-full bg-indigo-600 rounded-full transition-all duration-500"
              style={{ width: `${progressPct}%` }}
            />
          </div>
        </div>

        {/* Quick Filters */}
        <div className="flex items-center gap-2 shrink-0">
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="text-xs px-2.5 py-1.5 border border-slate-200 rounded-lg bg-white text-slate-700 font-medium focus:ring-1 focus:ring-indigo-500"
          >
            <option value="ALL">All Priorities</option>
            <option value="HIGH">High Priority</option>
            <option value="MEDIUM">Medium Priority</option>
            <option value="LOW">Low Priority</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="text-xs px-2.5 py-1.5 border border-slate-200 rounded-lg bg-white text-slate-700 font-medium focus:ring-1 focus:ring-indigo-500"
          >
            <option value="ALL">All Progress</option>
            <option value="NOT_STARTED">Not Started</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>
      </Card>

      {/* Roadmap Items List */}
      <div className="space-y-4">
        {filteredItems.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl border border-slate-200 p-8">
            <CheckCircle2 className="w-10 h-10 text-slate-400 mx-auto mb-2" />
            <p className="text-sm font-semibold text-slate-700">No milestones match the selected filters.</p>
          </div>
        ) : (
          filteredItems.map((item, idx) => {
            const isCompleted = item.progressStatus === 'COMPLETED';
            const isInProgress = item.progressStatus === 'IN_PROGRESS';

            const statusColors = {
              DEMONSTRATED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
              MENTIONED: 'bg-amber-50 text-amber-700 border-amber-200',
              MISSING: 'bg-rose-50 text-rose-700 border-rose-200',
              RELATED: 'bg-purple-50 text-purple-700 border-purple-200',
            };

            const priorityColors = {
              HIGH: 'bg-rose-100 text-rose-800 border-rose-200',
              MEDIUM: 'bg-amber-100 text-amber-800 border-amber-200',
              LOW: 'bg-blue-100 text-blue-800 border-blue-200',
            };

            return (
              <Card
                key={item.id || idx}
                className={`transition-all duration-200 ${
                  isCompleted
                    ? 'border-emerald-300 bg-emerald-50/15'
                    : isInProgress
                    ? 'border-indigo-300 shadow-sm'
                    : 'border-slate-200'
                }`}
              >
                <div className="space-y-4">
                  {/* Item Header */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3">
                    <div className="flex items-center gap-3">
                      <span className="w-7 h-7 rounded-full bg-slate-100 text-slate-600 font-bold text-xs flex items-center justify-center shrink-0">
                        {idx + 1}
                      </span>
                      <div>
                        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                          {item.skillName}
                          <span
                            className={`px-2 py-0.5 text-[11px] font-bold rounded-md border ${
                              statusColors[item.status] || 'bg-slate-100 text-slate-700'
                            }`}
                          >
                            {item.status}
                          </span>
                        </h3>
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <span
                        className={`px-2.5 py-0.5 text-xs font-bold rounded-full border ${
                          priorityColors[item.priority] || 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        {item.priority} PRIORITY
                      </span>

                      {/* Status Toggle Dropdown */}
                      <select
                        value={item.progressStatus || 'NOT_STARTED'}
                        disabled={updatingItemId === item.id}
                        onChange={(e) => handleProgressChange(item.id, e.target.value)}
                        className={`text-xs font-semibold px-2.5 py-1.5 rounded-lg border focus:outline-hidden transition-colors ${
                          isCompleted
                            ? 'bg-emerald-100 text-emerald-800 border-emerald-300'
                            : isInProgress
                            ? 'bg-indigo-100 text-indigo-800 border-indigo-300'
                            : 'bg-slate-100 text-slate-700 border-slate-200'
                        }`}
                      >
                        <option value="NOT_STARTED">Not Started</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="COMPLETED">Completed</option>
                      </select>

                      {/* Primary Action Button: Integrate Completed Skill into Simulator */}
                      {isCompleted ? (
                        <Button
                          size="sm"
                          onClick={() => handleOpenIntegrateModal(item)}
                          className="text-xs bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-1.5 shadow-xs py-1.5"
                        >
                          <PlusCircle className="w-3.5 h-3.5" />
                          Add to Resume Simulator
                        </Button>
                      ) : (
                        <button
                          onClick={async () => {
                            await handleProgressChange(item.id, 'COMPLETED');
                            handleOpenIntegrateModal(item);
                          }}
                          className="text-xs text-indigo-600 hover:text-indigo-800 font-semibold px-2 py-1 rounded hover:bg-indigo-50 transition-colors flex items-center gap-1"
                        >
                          Complete & Add &rarr;
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Why Required & Priority Reasoning */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs bg-slate-50 p-3 rounded-lg border border-slate-100">
                    <div>
                      <span className="font-bold text-slate-700 flex items-center gap-1.5 mb-1">
                        <Target className="w-3.5 h-3.5 text-indigo-600" />
                        Why Required
                      </span>
                      <p className="text-slate-600">{item.whyRequired}</p>
                    </div>
                    <div>
                      <span className="font-bold text-slate-700 flex items-center gap-1.5 mb-1">
                        <HelpCircle className="w-3.5 h-3.5 text-amber-600" />
                        Priority Calculation Rationale
                      </span>
                      <p className="text-slate-600">{item.priorityReason}</p>
                    </div>
                  </div>

                  {/* Learning Sequence Steps */}
                  {item.learningSequence && item.learningSequence.length > 0 && (
                    <div className="space-y-1.5">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                        <BookOpen className="w-3.5 h-3.5 text-indigo-600" />
                        Recommended Step-by-Step Learning Sequence
                      </h4>
                      <ol className="list-decimal list-inside space-y-1 text-xs text-slate-700 pl-1">
                        {item.learningSequence.map((step, sIdx) => (
                          <li key={sIdx} className="leading-relaxed">
                            <span className="font-medium text-slate-800">{step}</span>
                          </li>
                        ))}
                      </ol>
                    </div>
                  )}

                  {/* Practical Task & Suggested Mini-Project */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-1">
                    <div className="border border-slate-200 rounded-lg p-3 space-y-1">
                      <span className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                        <Code2 className="w-3.5 h-3.5 text-indigo-600" />
                        Practical Hands-on Task
                      </span>
                      <p className="text-xs text-slate-600">{item.practicalTasks}</p>
                    </div>

                    <div className="border border-slate-200 rounded-lg p-3 space-y-1">
                      <span className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                        <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                        Suggested Mini-Project
                      </span>
                      <p className="text-xs text-slate-600">{item.suggestedProject}</p>
                    </div>
                  </div>

                  {/* Footer: Duration & Completion Criteria */}
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pt-2 text-xs border-t border-slate-100 text-slate-500">
                    <div className="flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5 text-slate-400" />
                      <span>Estimated Duration: <strong className="text-slate-700">{item.estimatedDuration}</strong></span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Check className="w-3.5 h-3.5 text-emerald-600" />
                      <span>Completion Criteria: <strong className="text-slate-700">{item.completionCriteria}</strong></span>
                    </div>
                  </div>
                </div>
              </Card>
            );
          })
        )}
      </div>

      {/* Integrate Skill Modal */}
      {integratingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-lg w-full p-6 space-y-5 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <PlusCircle className="w-5 h-5 text-emerald-600" />
                Add Acquired Skill to Resume Simulator
              </h2>
              <button
                onClick={() => setIntegratingItem(null)}
                className="text-slate-400 hover:text-slate-600 text-lg font-semibold"
              >
                &times;
              </button>
            </div>

            {integrateError && (
              <div className="p-3 text-xs bg-rose-50 border border-rose-200 text-rose-700 rounded-lg flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                {integrateError}
              </div>
            )}

            {integrationSuccess ? (
              <div className="space-y-4 text-center py-2">
                <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
                  <Check className="w-6 h-6 stroke-[2.5]" />
                </div>
                <div>
                  <h3 className="font-bold text-slate-900 text-base">
                    Skill & Project Integrated Successfully!
                  </h3>
                  <p className="text-xs text-slate-600 mt-1">
                    <strong>{integrationSuccess.skillName}</strong> and its hands-on project evidence have been added to{' '}
                    <span className="font-semibold text-indigo-600">{integrationSuccess.versionName}</span>.
                  </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-2.5 pt-2">
                  <Button
                    onClick={() => navigate(`/resume-versions/${integrationSuccess.versionId}/edit`)}
                    className="flex-1 justify-center flex items-center gap-2 text-xs"
                  >
                    <FileEdit className="w-3.5 h-3.5" />
                    Open Resume Editor
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => navigate(`/resume-versions/compare?improvedId=${integrationSuccess.versionId}`)}
                    className="flex-1 justify-center flex items-center gap-2 text-xs text-indigo-600 border-indigo-200"
                  >
                    <GitCompare className="w-3.5 h-3.5" />
                    Simulate Score Delta
                  </Button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleIntegrateSkill} className="space-y-4">
                {/* Skill & Evidence Preview */}
                <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl space-y-2 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-slate-600">Acquired Skill:</span>
                    <span className="font-bold text-emerald-700 bg-emerald-100 px-2.5 py-0.5 rounded-full">
                      {integratingItem.skillName}
                    </span>
                  </div>
                  <div>
                    <span className="font-semibold text-slate-600 block mb-0.5">Injected Project Evidence:</span>
                    <p className="text-slate-800 font-medium">
                      "{integratingItem.suggestedProject || integratingItem.skillName + ' Project'}"
                    </p>
                    <p className="text-slate-500 text-[11px] mt-0.5">{integratingItem.practicalTasks}</p>
                  </div>
                </div>

                {/* Target Version Selector */}
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                    Select Target Resume Version
                  </label>
                  <select
                    value={selectedVersionId}
                    onChange={(e) => setSelectedVersionId(e.target.value)}
                    className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                    required
                  >
                    <option value="NEW">+ Create New Tailored Version (Cloned from Original)</option>
                    {versions.map((v) => (
                      <option key={v.id} value={v.id}>
                        Version {v.versionNumber}: {v.versionName}
                      </option>
                    ))}
                  </select>
                </div>

                {selectedVersionId === 'NEW' && (
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1.5">
                      New Version Name
                    </label>
                    <input
                      type="text"
                      value={newVersionName}
                      onChange={(e) => setNewVersionName(e.target.value)}
                      placeholder="e.g. Tailored for Cloud Role with Docker"
                      className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                      required
                    />
                  </div>
                )}

                <div className="p-3 bg-indigo-50/60 rounded-xl text-xs text-indigo-900 space-y-1">
                  <p className="font-semibold flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
                    How this Improves Your Match Score:
                  </p>
                  <p className="text-slate-600 text-[11px] leading-relaxed">
                    Rather than just adding a keyword, this injects both the skill and tangible project implementation into the simulator version. When you run dual simulation, this moves the skill from "Missing" to "Demonstrated" and increases both Skill Coverage and Project Evidence Relevance scores.
                  </p>
                </div>

                <div className="flex justify-end gap-2.5 pt-2 border-t border-slate-100">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setIntegratingItem(null)}
                    disabled={integrating}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={integrating} className="flex items-center gap-1.5">
                    {integrating ? 'Integrating...' : 'Integrate & Update Simulator'}
                    <ArrowRight className="w-4 h-4" />
                  </Button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
