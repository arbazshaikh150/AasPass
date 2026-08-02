import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { loadNearbyFeed } from '../../services/feedService';

export const fetchNearbyFeed = createAsyncThunk('feed/fetchNearbyFeed', async (location) => {
  const events = await loadNearbyFeed(location);
  return { events, location };
});

const initialState = {
  status: 'idle',
  events: [],
  location: null,
  error: '',
};

const feedSlice = createSlice({
  name: 'feed',
  initialState,
  reducers: {
    clearFeedError(state) {
      state.error = '';
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchNearbyFeed.pending, (state) => {
        state.status = 'loading';
        state.error = '';
      })
      .addCase(fetchNearbyFeed.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.events = action.payload.events;
        state.location = action.payload.location;
      })
      .addCase(fetchNearbyFeed.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Unable to load nearby events.';
      });
  },
});

export const { clearFeedError } = feedSlice.actions;
export default feedSlice.reducer;
