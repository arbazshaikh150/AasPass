import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { searchEvents } from '../../services/searchService';

export const runEventSearch = createAsyncThunk('search/runEventSearch', async (query) => {
  const trimmedQuery = query.trim();
  const results = await searchEvents(trimmedQuery);

  return { query: trimmedQuery, results };
});

const initialState = {
  status: 'idle',
  query: '',
  results: [],
  error: '',
};

const searchSlice = createSlice({
  name: 'search',
  initialState,
  reducers: {
    clearSearch(state) {
      state.status = 'idle';
      state.query = '';
      state.results = [];
      state.error = '';
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(runEventSearch.pending, (state, action) => {
        state.status = 'loading';
        state.query = action.meta.arg.trim();
        state.error = '';
      })
      .addCase(runEventSearch.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.query = action.payload.query;
        state.results = action.payload.results;
      })
      .addCase(runEventSearch.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Unable to search events.';
      });
  },
});

export const { clearSearch } = searchSlice.actions;
export default searchSlice.reducer;
