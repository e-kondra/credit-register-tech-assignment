import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getHistory, extractErrorMessage } from '../api/creditApi';
import type { CreditExtractSummary } from '../types';

export function HistoryPage() {
    const [ssn, setSsn] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [history, setHistory] = useState<CreditExtractSummary[] | null>(null);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!ssn.trim()) {
            setError('Please enter SSN');
            return;
        }
        setLoading(true);
        setError(null);
        setHistory(null);

        try {
            const data = await getHistory(ssn.trim());
            setHistory(data);
        } catch (err) {
            setError(extractErrorMessage(err));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page">
            <h2>Fetch History</h2>
            <form onSubmit={handleSearch} className="form">
                <label>
                    SSN:
                    <input
                        type="text"
                        value={ssn}
                        onChange={(e) => setSsn(e.target.value)}
                        placeholder="e.g. 987654-321"
                        disabled={loading}
                    />
                </label>
                <button type="submit" disabled={loading || !ssn.trim()}>
                    {loading ? 'Searching...' : 'Search'}
                </button>
            </form>

            {error && (
                <div className="error">
                    <strong>Error:</strong> {error}
                </div>
            )}

            {history && history.length === 0 && (
                <p>No records found for SSN "{ssn}".</p>
            )}

            {history && history.length > 0 && (
                <table className="history-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Fetch Date</th>
                            <th>Reference</th>
                            <th>Credit Ban</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {history.map((item) => (
                            <tr key={item.id}>
                                <td>{item.id}</td>
                                <td>{new Date(item.fetchDate).toLocaleString()}</td>
                                <td>{item.extractReference}</td>
                                <td>
                                    {item.voluntaryCreditBan ? (
                                        <span className="ban-active">YES</span>
                                    ) : (
                                        <span className="ban-inactive">No</span>
                                    )}
                                </td>
                                <td>
                                    <Link to={`/details/${item.id}`}>View</Link>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}