import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { ArrowLeft, CalendarPlus, ImagePlus, Loader2, LocateFixed, Plus, Trash2, UploadCloud, X } from 'lucide-react';
import { Link, useLocation } from 'wouter';
import Button from '../components/ui/Button';
import { useAppDispatch } from '../hooks/useAppDispatch';
import { useAppSelector } from '../hooks/useAppSelector';
import { createNewEvent } from '../features/events/eventSlice';
import { getCurrentBrowserLocation } from '../services/locationService';
import { getPresignedUploadUrl, uploadFileToS3 } from '../services/uploadService';
import { ROUTES } from '../constants/routes';

const defaultForm = {
  eventName: '',
  descriptionText: '',
  highlightedTags: '',
  type: 'FREE',
  latitude: '',
  longitude: '',
  numberOfSeats: '',
};

function splitLinesOrCommas(value) {
  return value
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean);
}

export default function CreateEventPage() {
  const dispatch = useAppDispatch();
  const [, navigate] = useLocation();
  const { createStatus } = useAppSelector((state) => state.events);
  const [form, setForm] = useState(defaultForm);
  const [eventImages, setEventImages] = useState([]);
  const eventImagesRef = useRef([]);
  const [uploadStatus, setUploadStatus] = useState('idle');
  const [locationStatus, setLocationStatus] = useState('idle');
  const isSubmitting = createStatus === 'loading';
  const isUploading = uploadStatus === 'loading';

  useEffect(() => {
    eventImagesRef.current = eventImages;
  }, [eventImages]);

  useEffect(() => {
    return () => {
      eventImagesRef.current.forEach((image) => {
        URL.revokeObjectURL(image.previewUrl);
      });
    };
  }, []);

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }));
  };

  const useCurrentLocation = async () => {
    try {
      setLocationStatus('loading');
      const location = await getCurrentBrowserLocation();
      setForm((current) => ({
        ...current,
        latitude: String(location.latitude),
        longitude: String(location.longitude),
      }));
      toast.success('Location added');
    } catch (error) {
      toast.error(error.message);
    } finally {
      setLocationStatus('idle');
    }
  };

  const handleImageSelection = (event) => {
    const files = Array.from(event.target.files || []);
    event.target.value = '';

    if (!files.length) {
      return;
    }

    const validFiles = files.filter((file) => ['image/jpeg', 'image/png', 'image/webp'].includes(file.type));
    if (validFiles.length !== files.length) {
      toast.error('Only JPG, PNG, and WebP images are supported');
    }

    if (!validFiles.length) {
      return;
    }

    setEventImages((current) => [
      ...current,
      ...validFiles.map((file) => ({
        id: `${file.name}-${file.size}-${crypto.randomUUID()}`,
        file,
        key: '',
        uploadUrl: '',
        name: file.name,
        size: file.size,
        previewUrl: URL.createObjectURL(file),
        status: 'selected',
      })),
    ]);
  };

  const getPresignedUrls = async () => {
    const pendingImages = eventImages.filter((image) => image.status === 'selected');

    if (!pendingImages.length) {
      toast.error('Choose images first');
      return;
    }

    try {
      setUploadStatus('loading');

      for (const image of pendingImages) {
        const { uploadUrl, key } = await getPresignedUploadUrl(image.file);
        setEventImages((current) =>
          current.map((item) =>
            item.id === image.id
              ? { ...item, uploadUrl, key, status: 'ready' }
              : item,
          ),
        );
      }

      toast.success('Images are ready to upload');
    } catch {
      toast.error('Could not prepare image upload');
    } finally {
      setUploadStatus('idle');
    }
  };

  const uploadPreparedImage = async (image) => {
    if (image.status === 'uploaded') {
      toast.success('Image already uploaded');
      return;
    }

    if (image.status !== 'ready' || !image.uploadUrl) {
      toast.error('Get a presigned URL first');
      return;
    }

    try {
      setEventImages((current) =>
        current.map((item) => (item.id === image.id ? { ...item, status: 'uploading' } : item)),
      );
      await uploadFileToS3(image.uploadUrl, image.file);
      setEventImages((current) =>
        current.map((item) => (item.id === image.id ? { ...item, status: 'uploaded' } : item)),
      );
      toast.success('Image uploaded');
    } catch {
      setEventImages((current) =>
        current.map((item) => (item.id === image.id ? { ...item, status: 'ready' } : item)),
      );
      toast.error('Image upload failed');
    }
  };

  const removeEventImage = (id) => {
    setEventImages((current) => {
      const image = current.find((item) => item.id === id);
      if (image?.previewUrl) {
        URL.revokeObjectURL(image.previewUrl);
      }

      return current.filter((item) => item.id !== id);
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const tags = splitLinesOrCommas(form.highlightedTags);
    const imageKeys = eventImages
      .filter((image) => image.status === 'uploaded')
      .map((image) => image.key);
    const latitude = Number(form.latitude);
    const longitude = Number(form.longitude);
    const seats = form.numberOfSeats ? Number(form.numberOfSeats) : null;

    if (!form.eventName.trim()) {
      toast.error('Event name is required');
      return;
    }

    if (!tags.length) {
      toast.error('Add at least one tag');
      return;
    }

    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      toast.error('Add a valid event location');
      return;
    }

    if (form.type === 'PAID' && (!Number.isFinite(seats) || seats < 0)) {
      toast.error('Add available seats for paid events');
      return;
    }

    if (isUploading) {
      toast.error('Please wait for image upload to finish');
      return;
    }

    if (eventImages.some((image) => image.status !== 'uploaded')) {
      toast.error('Upload selected images before creating the event');
      return;
    }

    const payload = {
      eventName: form.eventName.trim(),
      descriptionText: form.descriptionText.trim(),
      highlightedTags: tags,
      imageUrls: imageKeys,
      type: form.type,
      latitude,
      longitude,
      numberOfSeats: form.type === 'PAID' ? seats : null,
    };

    const result = await dispatch(createNewEvent(payload));

    if (createNewEvent.fulfilled.match(result)) {
      toast.success('Event created');
      navigate(ROUTES.home);
      return;
    }

    toast.error('Unable to create event');
  };

  return (
    <section className="mx-auto max-w-5xl space-y-6">
      <div className="flex flex-col justify-between gap-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm sm:flex-row sm:items-center">
        <div>
          <Link href={ROUTES.home} className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-slate-900">
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Back home
          </Link>
          <h2 className="mt-4 text-3xl font-semibold tracking-normal text-slate-950">Create an event</h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
            Add the details people need to understand where it is, what it is about, and how they can join.
          </p>
        </div>
        <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-brand-50 text-brand-700">
          <CalendarPlus className="h-7 w-7" aria-hidden="true" />
        </div>
      </div>

      <form onSubmit={handleSubmit} className="grid gap-6 lg:grid-cols-[1fr_18rem]">
        <div className="space-y-5 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="grid gap-5 sm:grid-cols-2">
            <label className="space-y-2 sm:col-span-2">
              <span className="text-sm font-medium text-slate-800">Event name</span>
              <input
                value={form.eventName}
                onChange={(event) => updateField('eventName', event.target.value)}
                className="h-11 w-full rounded-md border border-slate-300 px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                placeholder="Weekend music meetup"
              />
            </label>

            <label className="space-y-2 sm:col-span-2">
              <span className="text-sm font-medium text-slate-800">Description</span>
              <textarea
                value={form.descriptionText}
                onChange={(event) => updateField('descriptionText', event.target.value)}
                rows={5}
                className="w-full resize-none rounded-md border border-slate-300 px-3 py-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                placeholder="Share the key details, timing, and what attendees can expect."
              />
            </label>

            <label className="space-y-2">
              <span className="text-sm font-medium text-slate-800">Type</span>
              <select
                value={form.type}
                onChange={(event) => updateField('type', event.target.value)}
                className="h-11 w-full rounded-md border border-slate-300 bg-white px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
              >
                <option value="FREE">Free</option>
                <option value="PAID">Paid</option>
              </select>
            </label>

            <label className="space-y-2">
              <span className="text-sm font-medium text-slate-800">Seats</span>
              <input
                type="number"
                min="0"
                value={form.numberOfSeats}
                onChange={(event) => updateField('numberOfSeats', event.target.value)}
                disabled={form.type === 'FREE'}
                className="h-11 w-full rounded-md border border-slate-300 px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-100 disabled:text-slate-400"
                placeholder={form.type === 'FREE' ? 'Not needed' : '50'}
              />
            </label>

            <label className="space-y-2 sm:col-span-2">
              <span className="text-sm font-medium text-slate-800">Tags</span>
              <input
                value={form.highlightedTags}
                onChange={(event) => updateField('highlightedTags', event.target.value)}
                className="h-11 w-full rounded-md border border-slate-300 px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                placeholder="music, weekend, outdoor"
              />
            </label>

            <div className="space-y-3 sm:col-span-2">
              <div>
                <p className="text-sm font-medium text-slate-800">Event images</p>
                <p className="mt-1 text-xs text-slate-500">Upload JPG, PNG, or WebP images for your event.</p>
              </div>

              <label className="flex cursor-pointer flex-col items-center justify-center rounded-lg border border-dashed border-brand-200 bg-brand-50/60 px-5 py-8 text-center transition hover:border-brand-500 hover:bg-brand-50">
                <input
                  type="file"
                  accept="image/jpeg,image/png,image/webp"
                  multiple
                  onChange={handleImageSelection}
                  disabled={isUploading}
                  className="sr-only"
                />
                <span className="flex h-12 w-12 items-center justify-center rounded-lg bg-white text-brand-700 shadow-sm">
                  <UploadCloud className="h-6 w-6" aria-hidden="true" />
                </span>
                <span className="mt-4 text-sm font-semibold text-slate-900">
                  Choose images
                </span>
                <span className="mt-1 text-xs text-slate-500">Preview them here before uploading</span>
              </label>

              <Button
                type="button"
                variant="secondary"
                onClick={getPresignedUrls}
                disabled={isUploading || !eventImages.some((image) => image.status === 'selected')}
                className="w-full"
              >
                {isUploading ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : <UploadCloud className="h-4 w-4" aria-hidden="true" />}
                Get Presigned URL
              </Button>

              {eventImages.length ? (
                <div className="grid gap-3 sm:grid-cols-2">
                  {eventImages.map((image) => (
                    <div key={image.id} className="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
                      <button
                        type="button"
                        onClick={() => uploadPreparedImage(image)}
                        className="group relative block w-full overflow-hidden rounded-md bg-slate-100"
                      >
                        <img src={image.previewUrl} alt="" className="h-32 w-full object-cover transition group-hover:scale-105" />
                        <span className="absolute inset-x-2 bottom-2 rounded-md bg-white/95 px-2 py-1 text-xs font-medium text-slate-700 shadow-sm">
                          {image.status === 'selected' ? 'Get URL first' : null}
                          {image.status === 'ready' ? 'Click to upload' : null}
                          {image.status === 'uploading' ? 'Uploading...' : null}
                          {image.status === 'uploaded' ? 'Uploaded' : null}
                        </span>
                      </button>
                      <div className="mt-3 flex items-center gap-3">
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-slate-900">{image.name}</p>
                        <p className="mt-1 text-xs text-slate-500">{Math.ceil(image.size / 1024)} KB</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => removeEventImage(image.id)}
                        className="inline-flex h-9 w-9 items-center justify-center rounded-md text-slate-500 transition hover:bg-rose-50 hover:text-rose-600"
                        aria-label="Remove image"
                      >
                        <Trash2 className="h-4 w-4" aria-hidden="true" />
                      </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          </div>
        </div>

        <aside className="space-y-5">
          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Location</h3>
            <div className="mt-4 space-y-4">
              <label className="space-y-2">
                <span className="text-sm font-medium text-slate-800">Latitude</span>
                <input
                  value={form.latitude}
                  onChange={(event) => updateField('latitude', event.target.value)}
                  className="h-11 w-full rounded-md border border-slate-300 px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                  placeholder="19.0760"
                />
              </label>
              <label className="space-y-2">
                <span className="text-sm font-medium text-slate-800">Longitude</span>
                <input
                  value={form.longitude}
                  onChange={(event) => updateField('longitude', event.target.value)}
                  className="h-11 w-full rounded-md border border-slate-300 px-3 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                  placeholder="72.8777"
                />
              </label>
              <Button type="button" variant="secondary" onClick={useCurrentLocation} disabled={locationStatus === 'loading'} className="w-full">
                {locationStatus === 'loading' ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : <LocateFixed className="h-4 w-4" aria-hidden="true" />}
                Use Current Location
              </Button>
            </div>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Preview</h3>
            <div className="mt-4 rounded-lg bg-slate-50 p-4">
              {eventImages[0]?.previewUrl ? (
                <img src={eventImages[0].previewUrl} alt="" className="mb-4 h-32 w-full rounded-md object-cover" />
              ) : (
                <div className="mb-4 flex h-32 w-full items-center justify-center rounded-md bg-white text-slate-300">
                  <ImagePlus className="h-8 w-8" aria-hidden="true" />
                </div>
              )}
              <p className="text-base font-semibold text-slate-950">{form.eventName || 'Event name'}</p>
              <p className="mt-2 line-clamp-3 text-sm leading-6 text-slate-600">
                {form.descriptionText || 'Your event description will appear here.'}
              </p>
              <div className="mt-4 flex flex-wrap gap-2">
                {splitLinesOrCommas(form.highlightedTags).slice(0, 3).map((tag) => (
                  <span key={tag} className="inline-flex items-center gap-1 rounded-full bg-white px-3 py-1 text-xs font-medium text-brand-700">
                    {tag}
                    <X className="h-3 w-3" aria-hidden="true" />
                  </span>
                ))}
              </div>
            </div>
          </div>

          <Button type="submit" disabled={isSubmitting || isUploading} className="w-full">
            {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : <Plus className="h-4 w-4" aria-hidden="true" />}
            Create Event
          </Button>
        </aside>
      </form>
    </section>
  );
}
