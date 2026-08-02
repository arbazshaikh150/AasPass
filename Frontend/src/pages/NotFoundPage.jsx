import { Link } from 'wouter';

export default function NotFoundPage() {
  return (
    <section className="mx-auto max-w-xl py-16 text-center">
      <p className="text-sm font-semibold uppercase tracking-wide text-brand-700">404</p>
      <h2 className="mt-3 text-3xl font-semibold text-slate-950">Page not found</h2>
      <p className="mt-3 text-slate-600">The page you are looking for does not exist.</p>
      <Link
        href="/"
        className="mt-6 inline-flex h-10 items-center justify-center rounded-md bg-brand-600 px-4 text-sm font-medium text-white transition hover:bg-brand-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600"
      >
        Go Home
      </Link>
    </section>
  );
}
