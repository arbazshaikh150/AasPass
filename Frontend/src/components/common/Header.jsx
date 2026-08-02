import { CalendarPlus, Loader2, MapPin, Search } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { Link } from 'wouter';
import { useAppDispatch } from '../../hooks/useAppDispatch';
import { useAppSelector } from '../../hooks/useAppSelector';
import { clearSearch, runEventSearch } from '../../features/search/searchSlice';
import { ROUTES } from '../../constants/routes';

export default function Header() {
  const dispatch = useAppDispatch();
  const { isAuthenticated } = useAppSelector((state) => state.auth);
  const { status, query } = useAppSelector((state) => state.search);
  const [searchText, setSearchText] = useState(query);
  const isSearching = status === 'loading';

  const handleSearch = async (event) => {
    event.preventDefault();

    const nextQuery = searchText.trim();
    if (!nextQuery) {
      dispatch(clearSearch());
      return;
    }

    const result = await dispatch(runEventSearch(nextQuery));
    if (runEventSearch.rejected.match(result)) {
      toast.error('Search failed. Please try again.');
    }
  };

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex min-h-16 w-full max-w-7xl flex-col gap-3 px-4 py-3 sm:px-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <Link href={ROUTES.home} className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand-600 text-white">
            <MapPin className="h-5 w-5" aria-hidden="true" />
          </div>
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-brand-700">Aaspass</p>
            <h1 className="text-base font-semibold text-slate-950">Local Discovery Platform</h1>
          </div>
        </Link>

        <div className="flex w-full items-center gap-2 lg:w-auto">
          {isAuthenticated ? (
            <>
              <form onSubmit={handleSearch} className="relative w-full lg:w-96">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" aria-hidden="true" />
                <input
                  type="search"
                  value={searchText}
                  onChange={(event) => setSearchText(event.target.value)}
                  placeholder="Search events nearby"
                  className="h-10 w-full rounded-md border border-slate-300 bg-white pl-10 pr-12 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
                />
                <button
                  type="submit"
                  disabled={isSearching}
                  className="absolute right-1 top-1/2 inline-flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-md text-slate-500 transition hover:bg-slate-100 disabled:opacity-60"
                  aria-label="Search events"
                >
                  {isSearching ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : <Search className="h-4 w-4" aria-hidden="true" />}
                </button>
              </form>
              <Link
                href={ROUTES.createEvent}
                className="hidden h-10 items-center justify-center gap-2 rounded-md bg-brand-600 px-4 text-sm font-medium text-white transition hover:bg-brand-700 sm:inline-flex"
              >
                <CalendarPlus className="h-4 w-4" aria-hidden="true" />
                Create
              </Link>
            </>
          ) : (
            <Link
              href={ROUTES.signIn}
              className="hidden h-10 items-center justify-center rounded-md bg-brand-600 px-4 text-sm font-medium text-white transition hover:bg-brand-700 sm:inline-flex"
            >
              Sign In
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
