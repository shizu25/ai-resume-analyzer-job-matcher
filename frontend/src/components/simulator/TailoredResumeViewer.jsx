import React, { useState } from 'react';
import {
  Printer,
  Download,
  Copy,
  Check,
  Eye,
  Sparkles,
  FileText,
  Mail,
  Phone,
  Briefcase,
  GraduationCap,
  FolderGit2,
  Code,
  X,
} from 'lucide-react';
import Button from '../common/Button';

export default function TailoredResumeViewer({
  isOpen,
  onClose,
  versionName,
  versionNumber,
  content = {},
  rawText = '',
  acceptedSuggestions = [],
}) {
  const [highlightChanges, setHighlightChanges] = useState(true);
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const {
    candidate_name = 'Candidate Name',
    email = '',
    phone = '',
    summary = '',
    skills = [],
    experience = [],
    projects = [],
    education = [],
  } = content;

  // Find accepted revision phrases to highlight
  const acceptedPhrases = (acceptedSuggestions || [])
    .filter((s) => s.status === 'ACCEPTED')
    .map((s) => s.suggestedText)
    .filter(Boolean);

  const renderTextWithHighlights = (text) => {
    if (!text) return '';
    if (!highlightChanges || acceptedPhrases.length === 0) return text;

    let highlighted = text;
    for (const phrase of acceptedPhrases) {
      if (text.includes(phrase)) {
        return (
          <span>
            {text.split(phrase).map((part, i, arr) => (
              <React.Fragment key={i}>
                {part}
                {i < arr.length - 1 && (
                  <span className="bg-emerald-100 text-emerald-900 font-medium px-1 rounded border-b border-emerald-400">
                    {phrase}
                  </span>
                )}
              </React.Fragment>
            ))}
          </span>
        );
      }
    }
    return text;
  };

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadTxt = () => {
    let txt = `=================================================================\n`;
    txt += `${(candidate_name || 'CANDIDATE').toUpperCase()}\n`;
    txt += `${[email, phone].filter(Boolean).join(' | ')}\n`;
    txt += `=================================================================\n\n`;

    if (summary) {
      txt += `PROFESSIONAL SUMMARY\n--------------------\n${summary}\n\n`;
    }

    if (skills && skills.length > 0) {
      txt += `TECHNICAL SKILLS\n----------------\n${skills.join(', ')}\n\n`;
    }

    if (experience && experience.length > 0) {
      txt += `PROFESSIONAL EXPERIENCE\n-----------------------\n`;
      experience.forEach((exp) => {
        txt += `${exp.position || 'Position'} | ${exp.company || 'Company'} (${exp.start_date || ''} - ${exp.end_date || 'Present'})\n`;
        if (exp.location) txt += `Location: ${exp.location}\n`;
        if (exp.description) txt += `${exp.description}\n`;
        txt += `\n`;
      });
    }

    if (projects && projects.length > 0) {
      txt += `KEY PROJECTS\n------------\n`;
      projects.forEach((proj) => {
        txt += `${proj.title || 'Project'} | Technologies: ${proj.technologies || 'Various'}\n`;
        if (proj.description) txt += `${proj.description}\n`;
        txt += `\n`;
      });
    }

    if (education && education.length > 0) {
      txt += `EDUCATION\n---------\n`;
      education.forEach((edu) => {
        txt += `${edu.degree || 'Degree'} - ${edu.institution || 'University'}\n`;
        if (edu.field_of_study) txt += `Field: ${edu.field_of_study}\n`;
        txt += `\n`;
      });
    }

    const blob = new Blob([txt], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${(versionName || 'Tailored_Resume').replace(/\s+/g, '_')}.txt`;
    link.click();
    URL.revokeObjectURL(url);
  };

  const handleCopy = () => {
    let full = `${candidate_name}\n${[email, phone].filter(Boolean).join(' | ')}\n\nSUMMARY\n${summary}\n\nSKILLS\n${skills.join(', ')}\n\nEXPERIENCE\n`;
    experience.forEach((e) => {
      full += `${e.position} at ${e.company} (${e.start_date} - ${e.end_date})\n${e.description}\n\n`;
    });
    projects.forEach((p) => {
      full += `${p.title} (${p.technologies})\n${p.description}\n\n`;
    });
    navigator.clipboard.writeText(full);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex justify-center p-2 sm:p-4 md:p-6 print:p-0 print:bg-white">
      {/* Modal Container */}
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-4xl w-full flex flex-col max-h-[92vh] print:max-h-none print:border-none print:shadow-none animate-in fade-in zoom-in-95 duration-200">
        {/* Header (Hidden during print) */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between gap-3 shrink-0 print:hidden bg-slate-50/70 rounded-t-2xl">
          <div className="flex items-center gap-2">
            <span className="p-2 bg-indigo-100 text-indigo-700 rounded-lg">
              <FileText className="w-5 h-5" />
            </span>
            <div>
              <h2 className="font-bold text-slate-900 text-base flex items-center gap-2">
                Tailored ATS Resume Preview
                <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 font-semibold">
                  Version {versionNumber}: {versionName}
                </span>
              </h2>
              <p className="text-xs text-slate-500">
                Candidate-centric tailored document reflecting accepted suggestions and acquired skills.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setHighlightChanges(!highlightChanges)}
              className={`text-xs px-2.5 py-1.5 rounded-lg border font-medium flex items-center gap-1.5 transition-colors ${
                highlightChanges
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-300'
                  : 'bg-white text-slate-600 border-slate-200'
              }`}
              title="Toggle highlight on tailored revisions"
            >
              <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
              {highlightChanges ? 'Highlights: ON' : 'Highlights: OFF'}
            </button>

            <Button
              variant="outline"
              size="sm"
              onClick={handleCopy}
              className="text-xs flex items-center gap-1"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied' : 'Copy'}
            </Button>

            <Button
              variant="outline"
              size="sm"
              onClick={handleDownloadTxt}
              className="text-xs flex items-center gap-1"
            >
              <Download className="w-3.5 h-3.5" />
              .TXT
            </Button>

            <Button
              size="sm"
              onClick={handlePrint}
              className="text-xs flex items-center gap-1 bg-indigo-600 hover:bg-indigo-700"
            >
              <Printer className="w-3.5 h-3.5" />
              Print / Save PDF
            </Button>

            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 text-lg font-semibold ml-2"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Resume Paper Content (Scrollable in modal, formatted for standard printable page) */}
        <div className="overflow-y-auto p-6 sm:p-10 font-sans print:overflow-visible print:p-0">
          <div className="max-w-3xl mx-auto bg-white p-8 sm:p-12 border border-slate-200/80 shadow-xs rounded-xl print:border-none print:shadow-none print:p-0 space-y-6">
            {/* Header: Name & Contact */}
            <div className="border-b-2 border-slate-900 pb-4 text-center sm:text-left flex flex-col sm:flex-row sm:items-end justify-between gap-3">
              <div>
                <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900 uppercase">
                  {candidate_name || 'Candidate Name'}
                </h1>
                <p className="text-xs text-indigo-700 font-semibold tracking-wide uppercase mt-1">
                  Tailored Professional Profile
                </p>
              </div>
              <div className="text-xs text-slate-600 space-y-0.5 sm:text-right">
                {email && (
                  <p className="flex items-center gap-1.5 sm:justify-end">
                    <Mail className="w-3.5 h-3.5 text-slate-400" /> {email}
                  </p>
                )}
                {phone && (
                  <p className="flex items-center gap-1.5 sm:justify-end">
                    <Phone className="w-3.5 h-3.5 text-slate-400" /> {phone}
                  </p>
                )}
              </div>
            </div>

            {/* Professional Summary */}
            {summary && (
              <div className="space-y-1.5">
                <h2 className="text-xs font-bold uppercase tracking-wider text-slate-900 border-b border-slate-200 pb-1 flex items-center gap-1.5">
                  <Briefcase className="w-3.5 h-3.5 text-indigo-600" /> Professional Summary
                </h2>
                <p className="text-xs text-slate-700 leading-relaxed pt-1">
                  {renderTextWithHighlights(summary)}
                </p>
              </div>
            )}

            {/* Technical Skills */}
            {skills && skills.length > 0 && (
              <div className="space-y-1.5">
                <h2 className="text-xs font-bold uppercase tracking-wider text-slate-900 border-b border-slate-200 pb-1 flex items-center gap-1.5">
                  <Code className="w-3.5 h-3.5 text-indigo-600" /> Technical Competencies & Skills
                </h2>
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {skills.map((skill, sIdx) => (
                    <span
                      key={sIdx}
                      className="px-2 py-0.5 text-xs bg-slate-100 text-slate-800 rounded font-medium border border-slate-200"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Professional Experience */}
            {experience && experience.length > 0 && (
              <div className="space-y-3">
                <h2 className="text-xs font-bold uppercase tracking-wider text-slate-900 border-b border-slate-200 pb-1 flex items-center gap-1.5">
                  <Briefcase className="w-3.5 h-3.5 text-indigo-600" /> Work Experience
                </h2>
                <div className="space-y-4 pt-1">
                  {experience.map((exp, eIdx) => (
                    <div key={eIdx} className="space-y-1">
                      <div className="flex justify-between items-baseline text-xs">
                        <span className="font-bold text-slate-900">
                          {exp.position} <span className="font-normal text-slate-600">• {exp.company}</span>
                        </span>
                        <span className="text-slate-500 font-medium">
                          {exp.start_date} - {exp.end_date || 'Present'}
                        </span>
                      </div>
                      {exp.location && (
                        <p className="text-[11px] text-slate-400">{exp.location}</p>
                      )}
                      <p className="text-xs text-slate-700 leading-relaxed pl-2 border-l-2 border-indigo-200 mt-1">
                        {renderTextWithHighlights(exp.description)}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Key Projects & Acquired Evidence */}
            {projects && projects.length > 0 && (
              <div className="space-y-3">
                <h2 className="text-xs font-bold uppercase tracking-wider text-slate-900 border-b border-slate-200 pb-1 flex items-center gap-1.5">
                  <FolderGit2 className="w-3.5 h-3.5 text-indigo-600" /> Key Projects & Practical Implementations
                </h2>
                <div className="space-y-3 pt-1">
                  {projects.map((proj, pIdx) => (
                    <div key={pIdx} className="space-y-1">
                      <div className="flex justify-between items-baseline text-xs">
                        <span className="font-bold text-slate-900">{proj.title}</span>
                        {proj.technologies && (
                          <span className="text-[11px] text-indigo-600 font-semibold">
                            {proj.technologies}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-700 leading-relaxed pl-2 border-l-2 border-indigo-200">
                        {renderTextWithHighlights(proj.description)}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Education */}
            {education && education.length > 0 && (
              <div className="space-y-2">
                <h2 className="text-xs font-bold uppercase tracking-wider text-slate-900 border-b border-slate-200 pb-1 flex items-center gap-1.5">
                  <GraduationCap className="w-3.5 h-3.5 text-indigo-600" /> Education
                </h2>
                <div className="space-y-2 pt-1">
                  {education.map((edu, edIdx) => (
                    <div key={edIdx} className="flex justify-between text-xs">
                      <div>
                        <span className="font-bold text-slate-900">{edu.degree}</span>
                        {edu.field_of_study && <span>, {edu.field_of_study}</span>}
                        <p className="text-slate-600">{edu.institution}</p>
                      </div>
                      <span className="text-slate-500 font-medium">
                        {edu.start_year ? `${edu.start_year} - ` : ''}{edu.end_year || ''}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
