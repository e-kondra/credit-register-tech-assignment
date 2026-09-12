import { Link, Outlet } from 'react-router-dom';

export function Layout() {
    return (
        <div className="app">
            <header className="header">
                <h1>Credit Register Test Assignment</h1>
                <nav>
                    <Link to="/">Fetch</Link>
                    <Link to="/history">History</Link>
                </nav>
            </header>
            <main className="content">
                <Outlet />
            </main>
        </div>
    );
}