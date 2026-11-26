import api from './api';

export const roadmapService = {
  generateRoadmap: async (resumeId, jobId) => {
    const res = await api.post('/roadmaps/generate', { resumeId, jobId });
    return res.data.data;
  },

  regenerateRoadmap: async (resumeId, jobId) => {
    const res = await api.post('/roadmaps/regenerate', { resumeId, jobId });
    return res.data.data;
  },

  listRoadmaps: async () => {
    const res = await api.get('/roadmaps');
    return res.data.data;
  },

  getRoadmap: async (id) => {
    const res = await api.get(`/roadmaps/${id}`);
    return res.data.data;
  },

  updateProgress: async (roadmapId, itemId, progressStatus) => {
    const res = await api.patch(`/roadmaps/${roadmapId}/items/${itemId}/progress`, {
      progressStatus,
    });
    return res.data.data;
  },
};
