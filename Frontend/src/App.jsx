import { Route, Switch } from 'wouter';
import AppLayout from './layouts/AppLayout';
import CreateEventPage from './pages/CreateEventPage';
import EventDetailsPage from './pages/EventDetailsPage';
import HomePage from './pages/HomePage';
import NotFoundPage from './pages/NotFoundPage';
import SignInPage from './pages/SignInPage';

export default function App() {
  return (
    <AppLayout>
      <Switch>
        <Route path="/" component={HomePage} />
        <Route path="/signin" component={SignInPage} />
        <Route path="/events/new" component={CreateEventPage} />
        <Route path="/events/:eventId" component={EventDetailsPage} />
        <Route component={NotFoundPage} />
      </Switch>
    </AppLayout>
  );
}
