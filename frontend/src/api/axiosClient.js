import axios from 'axios';
import { auth } from '../auth/firebase';
import { getIdToken } from 'firebase/auth';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use(async (config) => {
  if (auth.currentUser) {
    try {
      const token = await getIdToken(auth.currentUser, false);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (error) {
      console.error("Error getting Firebase token", error);
    }
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      import('react-hot-toast').then(({ toast }) => {
        toast.error('Session expired. Please log in again.');
      });
      window.location.href = '/login';
    } else if (error.response && error.response.data && error.response.data.error) {
      import('react-hot-toast').then(({ toast }) => {
        toast.error(error.response.data.error);
      });
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
