'use client';

import { useState, ChangeEvent, FormEvent } from 'react';
import {requiredSpan, alert, ifElse, timeToSeconds} from "@/src/helper/helper";
import {VideoGifRequest} from "@/src/model/request/request";
import {uploadVideo} from "@/src/service/api";
import { useRouter } from 'next/navigation';
import {ApiConfig, VideoProcessType} from "@/src/config/config";
import TimePicker from "react-time-picker";
import 'react-time-picker/dist/TimePicker.css';
import 'react-clock/dist/Clock.css';

const initialVideoData: VideoGifRequest = {
    file: null,
    url: '',
    startTime: 0,
    endTime: 0,
    processType: 'GIF'
};

export default function Home() {
    const [data, setData] = useState<VideoGifRequest>(initialVideoData);
    const [startTime, setStartTime] = useState<string|null>("00:00:00");
    const [endTime, setEndTime] = useState<string|null>("00:00:00");
    const [videoNotFound, setVideoNotFound] = useState(false);
    const [invalidProcessType, setInvalidProcessType] = useState(false);
    const [invalidStartTime, setInvalidStartTime] = useState(false);
    const [invalidEndTime, setInvalidEndTime] = useState(false);
    const [alertMessage, setAlertMessage] = useState("");
    const [fieldErrMessage, setFieldErrMessageMessage] = useState<string|null>(null);
    const router = useRouter();

    // General handler for all input changes
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const {id, value, type, files } = e.target;

        if (type === 'file' && files) {
            // File upload area
            setData(prev => ({
                ...prev,
                file: files[0] || null,
                url: '', // Clear link when file is selected
            }));
        } else if (id === 'videoLink') {
            // Link input area
            setData(prev => ({
                ...prev,
                url: value,
                file: null, // Clear file when link is entered
            }));
        } else if (id === 'startTime' || id === 'endTime') {
            // Numeric inputs
            setData(prev => ({
                ...prev,
                [id]: Number(value),
            }));
        }
    }
    const handleSelectChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const {id, value } = e.target;
        setData(prev => ({
            ...prev,
            [id]: value,
        }));
    }

    const handleTime = (value: string | null, id: string) => {
        if (id == "startTime") {
            setStartTime(value);
        }

        if (id == "endTime") {
            setEndTime(value);
        }

        if (value) {
            setData(prev => ({
                ...prev,
                [id]: timeToSeconds(value),
            }));
        }
    }

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        let formBody = null;
        if (!data.file && !data.url) {
            setVideoNotFound(true);
            setFieldErrMessageMessage("Video file or url not found.");
            return;
        }

        setVideoNotFound(false);
        if (!data.processType) {
            setInvalidProcessType(true);
            setFieldErrMessageMessage("Invalid process type.");
        }
        setInvalidProcessType(false);

        if (data.startTime == null || data.startTime < 0) {
            setInvalidStartTime(true);
            setFieldErrMessageMessage("Invalid start time.");
            return;
        }
        setInvalidStartTime(false);

        if (data.endTime == null || data.endTime < 0 || data.endTime < data.startTime) {
            setInvalidEndTime(true);
            setFieldErrMessageMessage("Invalid end time.");
            return;
        }

        if (data.processType == VideoProcessType.GIF && data.endTime - data.startTime > ApiConfig.GIF_START_END_TIME_RANGE) {
            setInvalidEndTime(true);
            setFieldErrMessageMessage("Invalid time range.");
            return;
        }

        if (data.processType == VideoProcessType.CUT && data.endTime - data.startTime > ApiConfig.CUT_START_END_TIME_RANGE) {
            setInvalidEndTime(true);
            setFieldErrMessageMessage("Invalid time range.");
            return;
        }
        setInvalidEndTime(false);

        const isUrl = !!data.url;
        formBody = JSON.stringify(data);

        if (!isUrl) {
            formBody = new FormData(e.currentTarget);
            formBody.set("startTime", String(data.startTime));
            formBody.set("endTime", String(data.endTime));
        }

        uploadVideo(formBody, isUrl).then((res) => {
            console.log("Routing....");
            router.push(`/video?transactionId=${res.transactionId}`);
        }).catch((err) => {
            setAlertMessage(err.message);
            console.log('Error:', err);
        })
    };

    return (
        <div className="container mt-5">
            {ifElse(alertMessage, alert("danger", alertMessage), "")}
            <h1 className="mb-4 text-center">🎥 Video Processor Tool</h1>
            <form onSubmit={handleSubmit} className="p-4 border rounded shadow-sm">

                <div className="mb-4">
                    <label htmlFor="videoLink" className="form-label fw-bold">Process Type</label>
                    <select className={`form-control ${invalidProcessType ? 'border border-danger' : ''}`} name="processType" id="processType" onChange={handleSelectChange}>
                        <option value="GIF">Video To GIF</option>
                        <option value="CUT">Cut The Video</option>
                    </select>
                    { requiredSpan(invalidProcessType, fieldErrMessage) }
                </div>
                {/* --- 1. Video Upload Area (File) --- */}
                <div className="mb-4">
                    <input
                        className={`form-control ${videoNotFound ? 'border border-danger' : ''}`}
                        type="file"
                        id="videoUpload"
                        name="file"
                        accept="video/*"
                        onChange={handleChange}
                        disabled={!!data.url}
                    />
                    { requiredSpan(videoNotFound, fieldErrMessage) }
                </div>

                <div className="text-center my-3">
                    <span className="badge bg-secondary">OR</span>
                </div>
                <div className="mb-4">
                    <label htmlFor="videoLink" className="form-label fw-bold">2. Youtube Video Link (URL)</label>
                    <input
                        type="url"
                        className={`form-control ${videoNotFound ? 'border border-danger' : ''}`}
                        id="videoLink"
                        name="url"
                        placeholder="E.g.: https://www.youtube.com/watch?v=..."
                        value={data.url}
                        onChange={handleChange}
                        disabled={!!data.file}
                    />
                </div>
                { requiredSpan(videoNotFound) }
                <hr />

                {/* --- 3. Time Adjustment Fields --- */}
                <div className="row g-3">
                    <div className="col-md-6">
                        <label htmlFor="startTime" className="form-label fw-bold" >3. Start Time (Seconds)</label>
                        <TimePicker
                            format="HH:mm:ss"
                            disableClock={true}
                            onChange={(value) => handleTime(value, "startTime")}
                            maxDetail="second"
                            clearIcon={null}
                            className={`form-control ${invalidStartTime ? 'border border-danger' : ''}`}
                            id="startTime"
                            name="startTime"
                            value={startTime}
                            required
                        />
                        <div className="form-text">The second where the video trimming should start.</div>
                        { requiredSpan(invalidStartTime, fieldErrMessage) }
                    </div>

                    <div className="col-md-6">
                        <label htmlFor="endTime" className="form-label fw-bold">4. End Time (Seconds)</label>
                        <TimePicker
                            format="HH:mm:ss"
                            disableClock={true}
                            onChange={(value) => handleTime(value, "endTime")}
                            maxDetail="second"
                            clearIcon={null}
                            className={`form-control ${invalidStartTime ? 'border border-danger' : ''}`}
                            id="endTime"
                            name="endTime"
                            value={endTime}
                            required
                        />
                        <div className="form-text">The second where the video trimming should end.</div>
                        { requiredSpan(invalidEndTime, fieldErrMessage) }
                    </div>
                </div>

                {/* --- Form Submit Button --- */}
                <div className="d-grid gap-2 mt-4">
                    <button type="submit" className="btn btn-primary btn-lg">Start Process</button>
                </div>
            </form>
        </div>
    );
}