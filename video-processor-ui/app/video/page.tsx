import {Suspense} from "react";
import VideoContent from "@/app/video/video-content";

export default function GifPage() {
    return (
        <main>
            <Suspense>
                <VideoContent />
            </Suspense>
        </main>
    );
}