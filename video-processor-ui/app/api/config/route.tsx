
import { NextResponse } from 'next/server';
import {ApiConfig} from "@/src/config/config";

export async function GET() {
    return NextResponse.json(ApiConfig);
}