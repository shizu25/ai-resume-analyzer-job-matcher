import api from './api';

export const profileService = {
  getProfile: async () => {
    const res = await api.get('/users/me');
    return res.data.data;
  },

  updateProfile: async (profileData) => {
    const res = await api.put('/users/me', profileData);
    return res.data.data;
  },
};
