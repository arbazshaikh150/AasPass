import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { checkAuthSession } from '../../services/authService';

export const verifySession = createAsyncThunk('auth/verifySession', async () => {
  const message = await checkAuthSession();
  return { message };
});

const initialState = {
  status: 'idle',
  isAuthenticated: false,
  message: '',
  error: '',
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearAuthError(state) {
      state.error = '';
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(verifySession.pending, (state) => {
        state.status = 'loading';
        state.error = '';
      })
      .addCase(verifySession.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.isAuthenticated = true;
        state.message = action.payload.message;
      })
      .addCase(verifySession.rejected, (state) => {
        state.status = 'failed';
        state.isAuthenticated = false;
        state.error = 'You are not signed in yet.';
      });
  },
});

export const { clearAuthError } = authSlice.actions;
export default authSlice.reducer;
