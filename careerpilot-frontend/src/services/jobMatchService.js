import apiClient from './apiClient';

export const jobMatchService = {
  matchJob: async (jobData) => {
    const response = await apiClient.post('/api/v1/jobs/match', jobData);
    return response.data;
  },

  getMatches: async () => {
    const response = await apiClient.get('/api/v1/jobs/matches');
    return response.data;
  },

  getMatchDetail: async (id) => {
    const response = await apiClient.get(`/api/v1/jobs/matches/${id}`);
    return response.data;
  },

  getMatchesByResume: async (resumeId) => {
    const response = await apiClient.get(`/api/v1/jobs/matches/by-resume/${resumeId}`);
    return response.data;
  }
};

