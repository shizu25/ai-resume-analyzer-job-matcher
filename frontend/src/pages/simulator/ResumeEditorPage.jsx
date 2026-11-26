import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Save,
  Sparkles,
  GitCompare,
  CheckCircle2,
  XCircle,
  HelpCircle,
  AlertCircle,
  FileText,
  Lightbulb,
  Check,
  RefreshCw,
  Eye,
} from 'lucide-react';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import { simulatorService } from '../../services/simulatorService';
import { jobService } from '../../services/jobService';
import TailoredResumeViewer from '../../components/simulator/TailoredResumeViewer';

export default function ResumeEditorPage() {
  const { versionId } = useParams();
  const navigate = useNavigate();

  const [version, setVersion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [showPreview, setShowPreview] = useState(false);

  // Editable fields
  const [versionName, setVersionName] = useState('');
  const [content, setContent] = useState({
    candidate_name: '',
    email: '',
    phone: '',
    summary: '',
    skills: [],
    experience: [],
    projects: [],
    education: [],
  });
  const [rawText, setRawText] = useState('');

  // Suggestions state
  const [suggestions, setSuggestions] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedJobId, setSelectedJobId] = useState('');
  const [generatingSuggestions, setGeneratingSuggestions] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  useEffect(() => {
    loadVersionAndJobs();
  }, [versionId]);

  const loadVersionAndJobs = async () => {
    try {
      setLoading(true);
      const [ver, jobsData] = await Promise.all([
        simulatorService.getVersion(versionId),
        jobService.listJobs(),
      ]);

      setVersion(ver);
      setVersionName(ver.versionName || '');
      setRawText(ver.rawText || '');

      try {
        const parsed = JSON.parse(ver.contentJson || '{}');
        setContent({
          candidate_name: parsed.candidate_name || '',
          email: parsed.email || '',
          phone: parsed.phone || '',
          summary: parsed.summary || '',
          skills: Array.isArray(parsed.skills) ? parsed.skills : [],
          experience: Array.isArray(parsed.experience) ? parsed.experience : [],
          projects: Array.isArray(parsed.projects) ? parsed.projects : [],
          education: Array.isArray(parsed.education) ? parsed.education : [],
        });
      } catch (e) {
        // Fallback
      }

      const jobsList = jobsData?.content || (Array.isArray(jobsData) ? jobsData : []);
      setJobs(jobsList);
      if (ver.targetJobId) {
        setSelectedJobId(ver.targetJobId);
      } else if (jobsList.length > 0) {
        setSelectedJobId(jobsList[0].id);
      }

      // Load existing suggestions
      const suggs = await simulatorService.getSuggestions(versionId);
      setSuggestions(suggs || []);
    } catch (err) {
      console.error('Failed to load version:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      const payload = {
        versionName,
        contentJson: JSON.stringify(content),
        rawText,
      };
      await simulatorService.updateVersion(versionId, payload);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      console.error('Failed to save version:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleGenerateSuggestions = async () => {
    if (!selectedJobId) return;
    try {
      setGeneratingSuggestions(true);
      const suggs = await simulatorService.generateSuggestions(versionId, selectedJobId);
      setSuggestions(suggs || []);
    } catch (err) {
      console.error('Failed to generate suggestions:', err);
    } finally {
      setGeneratingSuggestions(false);
    }
  };

  const handleAcceptSuggestion = async (suggestion) => {
    try {
      // 1. Mark as accepted in backend
      const updated = await simulatorService.updateSuggestionStatus(suggestion.id, 'ACCEPTED');
      setSuggestions((prev) => prev.map((s) => (s.id === suggestion.id ? updated : s)));

      // 2. Intelligently apply replacement to editable state
      const orig = suggestion.originalText;
      const repl = suggestion.suggestedText;

      // Check summary
      if (content.summary && content.summary.includes(orig)) {
        setContent((prev) => ({
          ...prev,
          summary: prev.summary.replace(orig, repl),
        }));
      }

      // Check experience descriptions
      const updatedExp = (content.experience || []).map((exp) => {
        if (exp.description && exp.description.includes(orig)) {
          return { ...exp, description: exp.description.replace(orig, repl) };
        }
        return exp;
      });

      // Check project descriptions
      const updatedProj = (content.projects || []).map((proj) => {
        if (proj.description && proj.description.includes(orig)) {
          return { ...proj, description: proj.description.replace(orig, repl) };
        }
        return proj;
      });

      setContent((prev) => ({
        ...prev,
        experience: updatedExp,
        projects: updatedProj,
      }));

      // Also update rawText if present
      if (rawText.includes(orig)) {
        setRawText((prev) => prev.replace(orig, repl));
      }
    } catch (err) {
      console.error('Failed to accept suggestion:', err);
    }
  };

  const handleRejectSuggestion = async (suggestionId) => {
    try {
      const updated = await simulatorService.updateSuggestionStatus(suggestionId, 'REJECTED');
      setSuggestions((prev) => prev.map((s) => (s.id === suggestionId ? updated : s)));
    } catch (err) {
      console.error('Failed to reject suggestion:', err);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="h-8 w-48 bg-slate-100 rounded-md animate-pulse" />
        <div className="h-96 bg-slate-100 rounded-xl animate-pulse" />
      </div>
    );
  }

  const filteredSuggestions = suggestions.filter((s) => {
    if (categoryFilter === 'ALL') return true;
    return s.category === categoryFilter;
  });

  return (
    <div className="max-w-7xl mx-auto space-y-6 pb-12">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-200/80 pb-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/resumes')}
            className="p-1.5 rounded-lg text-slate-500 hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-indigo-100 text-indigo-800">
                Version {version.versionNumber}
              </span>
              <input
                type="text"
                value={versionName}
                onChange={(e) => setVersionName(e.target.value)}
                className="font-bold text-lg text-slate-900 border-b border-transparent hover:border-slate-300 focus:border-indigo-500 focus:outline-hidden px-1"
              />
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Interactive Resume Editor • Never overwrites parent resume
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => setShowPreview(true)}
            className="flex items-center gap-2 text-xs text-emerald-700 bg-emerald-50 border-emerald-300 hover:bg-emerald-100"
          >
            <Eye className="w-3.5 h-3.5" />
            Preview & Export Tailored Resume
          </Button>

          <Button
            variant="outline"
            onClick={() => navigate(`/resume-versions/compare?improvedId=${version.id}`)}
            className="flex items-center gap-2 text-xs text-indigo-600 border-indigo-200"
          >
            <GitCompare className="w-3.5 h-3.5" />
            Simulate Score Delta
          </Button>

          <Button
            onClick={handleSave}
            disabled={saving}
            className={`flex items-center gap-2 text-xs ${saveSuccess ? 'bg-emerald-600 hover:bg-emerald-700' : ''}`}
          >
            {saveSuccess ? (
              <>
                <Check className="w-4 h-4" />
                Draft Saved!
              </>
            ) : (
              <>
                <Save className="w-4 h-4" />
                {saving ? 'Saving...' : 'Save Draft'}
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Main Split Layout: Editor (Left) & Suggestions (Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Pane: Interactive Structured Editor (7 cols) */}
        <div className="lg:col-span-7 space-y-6">
          {/* Summary Section */}
          <Card className="space-y-3">
            <h3 className="font-bold text-sm text-slate-800 flex items-center gap-2">
              <FileText className="w-4 h-4 text-indigo-600" />
              Professional Summary
            </h3>
            <textarea
              rows={4}
              value={content.summary}
              onChange={(e) => setContent({ ...content, summary: e.target.value })}
              placeholder="Enter your concise professional summary..."
              className="w-full text-xs p-3 border border-slate-200 rounded-lg focus:ring-1 focus:ring-indigo-500 focus:outline-hidden leading-relaxed"
            />
          </Card>

          {/* Technical Skills List */}
          <Card className="space-y-3">
            <h3 className="font-bold text-sm text-slate-800 flex items-center gap-2">
              <Lightbulb className="w-4 h-4 text-amber-500" />
              Technical Skills
            </h3>
            <div className="flex flex-wrap gap-1.5 p-3 bg-slate-50 rounded-lg border border-slate-200 min-h-[50px]">
              {(content.skills || []).map((skill, sIdx) => (
                <span
                  key={sIdx}
                  className="px-2.5 py-1 text-xs font-semibold bg-white border border-slate-200 text-slate-800 rounded-md shadow-2xs"
                >
                  {skill}
                </span>
              ))}
            </div>
            <p className="text-[11px] text-slate-500">
              Skills are anchored to evidence in your experience and project descriptions.
            </p>
          </Card>

          {/* Experience Section */}
          <Card className="space-y-4">
            <h3 className="font-bold text-sm text-slate-800">Work Experience</h3>
            {(content.experience || []).length === 0 ? (
              <p className="text-xs text-slate-500 italic">No experience entries recorded.</p>
            ) : (
              content.experience.map((exp, eIdx) => (
                <div key={eIdx} className="p-3 bg-slate-50 rounded-lg border border-slate-200 space-y-2">
                  <div className="flex items-center justify-between text-xs font-bold text-slate-800">
                    <span>{exp.position || 'Position'} • {exp.company || 'Company'}</span>
                    <span className="text-slate-500 font-normal">{exp.start_date} - {exp.end_date || 'Present'}</span>
                  </div>
                  <textarea
                    rows={3}
                    value={exp.description || ''}
                    onChange={(e) => {
                      const updated = [...content.experience];
                      updated[eIdx].description = e.target.value;
                      setContent({ ...content, experience: updated });
                    }}
                    className="w-full text-xs p-2.5 bg-white border border-slate-200 rounded-md focus:ring-1 focus:ring-indigo-500 focus:outline-hidden leading-relaxed"
                  />
                </div>
              ))
            )}
          </Card>

          {/* Projects Section */}
          <Card className="space-y-4">
            <h3 className="font-bold text-sm text-slate-800">Projects</h3>
            {(content.projects || []).length === 0 ? (
              <p className="text-xs text-slate-500 italic">No project entries recorded.</p>
            ) : (
              content.projects.map((proj, pIdx) => (
                <div key={pIdx} className="p-3 bg-slate-50 rounded-lg border border-slate-200 space-y-2">
                  <div className="flex items-center justify-between text-xs font-bold text-slate-800">
                    <span>{proj.title || 'Project Title'}</span>
                    <span className="text-indigo-600 font-semibold">{proj.technologies}</span>
                  </div>
                  <textarea
                    rows={3}
                    value={proj.description || ''}
                    onChange={(e) => {
                      const updated = [...content.projects];
                      updated[pIdx].description = e.target.value;
                      setContent({ ...content, projects: updated });
                    }}
                    className="w-full text-xs p-2.5 bg-white border border-slate-200 rounded-md focus:ring-1 focus:ring-indigo-500 focus:outline-hidden leading-relaxed"
                  />
                </div>
              ))
            )}
          </Card>
        </div>

        {/* Right Pane: AI Suggestion Engine (5 cols) */}
        <div className="lg:col-span-5 space-y-4">
          <Card className="p-4 bg-indigo-50/50 border-indigo-200 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold uppercase tracking-wider text-indigo-900 flex items-center gap-1.5">
                <Sparkles className="w-4 h-4 text-indigo-600" />
                AI Improvement Simulator
              </h3>
              <span className="text-[11px] font-bold text-indigo-700 bg-indigo-100 px-2 py-0.5 rounded-full">
                Zero-Hallucination Guardrail
              </span>
            </div>

            <div className="space-y-2">
              <label className="block text-[11px] font-bold text-slate-600 uppercase tracking-wider">
                Target Role Alignment
              </label>
              <div className="flex gap-2">
                <select
                  value={selectedJobId}
                  onChange={(e) => setSelectedJobId(e.target.value)}
                  className="flex-1 text-xs px-2.5 py-1.5 border border-slate-200 rounded-lg bg-white focus:ring-1 focus:ring-indigo-500"
                >
                  {jobs.map((j) => (
                    <option key={j.id} value={j.id}>
                      {j.title} • {j.company}
                    </option>
                  ))}
                </select>
                <Button
                  onClick={handleGenerateSuggestions}
                  disabled={generatingSuggestions}
                  className="text-xs shrink-0 flex items-center gap-1.5"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${generatingSuggestions ? 'animate-spin' : ''}`} />
                  {generatingSuggestions ? 'Analyzing...' : 'Generate'}
                </Button>
              </div>
            </div>
          </Card>

          {/* Category Chips */}
          <div className="flex flex-wrap gap-1 text-[11px]">
            {['ALL', 'WEAK_PROJECT_DESCRIPTION', 'MISSING_SKILL_EVIDENCE', 'UNCLEAR_CONTRIBUTION'].map(
              (cat) => (
                <button
                  key={cat}
                  onClick={() => setCategoryFilter(cat)}
                  className={`px-2 py-1 rounded-md font-medium border transition-colors ${
                    categoryFilter === cat
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  {cat.replace(/_/g, ' ')}
                </button>
              )
            )}
          </div>

          {/* Suggestion Cards */}
          <div className="space-y-3">
            {filteredSuggestions.length === 0 ? (
              <div className="p-8 text-center bg-white rounded-xl border border-slate-200 text-slate-500 text-xs">
                <Sparkles className="w-8 h-8 text-slate-300 mx-auto mb-2" />
                <p className="font-semibold text-slate-700">No suggestions in this category.</p>
                <p className="text-slate-400 mt-1">
                  Click "Generate" above to run the grounded improvement engine against the target job.
                </p>
              </div>
            ) : (
              filteredSuggestions.map((sugg) => {
                const isAccepted = sugg.status === 'ACCEPTED';
                const isRejected = sugg.status === 'REJECTED';

                return (
                  <Card
                    key={sugg.id}
                    className={`p-4 transition-all ${
                      isAccepted
                        ? 'border-emerald-300 bg-emerald-50/20'
                        : isRejected
                        ? 'opacity-50 border-slate-200'
                        : 'border-slate-200 hover:border-indigo-300'
                    }`}
                  >
                    <div className="space-y-3 text-xs">
                      <div className="flex items-center justify-between">
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800">
                          {sugg.category.replace(/_/g, ' ')}
                        </span>
                        {isAccepted && (
                          <span className="text-[11px] font-bold text-emerald-700 flex items-center gap-1">
                            <CheckCircle2 className="w-3.5 h-3.5" /> Accepted
                          </span>
                        )}
                        {isRejected && (
                          <span className="text-[11px] font-bold text-slate-500">Rejected</span>
                        )}
                      </div>

                      {/* Issue */}
                      <div>
                        <span className="font-bold text-slate-700">Identified Issue:</span>
                        <p className="text-slate-600 mt-0.5">{sugg.issue}</p>
                      </div>

                      {/* Original vs Suggested */}
                      <div className="space-y-2 bg-slate-50 p-2.5 rounded-md border border-slate-100">
                        <div>
                          <span className="text-[11px] font-bold text-rose-700 block">Original Text:</span>
                          <p className="text-slate-600 italic">"{sugg.originalText}"</p>
                        </div>
                        <div>
                          <span className="text-[11px] font-bold text-emerald-700 block">
                            Suggested Revision:
                          </span>
                          <p className="text-slate-800 font-medium">"{sugg.suggestedText}"</p>
                        </div>
                      </div>

                      {/* Expected Benefit */}
                      <div className="text-[11px] text-slate-500">
                        <strong>Expected Benefit:</strong> {sugg.expectedBenefit}
                      </div>

                      {/* Actions */}
                      {!isAccepted && !isRejected && (
                        <div className="flex items-center gap-2 pt-1 border-t border-slate-100">
                          <Button
                            onClick={() => handleAcceptSuggestion(sugg)}
                            className="w-full justify-center text-xs py-1.5 flex items-center gap-1 bg-emerald-600 hover:bg-emerald-700"
                          >
                            <Check className="w-3.5 h-3.5" />
                            Apply Revision
                          </Button>
                          <Button
                            variant="outline"
                            onClick={() => handleRejectSuggestion(sugg.id)}
                            className="text-xs py-1.5 text-slate-500"
                          >
                            Dismiss
                          </Button>
                        </div>
                      )}
                    </div>
                  </Card>
                );
              })
            )}
          </div>
        </div>
      </div>

      <TailoredResumeViewer
        isOpen={showPreview}
        onClose={() => setShowPreview(false)}
        versionName={versionName}
        versionNumber={version?.versionNumber}
        content={content}
        rawText={rawText}
        acceptedSuggestions={suggestions}
      />
    </div>
  );
}
