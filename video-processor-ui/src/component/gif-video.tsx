'use client'

import {GifVideo} from "@/src/model/response/response";
import {ReactNode} from "react";
import {ApiConfig, VideoProcessType, VideoStatus} from "@/src/config/config";
import Head from "next/head";
import {allType} from "@/src/helper/types";
import {AnimatedDot} from "@/src/component/common";

interface GifVideoProps {
    gifVideo: GifVideo | undefined;
    loading: boolean;
}

interface VideoPageProps {
    children: ReactNode
}

function videoDownload(downloadLink: string, processType:string) {
    return (
        <>
            <div className="mb-4 text-success display-4">
                ✓
            </div>
            <h1 className="mb-3 fs-3 fw-bold">{processType} Ready for Download</h1>
            <p className="text-muted mb-4">
                Your video has been successfully processed and is ready to be accessed.
            </p>
            <a
                href={downloadLink}
                download
                className="btn btn-primary w-100 fw-bold"
            >
                Download {processType}
            </a>
        </>
    )
}

function videoProgress() {
    return (
        <>
            <div className="mb-4 text-primary display-4">
                <AnimatedDot />
            </div>
            <h1 className="mb-3 fs-3 fw-bold">Video is Processing</h1>
            <p className="text-muted mb-4">
                This may take a few minutes. Please keep this page open or check back later.
            </p>

            {/* Minimalist Progress Bar */}
            <div className="progress mb-3" style={{ height: '8px' }}>
                <div
                    className="progress-bar progress-bar-striped progress-bar-animated bg-primary"
                    role="progressbar"
                    style={{ width: '100%' }}
                    aria-valuenow={100}
                    aria-valuemin={0}
                    aria-valuemax={100}
                ></div>
            </div>
            <small className="text-muted">Status: Analyzing content...</small>
        </>
    )
}

function videoError(message: allType) {
    return (
        <>
            <div className="mb-4 text-danger display-4">
                x
            </div>
            <h1 className="mb-3 fs-3 fw-bold">An error occurred</h1>
            <p className="text-muted mb-4">
                <code>{message}</code>
            </p>
        </>
    )
}

function VideoMainPage(props: VideoPageProps) {
    return (
        <>
            <Head>
                <title>Video Status</title>
                <meta name="description" content="Check the status of your video processing job." />
            </Head>
            <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
                <div className="container" style={{ maxWidth: '500px' }}>
                    <div className="p-4 p-md-5 bg-white border rounded-3 text-center">
                    <a href={"/"} className="d-flex align-items-start justify-content-start">Back</a>
                        {props.children}
                    </div>
                </div>
            </div>
        </>
    )
}

export function VideoPageComponent(props: GifVideoProps) {
    if (props.loading && !props.gifVideo) {
        return (
            <VideoMainPage>
                {videoError("Video not found.")}
            </VideoMainPage>
        )
    }

    if (props.gifVideo?.status === VideoStatus.ERROR) {
        return (
            <VideoMainPage>
                {videoError("An error occurred while converting the video to a GIF.")}
            </VideoMainPage>
        )
    }

    const processType = props.gifVideo?.processType === VideoProcessType.GIF ? "GIF" : "Video";
    const isVideoProcessed = props.gifVideo?.status === VideoStatus.SUCCESS
    const downloadLink = ApiConfig.DOWNLOAD_URL.replace("{transactionId}", String(props.gifVideo?.transactionId))
    return (
        <VideoMainPage>
            { isVideoProcessed ? videoDownload(downloadLink, processType) : videoProgress() }
        </VideoMainPage>
    );
}