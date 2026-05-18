import { createBrowserRouter } from "react-router-dom";

function App() {
  return (
    <main className="min-h-screen bg-slate-50 p-6 text-slate-950">
      <h1 className="text-2xl font-semibold">Warehouse Ops Admin</h1>
      <p className="mt-2 text-sm text-slate-600">
        Back office shell for outbound waves, picking, print jobs, and agent status.
      </p>
    </main>
  );
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: <App />
  }
]);

