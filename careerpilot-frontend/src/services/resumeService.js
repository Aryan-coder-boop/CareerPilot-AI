import apiClient from './apiClient';

export const resumeService = {
  async uploadResume(file, onProgress) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });

    return response.data;
  },

  async getResumes() {
    const response = await apiClient.get('/resumes');
    return response.data;
  },

  async getResumeDetail(id) {
    const response = await apiClient.get(`/resumes/${id}`);
    return response.data;
  },

  async deleteResume(id) {
    const response = await apiClient.delete(`/resumes/${id}`);
    return response.data;
  },
};

