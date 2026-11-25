import api from './api';

export const authService = {
  register: async (dataOrEmail, password, fullName) => {
    const payload = (typeof dataOrEmail === 'object' && dataOrEmail !== null)
      ? dataOrEmail
      : { email: dataOrEmail, password, fullName };
    const res = await api.post('/auth/register', payload);
    return res.data;
  },

  login: async (dataOrEmail, password) => {
    const payload = (typeof dataOrEmail === 'object' && dataOrEmail !== null)
      ? dataOrEmail
      : { email: dataOrEmail, password };
    const res = await api.post('/auth/login', payload);
    return res.data;
  },

  refreshToken: async (tokenOrObj) => {
    const refreshToken = (typeof tokenOrObj === 'object' && tokenOrObj !== null)
      ? tokenOrObj.refreshToken
      : tokenOrObj;
    const res = await api.post('/auth/refresh', { refreshToken });
    return res.data;
  },

  logout: async () => {
    const res = await api.post('/auth/logout');
    return res.data;
  },
};

export default authService;
