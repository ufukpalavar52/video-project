
import { NextResponse } from 'next/server';
import {ApiConfig} from "@/src/config/config";

export async function GET() {
    const config = {
        API_URL: ApiConfig.BASE_URL,
        START_END_TIME_RANGE: ApiConfig.START_END_TIME_RANGE
    }
    return NextResponse.json(config);
}