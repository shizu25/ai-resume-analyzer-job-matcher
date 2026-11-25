import api from './api';

export const authService = {
  register: async (email, password, fullName) => {
    const res = await api.post('/auth/register', { email, password, fullName });
    return res.data;
  },

  login: async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    return res.data;
  },

  refreshToken: async (refreshToken) => {
    const res = await api.post('/auth/refresh', { refreshToken });
    return res.data;
  },

  logout: async () => {
    const res = await api.post('/auth/logout');
    return res.data;
  },
};
