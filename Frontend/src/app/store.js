import { configureStore } from '@reduxjs/toolkit';
import appReducer from '../features/app/appSlice';
import authReducer from '../features/auth/authSlice';
import eventReducer from '../features/events/eventSlice';
import feedReducer from '../features/feed/feedSlice';
import searchReducer from '../features/search/searchSlice';

export const store = configureStore({
  reducer: {
    app: appReducer,
    auth: authReducer,
    events: eventReducer,
    feed: feedReducer,
    search: searchReducer,
  },
});
