import apiClient from './apiClient';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export function startGoogleSignIn() {
  window.location.assign(`${API_BASE_URL}/oauth2/authorization/google`);
}

export async function checkAuthSession() {
  const response = await apiClient.get('/request/add');
  return response.data;
}

export async function getSecurityContext() {
  const response = await apiClient.get('/request');
  return response.data;
}
