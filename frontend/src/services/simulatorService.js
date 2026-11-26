import api from './api';

export const simulatorService = {
  createVersion: async (resumeId, payload) => {
    const res = await api.post(`/resumes/${resumeId}/versions`, payload);
    return res.data.data;
  },

  listVersions: async (resumeId) => {
    const res = await api.get(`/resumes/${resumeId}/versions`);
    return res.data.data;
  },

  listAllUserVersions: async () => {
    const res = await api.get('/resume-versions');
    return res.data.data;
  },

  getVersion: async (versionId) => {
    const res = await api.get(`/resume-versions/${versionId}`);
    return res.data.data;
  },

  updateVersion: async (versionId, payload) => {
    const res = await api.put(`/resume-versions/${versionId}`, payload);
    return res.data.data;
  },

  generateSuggestions: async (versionId, jobId) => {
    const res = await api.post(`/resume-versions/${versionId}/suggestions`, { jobId });
    return res.data.data;
  },

  getSuggestions: async (versionId) => {
    const res = await api.get(`/resume-versions/${versionId}/suggestions`);
    return res.data.data;
  },

  updateSuggestionStatus: async (suggestionId, status) => {
    const res = await api.patch(`/resume-suggestions/${suggestionId}/status`, { status });
    return res.data.data;
  },

  runSimulation: async (baseVersionId, improvedVersionId, jobId) => {
    const res = await api.post('/resume-simulations', {
      baseVersionId,
      improvedVersionId,
      jobId,
    });
    return res.data.data;
  },

  listSimulations: async () => {
    const res = await api.get('/resume-simulations');
    return res.data.data;
  },

  getSimulation: async (id) => {
    const res = await api.get(`/resume-simulations/${id}`);
    return res.data.data;
  },

  integrateSkill: async (versionId, { skillName, practicalTasks, suggestedProject }) => {
    const res = await api.post(`/resume-versions/${versionId}/integrate-skill`, {
      skillName,
      practicalTasks,
      suggestedProject,
    });
    return res.data.data;
  },
};
