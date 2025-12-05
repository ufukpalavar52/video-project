
import {ApiConfig} from "@/src/config/config";
import {buildQueryType} from "@/src/helper/types";
import {httpBuildQuery} from "@/src/helper/helper";
import {ErrorResponse} from "@/src/model/response/response";

export interface ApiData {
    method: string;
    endpoint: string;
    headers?: Record<string, string>;
    params?: Record<string, buildQueryType>;
    data?: BodyInit;
}

let configCache: { API_URL: string } | null = null;

export async function apiFetcher<T>(data: ApiData): Promise<T> {
    const config = await ensureConfigLoaded();
    if (!config) {
        throw new Error("Missing config");
    }

    let url = `${config?.API_URL}${data.endpoint}`;
    let headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };
    if (data.headers) {
        headers = data.headers;
    }
    const method = data.method.toUpperCase();
    const body = data.data;

    if (data.params) {
        url = httpBuildQuery(url, data.params)
    }

    const options: RequestInit = {
        method,
        headers: headers,
        body: (method !== 'GET' && method !== 'HEAD' && data)
            ? body
            : undefined,
        next: {
            revalidate: 60
        }
    };

    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            const errorBody = (await response.json()) as ErrorResponse;
            const apiError = new ApiError<ErrorResponse>(`API request failed: ${response.status}`, errorBody);
            return new Promise((resolve, reject) => {
                reject(apiError);
            });
        }

        if (response.status === 204) {
            return {} as T;
        }

        return (await response.json()) as T;

    } catch (error) {
        console.error(`Fetch error (${url}):`, error);
        throw error;
    }
}


export function doRequest<T>(apiData: ApiData): Promise<T> {
    try {
        return apiFetcher<T>(apiData)
    } catch (error) {
        return new Promise((resolve, reject) => {
            reject(error);
        });
    }
}

async function ensureConfigLoaded(): Promise<{ API_URL: string } | null> {
    if (configCache) {
        return configCache;
    }
    try {
        const response = await fetch('/api/config');

        configCache =  await response.json();
        return configCache;

    } catch (error) {
        throw error;
    }
}