import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { ArrowLeft, CalendarDays, ImageIcon, Loader2, MapPin, Ticket } from 'lucide-react';
import { Link, useParams } from 'wouter';
import { ROUTES } from '../constants/routes';
import { getEventById } from '../services/eventService';
import { getImageViewUrl } from '../services/uploadService';

export default function EventDetailsPage() {
  const { eventId } = useParams();
  const [status, setStatus] = useState('loading');
  const [event, setEvent] = useState(null);
  const [imageUrls, setImageUrls] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadEventDetails() {
      try {
        setStatus('loading');
        setError('');

        const eventDetails = await getEventById(eventId);
        setEvent(eventDetails);

        const resolvedImages = await Promise.all(
          (eventDetails.imageUrls || []).map((key) => getImageViewUrl(key)),
        );
        setImageUrls(resolvedImages);
        setStatus('succeeded');
      } catch {
        setError('Unable to load event details.');
        setStatus('failed');
        toast.error('Unable to load event details');
      }
    }

    loadEventDetails();
  }, [eventId]);

  if (status === 'loading') {
    return (
      <section className="flex min-h-[calc(100vh-8rem)] items-center justify-center">
        <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-white px-5 py-4 text-sm text-slate-600 shadow-sm">
          <Loader2 className="h-5 w-5 animate-spin text-brand-600" aria-hidden="true" />
          Loading event details
        </div>
      </section>
    );
  }

  if (status === 'failed') {
    return (
      <section className="mx-auto max-w-2xl rounded-lg border border-rose-200 bg-rose-50 p-6 text-rose-700">
        <p className="text-sm font-medium">{error}</p>
        <Link href={ROUTES.home} className="mt-4 inline-flex text-sm font-semibold text-rose-800">
          Back home
        </Link>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-6xl space-y-6">
      <Link href={ROUTES.home} className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-slate-900">
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        Back home
      </Link>

      <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <div className="space-y-4">
          {imageUrls.length ? (
            <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
              <img src={imageUrls[0]} alt="" className="h-80 w-full object-cover" />
            </div>
          ) : (
            <div className="flex h-80 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-300 shadow-sm">
              <ImageIcon className="h-12 w-12" aria-hidden="true" />
            </div>
          )}

          {imageUrls.length > 1 ? (
            <div className="grid grid-cols-3 gap-3">
              {imageUrls.slice(1).map((url) => (
                <img key={url} src={url} alt="" className="h-28 rounded-lg border border-slate-200 object-cover shadow-sm" />
              ))}
            </div>
          ) : null}
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-soft">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-wide text-brand-700">Event</p>
              <h2 className="mt-2 text-3xl font-semibold tracking-normal text-slate-950">{event.eventName}</h2>
            </div>
            <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-700">
              {event.type}
            </span>
          </div>

          <p className="mt-5 text-sm leading-7 text-slate-600">
            {event.descriptionText || 'No description available.'}
          </p>

          {event.highlightedTags?.length ? (
            <div className="mt-6 flex flex-wrap gap-2">
              {event.highlightedTags.map((tag) => (
                <span key={tag} className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
                  {tag}
                </span>
              ))}
            </div>
          ) : null}

          <div className="mt-8 grid gap-3">
            <div className="flex items-center gap-3 rounded-lg bg-slate-50 p-4 text-sm text-slate-700">
              <MapPin className="h-5 w-5 text-brand-600" aria-hidden="true" />
              <span>
                {Number(event.latitude || 0).toFixed(4)}, {Number(event.longitude || 0).toFixed(4)}
              </span>
            </div>
            <div className="flex items-center gap-3 rounded-lg bg-slate-50 p-4 text-sm text-slate-700">
              <Ticket className="h-5 w-5 text-brand-600" aria-hidden="true" />
              <span>{event.type === 'PAID' ? `${event.numberOfSeats ?? 0} seats available` : 'Free to attend'}</span>
            </div>
            <div className="flex items-center gap-3 rounded-lg bg-slate-50 p-4 text-sm text-slate-700">
              <CalendarDays className="h-5 w-5 text-brand-600" aria-hidden="true" />
              <span>{event.createdAt ? new Date(event.createdAt).toLocaleDateString('en-IN') : 'Recently added'}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
