import { Outlet, useLocation } from 'react-router-dom';
import ErrorBoundary from './ErrorBoundary';

// Keyed by path so a page crash keeps the surrounding layout and clears on navigation
export default function RouteBoundary() {
  const { pathname } = useLocation();
  return (
    <ErrorBoundary key={pathname}>
      <Outlet />
    </ErrorBoundary>
  );
}
