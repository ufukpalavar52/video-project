const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';
const VIDEO_BASE_ENDPOINT = "/video";
const VIDEO_UPLOAD_ENDPOINT = "/video/upload";
const VIDEO_URL_ENDPOINT = "/video/url";
const DOWNLOAD_ENDPOINT = "/video/download/{transactionId}";
const BASE_URL = process.env.VIDEO_PROCESSOR_BASE_URL || DEFAULT_API_BASE_URL;

export const ApiConfig = {
    BASE_URL: BASE_URL,
    VIDEO_BASE_ENDPOINT: VIDEO_BASE_ENDPOINT,
    VIDEO_UPLOAD_ENDPOINT: VIDEO_UPLOAD_ENDPOINT,
    VIDEO_URL_ENDPOINT: VIDEO_URL_ENDPOINT,
    DOWNLOAD_ENDPOINT: DOWNLOAD_ENDPOINT,
    DOWNLOAD_URL: BASE_URL + DOWNLOAD_ENDPOINT,
    GIF_START_END_TIME_RANGE: Number(process.env.GIF_START_END_TIME_RANGE || 60),
    CUT_START_END_TIME_RANGE: Number(process.env.CUT_START_END_TIME_RANGE || 300),
};

export const VideoStatus = {
    IN_PROGRESS: 'IN_PROGRESS',
    SUCCESS: 'SUCCESS',
    ERROR: 'ERROR',
}

export const VideoProcessType = {
    GIF: 'GIF',
    CUT: 'CUT',
}
