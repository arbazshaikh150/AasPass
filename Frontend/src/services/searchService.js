import apiClient from './apiClient';

export async function searchEvents(query) {
  const response = await apiClient.get('/search', {
    params: { query },
  });

  return response.data;
}
