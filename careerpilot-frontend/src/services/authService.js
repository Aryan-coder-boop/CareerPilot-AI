import apiClient from './apiClient';

export const authService = {
  async register({ fullName, email, password }) {
    const response = await apiClient.post('/auth/register', {
      fullName,
      email,
      password,
    });
    return response.data;
  },

  async login({ email, password }) {
    const response = await apiClient.post('/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  async getCurrentUser() {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },

  logout() {
    localStorage.removeItem('cp_access_token');
    localStorage.removeItem('cp_refresh_token');
    localStorage.removeItem('cp_user');
    window.dispatchEvent(new Event('auth-logout'));
  },
};

