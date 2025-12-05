'use client'

import {GifVideo} from "@/src/model/response/response";
import {ReactNode} from "react";
import {ApiConfig, GifVideoStatus} from "@/src/config/config";
import Head from "next/head";
import {allType} from "@/src/helper/types";
import {AnimatedDot} from "@/src/component/common";

interface GifVideoProps {
    gifVideo: GifVideo | undefined;
    loading: boolean;
}

interface GifPageProps {
    children: ReactNode
}

function videoDownload(downloadLink: string) {
    return (
        <>
            <div className="mb-4 text-success display-4">
                ✓
            </div>
            <h1 className="mb-3 fs-3 fw-bold">Gif Ready for Download</h1>
            <p className="text-muted mb-4">
                Your video has been successfully processed and is ready to be accessed.
            </p>
            <a
                href={downloadLink}
                download
                className="btn btn-primary w-100 fw-bold"
            >
                Download GIF
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

function GifMainPage(props: GifPageProps) {
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

export function GifPageComponent(props: GifVideoProps) {
    if (props.loading && !props.gifVideo) {
        return (
            <GifMainPage>
                {videoError("Video not found.")}
            </GifMainPage>
        )
    }

    if (props.gifVideo?.status === GifVideoStatus.ERROR) {
        return (
            <GifMainPage>
                {videoError("An error occurred while converting the video to a GIF.")}
            </GifMainPage>
        )
    }

    const isVideoProcessed = props.gifVideo?.status === GifVideoStatus.SUCCESS
    const downloadLink = ApiConfig.GIF_DOWNLOAD_URL.replace("{transactionId}", String(props.gifVideo?.transactionId))
    return (
        <GifMainPage>
            { isVideoProcessed ? videoDownload(downloadLink) : videoProgress() }
        </GifMainPage>
    );
}