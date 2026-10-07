import axios from 'axios';
import { API_BASE_URL } from '../constants/apiEndpoints';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// We can add interceptors here later for auth or global error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Standardized error logging or toast triggers could go here
    return Promise.reject(error);
  }
);
