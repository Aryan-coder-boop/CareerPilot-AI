import apiClient from './apiClient';

export const interviewService = {
  startInterview: async (data) => {
    const response = await apiClient.post('/api/v1/interviews/start', data);
    return response.data;
  },

  getCurrentQuestion: async (sessionId) => {
    const response = await apiClient.get(`/api/v1/interviews/${sessionId}/current-question`);
    return response.data;
  },

  submitAnswer: async (sessionId, data) => {
    const response = await apiClient.post(`/api/v1/interviews/${sessionId}/submit-answer`, data);
    return response.data;
  },

  finishInterview: async (sessionId) => {
    const response = await apiClient.post(`/api/v1/interviews/${sessionId}/finish`);
    return response.data;
  },

  getSessions: async () => {
    const response = await apiClient.get('/api/v1/interviews');
    return response.data;
  },

  getSessionDetail: async (sessionId) => {
    const response = await apiClient.get(`/api/v1/interviews/${sessionId}`);
    return response.data;
  }
};

