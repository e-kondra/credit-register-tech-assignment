export interface CreditExtractSummary {
    id: number;
    ssn: string;
    fetchDate: string;
    extractReference: string;
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