import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { FetchPage } from './pages/FetchPage';
import { HistoryPage } from './pages/HistoryPage';
import { DetailsPage } from './pages/DetailsPage';
import './App.css';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<FetchPage />} />
                    <Route path="history" element={<HistoryPage />} />
                    <Route path="details/:id" element={<DetailsPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
