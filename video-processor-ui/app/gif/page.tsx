import {Suspense} from "react";
import GifContent from "@/app/gif/gif-content";

export default function GifPage() {
    return (
        <main>
            <Suspense>
                <GifContent />
            </Suspense>
        </main>
    );
}