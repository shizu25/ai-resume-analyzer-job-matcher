import api from './api';

export const jobService = {
  listJobs: async (filters = {}) => {
    const params = {};
    if (filters.q && filters.q.trim()) params.q = filters.q.trim();
    if (filters.skill && filters.skill.trim()) params.skill = filters.skill.trim();
    if (filters.location && filters.location.trim()) params.location = filters.location.trim();
    if (filters.workMode) params.workMode = filters.workMode;
    if (filters.jobType) params.jobType = filters.jobType;
    if (filters.experienceLevel) params.experienceLevel = filters.experienceLevel;

    const res = await api.get('/jobs', { params });
    return res.data.data;
  },

  getJob: async (id) => {
    const res = await api.get(`/jobs/${id}`);
    return res.data.data;
  },

  createJob: async (payload) => {
    const res = await api.post('/jobs', payload);
    return res.data.data;
  },

  updateJob: async (id, payload) => {
    const res = await api.put(`/jobs/${id}`, payload);
    return res.data.data;
  },

  deactivateJob: async (id) => {
    const res = await api.delete(`/jobs/${id}`);
    return res.data;
  },

  matchJob: async (jobId, resumeId) => {
    const params = resumeId ? { resumeId } : undefined;
    const res = await api.post(`/jobs/${jobId}/match`, null, { params });
    return res.data.data;
  },

  getJobMatch: async (jobId, resumeId) => {
    const params = resumeId ? { resumeId } : undefined;
    const res = await api.get(`/jobs/${jobId}/match`, { params });
    return res.data.data;
  },

  runAllMatches: async (resumeId) => {
    const params = resumeId ? { resumeId } : undefined;
    const res = await api.post('/matches/run', null, { params });
    return res.data.data;
  },

  listMatches: async (resumeId) => {
    const params = resumeId ? { resumeId } : undefined;
    const res = await api.get('/matches', { params });
    return res.data.data;
  },
};

export default jobService;
