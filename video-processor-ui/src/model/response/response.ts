export interface ErrorResponse {
    message: string;
    code: number;
}


export interface GifVideo {
    id: number;
    transactionId: string;
    isUrl: boolean;
    path: string;
    pathType: string;
    processType: string;
    status: string;
    startTime: number;
    endTime: number;
    createdAt: Date;
    updatedAt: Date;
}