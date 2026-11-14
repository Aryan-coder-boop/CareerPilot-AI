import apiClient from './apiClient';

export const roadmapService = {
  generateRoadmap: async (data) => {
    const response = await apiClient.post('/api/v1/roadmaps/generate', data);
    return response.data;
  },

  getRoadmaps: async () => {
    const response = await apiClient.get('/api/v1/roadmaps');
    return response.data;
  },

  getRoadmapDetail: async (id) => {
    const response = await apiClient.get(`/api/v1/roadmaps/${id}`);
    return response.data;
  },

  updateMilestoneStatus: async (milestoneId, status) => {
    const response = await apiClient.patch(`/api/v1/roadmaps/milestones/${milestoneId}`, { status });
    return response.data;
  }
};

