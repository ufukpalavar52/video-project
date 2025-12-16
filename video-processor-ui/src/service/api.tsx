
import {ApiData, doRequest} from "@/src/service/fetcher";
import {GifVideo} from "@/src/model/response/response";
import {ApiConfig} from "@/src/config/config";

export async function uploadVideo(request: BodyInit, isUrl:boolean): Promise<GifVideo> {
    let endpoint = ApiConfig.VIDEO_UPLOAD_ENDPOINT;
    const headers: Record<string, string> = {
        "Accept": "application/json",
    }
    if (isUrl) {
        endpoint = ApiConfig.VIDEO_URL_ENDPOINT
        headers["Content-Type"] = "application/json"
    }

    const apiData: ApiData = {
        method: "POST",
        data: request,
        endpoint: endpoint,
        headers: headers

    }
    return doRequest(apiData)
}

export async function getVideo(transactionId: string): Promise<GifVideo> {
    const endpoint = ApiConfig.VIDEO_BASE_ENDPOINT
    const apiData: ApiData = {
        method: "GET",
        endpoint: endpoint,
        params: {
            transactionId: transactionId,
        }
    }
    return doRequest(apiData)
}