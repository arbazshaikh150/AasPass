import apiClient from './apiClient';

export async function createEvent(payload) {
  const response = await apiClient.post('/api/events', payload);
  return response.data;
}

export async function getEventById(eventId) {
  const response = await apiClient.get(`/api/events/${eventId}`);
  return response.data;
}
