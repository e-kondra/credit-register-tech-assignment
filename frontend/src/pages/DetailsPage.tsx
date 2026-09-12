import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getDetails, extractErrorMessage } from '../api/creditApi';
import type { CreditExtractDetails } from '../types';

export function DetailsPage() {
    const { id } = useParams<{ id: string }>();
    const [details, setDetails] = useState<CreditExtractDetails | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;

        const fetchDetails = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await getDetails(Number(id));
                setDetails(data);
            } catch (err) {
                setError(extractErrorMessage(err));
            } finally {
                setLoading(false);
            }
        };

        fetchDetails();
    }, [id]);

    if (loading) return <p>Loading...</p>;
    if (error) return <div className="error"><strong>Error:</strong> {error}</div>;
    if (!details) return <p>Not found.</p>;

    let parsedResponse: unknown = null;
    try {
        parsedResponse = details.fullResponse ? JSON.parse(details.fullResponse) : null;
    } catch {
        // ignore
    }

    return (
        <div className="page">
            <Link to="/history" className="back-link">← Back to History</Link>
            <h2>Credit Extract Details {details.id}</h2>

            <section className="details-summary">
                <h3>Summary</h3>
                <table>
                    <tbody>
                        <tr><td>SSN:</td><td>{details.ssn}</td></tr>
                        <tr><td>Fetch Date:</td><td>{new Date(details.fetchDate).toLocaleString()}</td></tr>
                        <tr><td>Reference:</td><td>{details.extractReference}</td></tr>
                        <tr><td>Status:</td><td>{details.status}</td></tr>
                        <tr>
                            <td>Voluntary Credit Ban:</td>
                            <td>
                                {details.voluntaryCreditBan ? (
                                    <span className="ban-active">YES ({details.banReason})</span>
                                ) : (
                                    <span className="ban-inactive">No</span>
                                )}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </section>

            <section className="details-full">
                <h3>Full Response</h3>
                <pre>{JSON.stringify(parsedResponse, null, 2)}</pre>
            </section>
        </div>
    );
}