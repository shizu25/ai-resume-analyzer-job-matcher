import api from './api';

export const resumeService = {
  uploadResume: async (file, onUploadProgress) => {
    const formData = new FormData();
    formData.append('file', file);

    const res = await api.post('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onUploadProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onUploadProgress(percent);
        }
      },
    });
    return res.data.data;
  },

  listResumes: async () => {
    const res = await api.get('/resumes');
    return res.data.data;
  },

  getResume: async (id) => {
    const res = await api.get(`/resumes/${id}`);
    return res.data.data;
  },

  downloadResume: async (id, filename) => {
    const res = await api.get(`/resumes/${id}/download`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename || 'resume.pdf');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  setActiveResume: async (id) => {
    const res = await api.patch(`/resumes/${id}/active`);
    return res.data;
  },

  deleteResume: async (id) => {
    const res = await api.delete(`/resumes/${id}`);
    return res.data;
  },

  analyzeResume: async (id) => {
    const res = await api.post(`/resumes/${id}/analyze`);
    return res.data.data;
  },
};
