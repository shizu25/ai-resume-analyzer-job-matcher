import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import FileUploader from '../../components/common/FileUploader';
import EmptyState from '../../components/common/EmptyState';
import LoadingSkeleton from '../../components/common/LoadingSkeleton';
import ErrorMessage from '../../components/common/ErrorMessage';
import ToastNotification from '../../components/common/ToastNotification';
import {
  FileText,
  Upload,
  Download,
  Trash2,
  Sparkles,
  CheckCircle,
  Eye,
  AlertCircle,
  Clock,
  Star
} from 'lucide-react';

export default function ResumeList() {
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [analyzingId, setAnalyzingId] = useState(null);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);
  const [deleteModalResume, setDeleteModalResume] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      setLoading(true);
      const res = await api.get('/resumes');
      setResumes(res.data?.data || []);
    } catch (err) {
      setError('Failed to fetch resumes. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    setUploading(true);
    setError(null);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      const res = await api.post('/resumes/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      setUploadModalOpen(false);
      setSelectedFile(null);
      setToast({ message: 'Resume uploaded successfully!', type: 'success' });
      fetchResumes();

      // Automatically offer or trigger analysis for smooth UX
      const newResume = res.data.data;
      if (newResume?.id) {
        handleAnalyze(newResume.id);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload resume. Please check file format and size.');
    } finally {
      setUploading(false);
    }
  };

  const handleAnalyze = async (resumeId) => {
    setAnalyzingId(resumeId);
    setError(null);
    try {
      await api.post(`/resumes/${resumeId}/analyze`);
      setToast({ message: 'Resume analyzed successfully!', type: 'success' });
      fetchResumes();
      navigate(`/resumes/${resumeId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Analysis failed. Please verify AI service status.');
      fetchResumes();
    } finally {
      setAnalyzingId(null);
    }
  };

  const handleSetActive = async (resumeId) => {
    try {
      await api.patch(`/resumes/${resumeId}/active`);
      setToast({ message: 'Default active resume updated.', type: 'success' });
      fetchResumes();
    } catch (err) {
      setError('Failed to set resume as active.');
    }
  };

  const handleDownload = async (resume) => {
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

  const confirmDelete = async () => {
    if (!deleteModalResume) return;
    try {
      await api.delete(`/resumes/${deleteModalResume.id}`);
      setToast({ message: 'Resume deleted successfully.', type: 'success' });
      setDeleteModalResume(null);
      fetchResumes();
    } catch (err) {
      setError('Failed to delete resume.');
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
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
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Resume Management</h1>
          <p className="text-sm text-slate-500 mt-1">
            Upload, inspect, and analyze your resumes with AI extraction
          </p>
        </div>
        <Button
          onClick={() => { setSelectedFile(null); setUploadModalOpen(true); }}
          variant="primary"
          className="self-start sm:self-auto shadow-sm"
        >
          <Upload className="w-4 h-4 mr-2" /> Upload Resume
        </Button>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError(null)} />

      {/* Resumes Content */}
      {loading ? (
        <div className="space-y-4">
          <LoadingSkeleton count={3} variant="card" />
        </div>
      ) : resumes.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="No resumes uploaded yet"
          description="Upload your PDF or DOCX resume to extract skills, work history, and get AI-powered improvement suggestions."
          actionLabel="Upload Your First Resume"
          onAction={() => setUploadModalOpen(true)}
        />
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {resumes.map((resume) => {
            const isAnalyzingThis = analyzingId === resume.id;
            const isCompleted = resume.parsingStatus === 'COMPLETED';

            return (
              <Card
                key={resume.id}
                className="hover:border-slate-300 transition-shadow hover:shadow-md"
              >
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0 border border-indigo-100">
                      <FileText className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <h3 className="text-base font-semibold text-slate-900">{resume.fileName}</h3>
                        {resume.isActive && (
                          <Badge variant="success" size="sm">
                            <Star className="w-3 h-3 fill-emerald-600" /> Active Resume
                          </Badge>
                        )}
                        <Badge variant={resume.parsingStatus} size="sm">
                          {resume.parsingStatus}
                        </Badge>
                      </div>

                      <div className="flex items-center gap-4 text-xs text-slate-500 mt-1.5 flex-wrap">
                        <span>{formatFileSize(resume.fileSize)}</span>
                        <span>•</span>
                        <span>Uploaded {new Date(resume.createdAt).toLocaleDateString()}</span>
                        {resume.candidateName && (
                          <>
                            <span>•</span>
                            <span className="font-medium text-slate-700">Candidate: {resume.candidateName}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 flex-wrap self-end md:self-auto">
                    {isCompleted ? (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => navigate(`/resumes/${resume.id}`)}
                      >
                        <Eye className="w-3.5 h-3.5 mr-1.5" /> View Analysis
                      </Button>
                    ) : (
                      <Button
                        variant="primary"
                        size="sm"
                        loading={isAnalyzingThis}
                        onClick={() => handleAnalyze(resume.id)}
                      >
                        <Sparkles className="w-3.5 h-3.5 mr-1.5" /> Analyze with AI
                      </Button>
                    )}

                    {!resume.isActive && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleSetActive(resume.id)}
                        title="Set as active resume for job matching"
                      >
                        Set Active
                      </Button>
                    )}

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDownload(resume)}
                      title="Download file"
                    >
                      <Download className="w-4 h-4 text-slate-600" />
                    </Button>

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setDeleteModalResume(resume)}
                      className="text-rose-500 hover:text-rose-700 hover:bg-rose-50"
                      title="Delete resume"
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

      {/* Upload Modal */}
      <Modal
        isOpen={uploadModalOpen}
        onClose={() => { if (!uploading) setUploadModalOpen(false); }}
        title="Upload Resume Document"
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setUploadModalOpen(false)}
              disabled={uploading}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleUpload}
              disabled={!selectedFile}
              loading={uploading}
            >
              Upload & Process
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <p className="text-sm text-slate-600">
            Select a PDF or DOCX resume document. Our AI service will parse sections, extract multi-category skills, education, and career achievements.
          </p>
          <FileUploader
            onFileSelect={(file) => setSelectedFile(file)}
            isUploading={uploading}
          />
        </div>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!deleteModalResume}
        onClose={() => setDeleteModalResume(null)}
        title="Delete Resume"
        footer={
          <>
            <Button variant="outline" onClick={() => setDeleteModalResume(null)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={confirmDelete}>
              Delete Permanently
            </Button>
          </>
        }
      >
        <p className="text-sm text-slate-600">
          Are you sure you want to delete <span className="font-semibold text-slate-900">{deleteModalResume?.fileName}</span>?
          This action will permanently remove the stored file and all associated AI-extracted skills and suggestions.
        </p>
      </Modal>
    </div>
  );
}
