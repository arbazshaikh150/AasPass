import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { createEvent } from '../../services/eventService';

export const createNewEvent = createAsyncThunk('events/createNewEvent', async (payload) => {
  return createEvent(payload);
});

const initialState = {
  createStatus: 'idle',
  createdEvent: null,
  error: '',
};

const eventSlice = createSlice({
  name: 'events',
  initialState,
  reducers: {
    resetCreateEvent(state) {
      state.createStatus = 'idle';
      state.createdEvent = null;
      state.error = '';
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(createNewEvent.pending, (state) => {
        state.createStatus = 'loading';
        state.error = '';
      })
      .addCase(createNewEvent.fulfilled, (state, action) => {
        state.createStatus = 'succeeded';
        state.createdEvent = action.payload;
      })
      .addCase(createNewEvent.rejected, (state, action) => {
        state.createStatus = 'failed';
        state.error = action.error.message || 'Unable to create event.';
      });
  },
});

export const { resetCreateEvent } = eventSlice.actions;
export default eventSlice.reducer;
