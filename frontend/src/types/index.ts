export type CreditExtractStatus = 'SUCCESS' | 'PCR_ERROR' | 'DECEASED' | 'SERVICE_ERROR';

export interface CreditExtractSummary {
    id: number;
    ssn: string;
    fetchDate: string;
    extractReference: string;
    status: CreditExtractStatus;
    voluntaryCreditBan: boolean;
    banReason: string | null;
    lendersCount: number | null;
    loanContractsCount: number | null;
    totalLoanAmount: number | null;
    currencyCode: string | null;
}

export interface CreditExtractDetails extends CreditExtractSummary {
    fullResponse: string;
}

export interface FetchRequest {
    ssn: string;
}

export interface ApiError {
    error: string;
}