'use client'

import {useEffect, useState} from "react";
import {GifVideo} from "@/src/model/response/response";
import {useSearchParams} from "next/navigation";
import {getVideo} from "@/src/service/api";
import {VideoPageComponent} from "@/src/component/gif-video";

export default function VideoContent() {
    const [gifData, setGifData] = useState<GifVideo>()
    const [loading, setLoading] = useState<boolean>(false);
    const searchParams = useSearchParams();
    const pollingInterval = 5000;
    const transactionId: string = String(searchParams.get("transactionId"));
    useEffect(() => {
        if (!transactionId) {
            return
        }

        const fetchData = async () => {
            try {
                const response = await getVideo(transactionId);
                setGifData(response);
                setLoading(true);
                return response;
            } catch (error) {
                console.error(error);
                throw error;
            }
        }

        const checkStatus = (response: GifVideo) => {
            if (response.status !== "IN_PROGRESS") {
                clearInterval(intervalId);
            }
        }

        const intervalId = setInterval(async () => {
            const response = await fetchData();
            checkStatus(response);
        }, pollingInterval);

        fetchData().then((res) => {
            checkStatus(res);
        });

        return () => {
            clearInterval(intervalId);
        }

    }, [transactionId]);


    return (
        <VideoPageComponent gifVideo={gifData} loading={loading}/>
    );
}