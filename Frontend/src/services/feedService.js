import apiClient from './apiClient';

export async function updateCurrentUserLocation(location) {
  await apiClient.put('/api/users/me/location', location);
}

export async function getCurrentUserFeed({ radiusKm = 5, limit = 50 } = {}) {
  const response = await apiClient.get('/api/users/me/feed', {
    params: { radiusKm, limit },
  });

  return response.data;
}

export async function loadNearbyFeed(location, options) {
  await updateCurrentUserLocation(location);
  const nearbyEvents = await getCurrentUserFeed(options);

  return Promise.all(
    nearbyEvents.map(async (nearbyEvent) => {
      const response = await apiClient.get(`/api/events/${nearbyEvent.id}`);
      return {
        ...response.data,
        distanceKm: nearbyEvent.distanceKm,
        score: nearbyEvent.score,
      };
    }),
  );
}
