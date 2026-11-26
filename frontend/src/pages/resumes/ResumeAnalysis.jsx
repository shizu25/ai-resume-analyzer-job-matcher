import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ErrorMessage from '../../components/common/ErrorMessage';
import ToastNotification from '../../components/common/ToastNotification';
import {
  ArrowLeft,
  Sparkles,
  Download,
  Mail,
  Phone,
  User,
  GraduationCap,
  Briefcase,
  Code,
  FolderGit2,
  AlertTriangle,
  FileText,
  ChevronDown,
  ChevronUp,
  ExternalLink,
  Calendar,
  CheckCircle2,
  Lightbulb,
  Compass,
  Layers,
} from 'lucide-react';

export default function ResumeAnalysis() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [resume, setResume] = useState(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [showRawText, setShowRawText] = useState(false);
  const [activeTab, setActiveTab] = useState('suggestions'); // suggestions, skills, experience, education, projects

  useEffect(() => {
    fetchResumeDetails();
  }, [id]);

  const fetchResumeDetails = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/resumes/${id}`);
      setResume(res.data?.data);
    } catch (err) {
      setError('Failed to load resume details. Please ensure you have permission.');
    } finally {
      setLoading(false);
    }
  };

  const handleReanalyze = async () => {
    setAnalyzing(true);
    setError(null);
    try {
      const res = await api.post(`/resumes/${id}/analyze`);
      setResume(res.data?.data);
      setToast({ message: 'Resume re-analyzed successfully!', type: 'success' });
    } catch (err) {
      setError(err.response?.data?.message || 'Re-analysis failed.');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleDownload = async () => {
    if (!resume) return;
    try {
      const response = await api.get(`/resumes/${resume.id}/download`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', resume.fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError('Failed to download resume file.');
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <Link to="/resumes" className="text-slate-500 hover:text-slate-800">
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Loading Resume Analysis...</h1>
          </div>
        </div>
        <LoadingSkeleton count={3} variant="card" />
      </div>
    );
  }

  if (!resume) {
    return (
      <div className="text-center py-12">
        <h2 className="text-lg font-semibold text-slate-800">Resume not found</h2>
        <p className="text-sm text-slate-500 mt-1">The requested document does not exist or has been removed.</p>
        <Link to="/resumes" className="mt-4 inline-block text-sm font-medium text-indigo-600 hover:underline">
          Return to Resumes
        </Link>
      </div>
    );
  }

  // Group skills by category
  const skillsByCategory = (resume.skills || []).reduce((acc, skill) => {
    const cat = skill.category || 'Other';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(skill.name);
    return acc;
  }, {});

  const suggestions = resume.suggestions || [];
  const highPriority = suggestions.filter((s) => s.priority === 'high');
  const mediumPriority = suggestions.filter((s) => s.priority === 'medium');
  const lowPriority = suggestions.filter((s) => s.priority === 'low');

  return (
    <div className="space-y-6">
      {toast && (
        <ToastNotification
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Top Navigation */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link
            to="/resumes"
            className="p-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 transition-colors"
            title="Back to Resumes"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900">{resume.fileName}</h1>
              <Badge variant={resume.parsingStatus} size="sm">
                {resume.parsingStatus}
              </Badge>
              {resume.isActive && (
                <Badge variant="success" size="sm">Active Resume</Badge>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              Parsed on {new Date(resume.updatedAt || resume.createdAt).toLocaleString()}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Link to={`/roadmaps?resumeId=${resume.id}`}>
            <Button variant="outline" size="sm" className="text-indigo-600 border-indigo-200">
              <Compass className="w-4 h-4 mr-1.5" /> Skill Roadmap
            </Button>
          </Link>
          <Link to={`/resume-versions?resumeId=${resume.id}`}>
            <Button variant="outline" size="sm" className="text-indigo-600 border-indigo-200">
              <Layers className="w-4 h-4 mr-1.5" /> Tailor Version
            </Button>
          </Link>
          <Button variant="outline" size="sm" onClick={handleDownload}>
            <Download className="w-4 h-4 mr-1.5" /> Download Document
          </Button>
          <Button
            variant="primary"
            size="sm"
            loading={analyzing}
            onClick={handleReanalyze}
          >
            <Sparkles className="w-4 h-4 mr-1.5" /> Re-Analyze with AI
          </Button>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* Parsing Warnings Alert */}
      {resume.warnings && resume.warnings.length > 0 && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl text-amber-900 text-sm space-y-1">
          <div className="flex items-center gap-2 font-semibold text-amber-800">
            <AlertTriangle className="w-4 h-4 text-amber-600" />
            Extraction Warnings
          </div>
          <ul className="list-disc list-inside text-xs text-amber-700 pl-1 space-y-0.5">
            {resume.warnings.map((w, idx) => (
              <li key={idx}>{w}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Overview Card */}
      <Card className="bg-gradient-to-br from-white to-slate-50/50">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="md:col-span-3 space-y-3">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-indigo-100 text-indigo-700 font-bold text-lg flex items-center justify-center border border-indigo-200">
                {resume.candidateName ? resume.candidateName.charAt(0) : 'U'}
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900">
                  {resume.candidateName || 'Candidate Name Not Detected'}
                </h2>
                <div className="flex items-center gap-4 text-xs text-slate-600 mt-1 flex-wrap">
                  {resume.candidateEmail && (
                    <span className="flex items-center gap-1 font-mono">
                      <Mail className="w-3.5 h-3.5 text-slate-400" /> {resume.candidateEmail}
                    </span>
                  )}
                  {resume.candidatePhone && (
                    <span className="flex items-center gap-1 font-mono">
                      <Phone className="w-3.5 h-3.5 text-slate-400" /> {resume.candidatePhone}
                    </span>
                  )}
                </div>
              </div>
            </div>

            {resume.summary && (
              <div className="pt-2">
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
                  Professional Summary
                </p>
                <p className="text-sm text-slate-700 leading-relaxed bg-white/80 p-3 rounded-lg border border-slate-200/60">
                  {resume.summary}
                </p>
              </div>
            )}
          </div>

          <div className="border-t md:border-t-0 md:border-l border-slate-200 md:pl-6 flex flex-col justify-center space-y-2.5">
            <div className="text-xs text-slate-500 font-medium">Extracted Highlights</div>
            <div className="grid grid-cols-2 gap-2 text-center">
              <div className="p-2.5 bg-white rounded-lg border border-slate-200/80 shadow-xs">
                <p className="text-lg font-bold text-indigo-600">{resume.skills?.length || 0}</p>
                <p className="text-[11px] text-slate-500 font-medium">Skills Found</p>
              </div>
              <div className="p-2.5 bg-white rounded-lg border border-slate-200/80 shadow-xs">
                <p className="text-lg font-bold text-emerald-600">{resume.experiences?.length || 0}</p>
                <p className="text-[11px] text-slate-500 font-medium">Experience</p>
              </div>
              <div className="p-2.5 bg-white rounded-lg border border-slate-200/80 shadow-xs">
                <p className="text-lg font-bold text-purple-600">{resume.projects?.length || 0}</p>
                <p className="text-[11px] text-slate-500 font-medium">Projects</p>
              </div>
              <div className="p-2.5 bg-white rounded-lg border border-slate-200/80 shadow-xs">
                <p className="text-lg font-bold text-amber-600">{suggestions.length}</p>
                <p className="text-[11px] text-slate-500 font-medium">Suggestions</p>
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Tabs */}
      <div className="border-b border-slate-200 flex gap-2 overflow-x-auto pb-0.5">
        {[
          { key: 'suggestions', label: 'AI Suggestions', count: suggestions.length, icon: Lightbulb },
          { key: 'skills', label: 'Extracted Skills', count: resume.skills?.length || 0, icon: Code },
          { key: 'experience', label: 'Work Experience', count: resume.experiences?.length || 0, icon: Briefcase },
          { key: 'education', label: 'Education', count: resume.education?.length || 0, icon: GraduationCap },
          { key: 'projects', label: 'Projects', count: resume.projects?.length || 0, icon: FolderGit2 },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex items-center gap-2 px-4 py-2.5 text-sm font-semibold border-b-2 whitespace-nowrap transition-colors ${
              activeTab === tab.key
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            <span>{tab.label}</span>
            <span
              className={`text-xs px-1.5 py-0.2 rounded-full font-mono ${
                activeTab === tab.key
                  ? 'bg-indigo-100 text-indigo-700'
                  : 'bg-slate-100 text-slate-600'
              }`}
            >
              {tab.count}
            </span>
          </button>
        ))}
      </div>

      {/* Tab Panels */}

      {/* 1. AI SUGGESTIONS */}
      {activeTab === 'suggestions' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-semibold text-slate-900">
              Resume Enhancement Recommendations
            </h3>
            <span className="text-xs text-slate-500">
              Powered by Rule Engine & NLP Analysis
            </span>
          </div>

          {suggestions.length === 0 ? (
            <Card className="text-center py-8">
              <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
              <p className="font-semibold text-slate-800">Excellent Resume Structure!</p>
              <p className="text-xs text-slate-500 mt-1">No critical issues or missing sections were detected.</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {suggestions.map((sug, i) => (
                <div
                  key={sug.id || i}
                  className={`p-4 rounded-xl border bg-white shadow-xs transition-shadow hover:shadow-sm ${
                    sug.priority === 'high'
                      ? 'border-rose-200/80 bg-rose-50/20'
                      : sug.priority === 'medium'
                      ? 'border-amber-200/80 bg-amber-50/20'
                      : 'border-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2 mb-1.5">
                    <div className="flex items-center gap-2">
                      <Badge variant={sug.priority} size="sm">
                        {sug.priority.toUpperCase()} PRIORITY
                      </Badge>
                      <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                        {sug.category}
                      </span>
                    </div>
                  </div>
                  <h4 className="text-sm font-semibold text-slate-900 mb-1">{sug.issue}</h4>
                  <p className="text-xs text-slate-600 leading-relaxed bg-white/80 p-2.5 rounded-lg border border-slate-200/50">
                    <span className="font-semibold text-indigo-700">Recommendation: </span>
                    {sug.recommendation}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* 2. SKILLS */}
      {activeTab === 'skills' && (
        <div className="space-y-5">
          {Object.keys(skillsByCategory).length === 0 ? (
            <Card className="text-center py-8">
              <Code className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-sm text-slate-600">No skills recognized in this resume.</p>
            </Card>
          ) : (
            Object.entries(skillsByCategory).map(([category, items]) => (
              <Card key={category} title={category} subtitle={`${items.length} skills detected`}>
                <div className="flex flex-wrap gap-2">
                  {items.map((skill, idx) => (
                    <Badge key={idx} variant="primary" size="md">
                      {skill}
                    </Badge>
                  ))}
                </div>
              </Card>
            ))
          )}
        </div>
      )}

      {/* 3. EXPERIENCE */}
      {activeTab === 'experience' && (
        <div className="space-y-4">
          {!resume.experiences || resume.experiences.length === 0 ? (
            <Card className="text-center py-8">
              <Briefcase className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-sm text-slate-600">No work experience entries extracted.</p>
            </Card>
          ) : (
            resume.experiences.map((exp, idx) => (
              <Card key={exp.id || idx}>
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
                  <div>
                    <h4 className="text-base font-bold text-slate-900">{exp.position}</h4>
                    <p className="text-sm font-medium text-indigo-600">{exp.company}</p>
                    {exp.location && (
                      <p className="text-xs text-slate-500 mt-0.5">{exp.location}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-2 self-start">
                    <span className="inline-flex items-center gap-1 text-xs text-slate-500 font-mono">
                      <Calendar className="w-3.5 h-3.5" />
                      {exp.startDate || 'N/A'} – {exp.endDate || (exp.isCurrent ? 'Present' : 'N/A')}
                    </span>
                    {exp.isCurrent && (
                      <Badge variant="success" size="sm">Current</Badge>
                    )}
                  </div>
                </div>

                {exp.description && (
                  <div className="mt-3.5 pt-3 border-t border-slate-100 text-xs text-slate-700 whitespace-pre-line leading-relaxed">
                    {exp.description}
                  </div>
                )}
              </Card>
            ))
          )}
        </div>
      )}

      {/* 4. EDUCATION */}
      {activeTab === 'education' && (
        <div className="space-y-4">
          {!resume.education || resume.education.length === 0 ? (
            <Card className="text-center py-8">
              <GraduationCap className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-sm text-slate-600">No education entries extracted.</p>
            </Card>
          ) : (
            resume.education.map((edu, idx) => (
              <Card key={edu.id || idx}>
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
                  <div>
                    <h4 className="text-base font-bold text-slate-900">{edu.degree}</h4>
                    <p className="text-sm font-medium text-indigo-600">{edu.institution}</p>
                    {edu.fieldOfStudy && (
                      <p className="text-xs text-slate-600 mt-0.5">Field: {edu.fieldOfStudy}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-3">
                    {(edu.startYear || edu.endYear) && (
                      <span className="text-xs font-mono text-slate-500">
                        {edu.startYear ? `${edu.startYear} – ` : ''}{edu.endYear || 'Present'}
                      </span>
                    )}
                    {edu.grade && (
                      <Badge variant="secondary" size="sm">{edu.grade}</Badge>
                    )}
                  </div>
                </div>
              </Card>
            ))
          )}
        </div>
      )}

      {/* 5. PROJECTS */}
      {activeTab === 'projects' && (
        <div className="space-y-4">
          {!resume.projects || resume.projects.length === 0 ? (
            <Card className="text-center py-8">
              <FolderGit2 className="w-8 h-8 text-slate-400 mx-auto mb-2" />
              <p className="text-sm text-slate-600">No technical projects extracted.</p>
            </Card>
          ) : (
            resume.projects.map((proj, idx) => (
              <Card key={proj.id || idx}>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h4 className="text-base font-bold text-slate-900">{proj.title}</h4>
                    {proj.technologies && (
                      <div className="flex items-center gap-1.5 flex-wrap mt-1">
                        <span className="text-[11px] font-semibold text-slate-500 uppercase">Tech:</span>
                        <span className="text-xs text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded font-medium">
                          {proj.technologies}
                        </span>
                      </div>
                    )}
                  </div>
                  {proj.link && (
                    <a
                      href={proj.link.startsWith('http') ? proj.link : `https://${proj.link}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 text-xs font-semibold text-indigo-600 hover:text-indigo-800 transition-colors"
                    >
                      <ExternalLink className="w-3.5 h-3.5" /> View Project
                    </a>
                  )}
                </div>

                {proj.description && (
                  <p className="mt-3 text-xs text-slate-700 leading-relaxed whitespace-pre-line border-t border-slate-100 pt-2.5">
                    {proj.description}
                  </p>
                )}
              </Card>
            ))
          )}
        </div>
      )}

      {/* Raw Extracted Text Collapsible Viewer */}
      <div className="pt-4 border-t border-slate-200">
        <button
          onClick={() => setShowRawText(!showRawText)}
          className="flex items-center justify-between w-full p-4 bg-white rounded-xl border border-slate-200/80 hover:bg-slate-50 transition-colors text-left"
        >
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-700">
            <FileText className="w-4 h-4 text-slate-500" />
            <span>Raw Parsed Text Inspection</span>
          </div>
          {showRawText ? <ChevronUp className="w-4 h-4 text-slate-400" /> : <ChevronDown className="w-4 h-4 text-slate-400" />}
        </button>

        {showRawText && (
          <div className="mt-2 p-4 bg-slate-900 text-slate-200 rounded-xl font-mono text-xs overflow-x-auto max-h-96 whitespace-pre-wrap leading-relaxed">
            {resume.parsedText || 'No raw text available.'}
          </div>
        )}
      </div>
    </div>
  );
}
