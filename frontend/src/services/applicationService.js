import api from './api';

export const applicationService = {
  applyToJob: async (payload) => {
    const res = await api.post('/applications', payload);
    return res.data.data;
  },

  listApplications: async (status) => {
    const params = status ? { status } : undefined;
    const res = await api.get('/applications', { params });
    return res.data.data;
  },

  getApplication: async (id) => {
    const res = await api.get(`/applications/${id}`);
    return res.data.data;
  },

  updateApplication: async (id, payload) => {
    const res = await api.patch(`/applications/${id}`, payload);
    return res.data.data;
  },

  deleteApplication: async (id) => {
    const res = await api.delete(`/applications/${id}`);
    return res.data;
  },
};

export default applicationService;
