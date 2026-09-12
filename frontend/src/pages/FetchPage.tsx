import { useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchCreditData, extractErrorMessage } from '../api/creditApi';
import type { CreditExtractSummary } from '../types';

export function FetchPage() {
    const [ssn, setSsn] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [result, setResult] = useState<CreditExtractSummary | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!ssn.trim()) {
            setError('Please enter SSN');
            return;
        }
        setLoading(true);
        setError(null);
        setResult(null);

        try {
            const data = await fetchCreditData(ssn.trim());
            setResult(data);
        } catch (err) {
            setError(extractErrorMessage(err));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page">
            <h2>Fetch Credit Data</h2>
            <form onSubmit={handleSubmit} className="form">
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
                    {loading ? 'Fetching...' : 'Fetch'}
                </button>
            </form>

            {error && (
                <div className="error">
                    <strong>Error:</strong> {error}
                </div>
            )}

            {result && (
                <div className="result">
                    <h3>Result</h3>
                    <table>
                        <tbody>
                            <tr><td>ID:</td><td>{result.id}</td></tr>
                            <tr><td>SSN:</td><td>{result.ssn}</td></tr>
                            <tr><td>Fetch Date:</td><td>{new Date(result.fetchDate).toLocaleString()}</td></tr>
                            <tr><td>Reference:</td><td>{result.extractReference}</td></tr>
                            <tr>
                                <td>Voluntary Credit Ban:</td>
                                <td>
                                    {result.voluntaryCreditBan ? (
                                        <span className="ban-active">YES ({result.banReason})</span>
                                    ) : (
                                        <span className="ban-inactive">No</span>
                                    )}
                                </td>
                            </tr>
                            <tr><td>Lenders:</td><td>{result.lendersCount ?? '-'}</td></tr>
                            <tr>
                                <td>Total Loan Amount:</td>
                                <td>
                                    {result.totalLoanAmount
                                        ? `${result.totalLoanAmount} ${result.currencyCode}`
                                        : '-'}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <Link to={`/details/${result.id}`} className="link">
                        View Details →
                    </Link>
                </div>
            )}
        </div>
    );
}