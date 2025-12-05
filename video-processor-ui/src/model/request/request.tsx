export interface VideoGifRequest {
    file: File | null;
    url: string;
    startTime: number;
    endTime: number;
}