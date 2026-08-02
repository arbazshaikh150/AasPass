import { useEffect } from 'react';
import toast from 'react-hot-toast';
import { ArrowRight, CalendarDays, KeyRound, MapPin, ShieldCheck } from 'lucide-react';
import { useAppSelector } from '../hooks/useAppSelector';
import Button from '../components/ui/Button';
import { startGoogleSignIn } from '../services/authService';

export default function SignInPage() {
  const { isAuthenticated, error } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (isAuthenticated) {
      toast.success('You are signed in');
    }
  }, [isAuthenticated]);

  return (
    <section className="grid min-h-[calc(100vh-8rem)] gap-8 lg:grid-cols-[1fr_0.9fr] lg:items-center">
      <div className="max-w-2xl space-y-8">
        <div className="inline-flex items-center gap-2 rounded-full border border-brand-100 bg-white px-3 py-1 text-sm font-medium text-brand-700 shadow-sm">
          <ShieldCheck className="h-4 w-4" aria-hidden="true" />
          Secure sign in
        </div>

        <div className="space-y-4">
          <h2 className="text-4xl font-semibold tracking-normal text-slate-950 sm:text-5xl">
            Sign in to manage and discover local events.
          </h2>
          <p className="max-w-xl text-base leading-7 text-slate-600">
            Find events around you, create your own gatherings, and keep your local plans in one place.
          </p>
        </div>

        <div className="grid gap-3 sm:grid-cols-3">
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <MapPin className="h-5 w-5 text-brand-600" aria-hidden="true" />
            <p className="mt-3 text-sm font-medium text-slate-800">Nearby events</p>
          </div>
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <CalendarDays className="h-5 w-5 text-brand-600" aria-hidden="true" />
            <p className="mt-3 text-sm font-medium text-slate-800">Create events</p>
          </div>
          <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <ShieldCheck className="h-5 w-5 text-brand-600" aria-hidden="true" />
            <p className="mt-3 text-sm font-medium text-slate-800">Simple access</p>
          </div>
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-soft sm:p-8">
        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-brand-50 text-brand-700">
          <KeyRound className="h-6 w-6" aria-hidden="true" />
        </div>

        <div className="mt-6 space-y-2">
          <h3 className="text-2xl font-semibold text-slate-950">Welcome back</h3>
          <p className="text-sm leading-6 text-slate-600">
            Continue with the Google account that should be linked to your Aaspass profile.
          </p>
        </div>

        <div className="mt-8 space-y-3">
          <Button onClick={startGoogleSignIn} className="w-full">
            Continue with Google
            <ArrowRight className="h-4 w-4" aria-hidden="true" />
          </Button>
        </div>

        {isAuthenticated ? (
          <div className="mt-4 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
            You are signed in.
          </div>
        ) : null}

        {error ? (
          <div className="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
            {error}
          </div>
        ) : null}
      </div>
    </section>
  );
}
