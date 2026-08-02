import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Link } from 'wouter';
import { CalendarPlus, Compass, Loader2, LocateFixed, LogIn, MapPin, Search } from 'lucide-react';
import { useAppDispatch } from '../hooks/useAppDispatch';
import { useAppSelector } from '../hooks/useAppSelector';
import { verifySession } from '../features/auth/authSlice';
import { fetchNearbyFeed } from '../features/feed/feedSlice';
import { clearSearch } from '../features/search/searchSlice';
import { getCurrentBrowserLocation } from '../services/locationService';
import { getImageViewUrl } from '../services/uploadService';
import { ROUTES } from '../constants/routes';

function EventThumbnail({ imageKey, fallbackElement }) {
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadImage() {
      if (!imageKey) return;

      try {
        const url = await getImageViewUrl(imageKey);
        if (!ignore) {
          setImageUrl(url);
        }
      } catch {
        if (!ignore) {
          setImageUrl('');
        }
      }
    }

    loadImage();

    return () => {
      ignore = true;
    };
  }, [imageKey]);

  if (imageUrl) {
    return <img src={imageUrl} alt="" className="h-40 w-full object-cover" />;
  }

  return (
    <div className="flex h-40 w-full items-center justify-center bg-slate-100 text-slate-400">
      {fallbackElement}
    </div>
  );
}

function NearbyEventCard({ event }) {
  const eventId = event.eventId || event.id;

  return (
    <Link href={ROUTES.eventDetails(eventId)} className="block overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-soft">
      <EventThumbnail imageKey={event.imageUrls?.[0]} fallbackElement={<CalendarPlus className="h-8 w-8" aria-hidden="true" />} />
      <div className="p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="text-base font-semibold text-slate-950">{event.eventName || 'Local event'}</h3>
            <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">
              {event.descriptionText || 'More details will be available soon.'}
            </p>
          </div>
          <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-medium text-brand-700">
            {event.type || 'Event'}
          </span>
        </div>
        <div className="mt-4 flex items-center gap-2 text-sm text-slate-500">
          <MapPin className="h-4 w-4" aria-hidden="true" />
          <span>{Number(event.distanceKm || 0).toFixed(2)} km away</span>
        </div>
      </div>
    </Link>
  );
}

function SearchResultCard({ event }) {
  return (
    <Link href={ROUTES.eventDetails(event.eventId)} className="block overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-soft">
      <EventThumbnail imageKey={event.imageUrls?.[0]} fallbackElement={<Search className="h-8 w-8" aria-hidden="true" />} />
      <div className="p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="text-base font-semibold text-slate-950">{event.eventName}</h3>
            <p className="mt-2 line-clamp-2 text-sm leading-6 text-slate-600">{event.descriptionText}</p>
          </div>
          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">
            {event.type}
          </span>
        </div>
        {event.highlightedTags?.length ? (
          <div className="mt-4 flex flex-wrap gap-2">
            {event.highlightedTags.slice(0, 4).map((tag) => (
              <span key={tag} className="rounded-full bg-brand-50 px-3 py-1 text-xs font-medium text-brand-700">
                {tag}
              </span>
            ))}
          </div>
        ) : null}
      </div>
    </Link>
  );
}

export default function HomePage() {
  const dispatch = useAppDispatch();
  const auth = useAppSelector((state) => state.auth);
  const feed = useAppSelector((state) => state.feed);
  const searchState = useAppSelector((state) => state.search);
  const feedRequestedRef = useRef(false);

  useEffect(() => {
    if (auth.status === 'idle') {
      dispatch(verifySession());
    }
  }, [auth.status, dispatch]);

  useEffect(() => {
    async function loadFeedFromCurrentLocation() {
      try {
        const location = await getCurrentBrowserLocation();
        const result = await dispatch(fetchNearbyFeed(location));

        if (fetchNearbyFeed.rejected.match(result)) {
          toast.error(result.error.message || 'Unable to load nearby events.');
        }
      } catch (error) {
        toast.error(error.message);
      }
    }

    if (auth.isAuthenticated && !feedRequestedRef.current) {
      feedRequestedRef.current = true;
      loadFeedFromCurrentLocation();
    }
  }, [auth.isAuthenticated, dispatch]);

  if (auth.status === 'loading' || auth.status === 'idle') {
    return (
      <section className="flex min-h-[calc(100vh-8rem)] items-center justify-center">
        <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-white px-5 py-4 text-sm text-slate-600 shadow-sm">
          <Loader2 className="h-5 w-5 animate-spin text-brand-600" aria-hidden="true" />
          Checking your backend session
        </div>
      </section>
    );
  }

  if (!auth.isAuthenticated) {
    return (
      <section className="grid min-h-[calc(100vh-8rem)] gap-8 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
        <div className="space-y-5">
          <p className="text-sm font-semibold uppercase tracking-wide text-brand-700">Welcome</p>
          <h2 className="max-w-3xl text-4xl font-semibold tracking-normal text-slate-950 sm:text-5xl">
            Sign in to see events happening around you.
          </h2>
          <p className="max-w-xl text-base leading-7 text-slate-600">
            Discover nearby gatherings, follow what is happening around you, and create your own event when you are ready.
          </p>
          <Link
            href={ROUTES.signIn}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-brand-600 px-4 text-sm font-medium text-white transition hover:bg-brand-700"
          >
            <LogIn className="h-4 w-4" aria-hidden="true" />
            Sign In
          </Link>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-soft">
          <Compass className="h-10 w-10 text-brand-600" aria-hidden="true" />
          <h3 className="mt-5 text-xl font-semibold text-slate-950">Nearby feed</h3>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Your home feed is personalized around your current location.
          </p>
        </div>
      </section>
    );
  }

  const showingSearchResults = searchState.query && searchState.status !== 'idle';

  return (
    <section className="space-y-8">
      <div className="flex flex-col justify-between gap-4 border-b border-slate-200 pb-6 lg:flex-row lg:items-end">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-brand-700">Home</p>
          <h2 className="mt-2 text-3xl font-semibold tracking-normal text-slate-950">
            {showingSearchResults ? `Search results for "${searchState.query}"` : 'Nearby events for you'}
          </h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
            {showingSearchResults
              ? 'Explore matching events and find something that fits your plans.'
              : 'Fresh local events based on where you are right now.'}
          </p>
        </div>

        {showingSearchResults ? (
          <button
            type="button"
            onClick={() => dispatch(clearSearch())}
            className="h-10 rounded-md border border-slate-300 bg-white px-4 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
          >
            Back to Nearby Feed
          </button>
        ) : (
          <div className="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600">
            <LocateFixed className="h-4 w-4 text-brand-600" aria-hidden="true" />
            {feed.location ? 'Location ready' : 'Finding your location'}
          </div>
        )}
      </div>

      {!showingSearchResults ? (
        <div className="flex flex-col justify-between gap-4 rounded-lg border border-brand-100 bg-brand-50 p-5 sm:flex-row sm:items-center">
          <div>
            <h3 className="text-lg font-semibold text-slate-950">Hosting something nearby?</h3>
            <p className="mt-1 text-sm leading-6 text-slate-600">
              Create an event and make it easier for people around you to discover it.
            </p>
          </div>
          <Link
            href={ROUTES.createEvent}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-md bg-brand-600 px-4 text-sm font-medium text-white transition hover:bg-brand-700"
          >
            <CalendarPlus className="h-4 w-4" aria-hidden="true" />
            Create Event
          </Link>
        </div>
      ) : null}

      {showingSearchResults ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {searchState.status === 'loading' ? (
            <div className="col-span-full flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-5 text-sm text-slate-600">
              <Loader2 className="h-5 w-5 animate-spin text-brand-600" aria-hidden="true" />
              Searching events
            </div>
          ) : null}

          {searchState.status === 'failed' ? (
            <div className="col-span-full rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">
              {searchState.error}
            </div>
          ) : null}

          {searchState.status === 'succeeded' && searchState.results.length === 0 ? (
            <div className="col-span-full rounded-lg border border-slate-200 bg-white p-5 text-sm text-slate-600">
              No events matched your search.
            </div>
          ) : null}

          {searchState.results.map((event) => (
            <SearchResultCard key={event.eventId} event={event} />
          ))}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {feed.status === 'loading' ? (
            <div className="col-span-full flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-5 text-sm text-slate-600">
              <Loader2 className="h-5 w-5 animate-spin text-brand-600" aria-hidden="true" />
              Loading nearby feed
            </div>
          ) : null}

          {feed.status === 'failed' ? (
            <div className="col-span-full rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">
              {feed.error}
            </div>
          ) : null}

          {feed.status === 'succeeded' && feed.events.length === 0 ? (
            <div className="col-span-full rounded-lg border border-slate-200 bg-white p-5 text-sm text-slate-600">
              No nearby events were found for your current location.
            </div>
          ) : null}

          {feed.events.map((event) => (
            <NearbyEventCard key={event.eventId || event.id} event={event} />
          ))}
        </div>
      )}
    </section>
  );
}
