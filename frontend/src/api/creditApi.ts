import axios from 'axios';
import type {
    CreditExtractSummary,
    CreditExtractDetails,
    FetchRequest,
} from '../types';

const API_BASE = '/api';

const api = axios.create({
    baseURL: API_BASE,
    headers: {
        'Content-Type': 'application/json',
    },
});

export async function fetchCreditData(ssn: string): Promise<CreditExtractSummary> {
    const request: FetchRequest = { ssn };
    const response = await api.post<CreditExtractSummary>('/credit/fetch', request);
    return response.data;
}

export async function getHistory(ssn: string): Promise<CreditExtractSummary[]> {
    const response = await api.get<CreditExtractSummary[]>(`/credit/history/${ssn}`);
    return response.data;
}

export async function getDetails(id: number): Promise<CreditExtractDetails> {
    const response = await api.get<CreditExtractDetails>(`/credit/details/${id}`);
    return response.data;
}

export function extractErrorMessage(error: unknown): string {
    if (axios.isAxiosError(error)) {
        return error.response?.data?.error || error.message || 'Unknown error';
    }
    return 'Unknown error';
}