import React from "react";

import {allType, buildQueryType} from "@/src/helper/types";

export const requiredSpan= (condition: boolean, message: string|null = null) => {
    if (!message || message === "") {
        message = "This field is required.";
    }

    if (condition) {
        return (<span className="text-danger">{message}</span>)
    }
    return (<></>)
}

export const ifElse= (condition: allType, ifConn: allType, elseConn: allType) => {
    return condition ? ifConn : elseConn
}

export const alert = (alertType: string, message: allType) => {
    return (
        <div className={`alert alert-${alertType} alert-dismissible fade show`} role="alert">
            {React.isValidElement(message) ?
                message : (
                <>{message}</>
            )}
            <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    )
}

export const httpBuildQuery = (url: string, params: Record<string, buildQueryType>) => {
    const paramList:string[] = [];

    for (const key in params) {
        if (!Array.isArray(params[key])) {
            paramList.push(`${key}=${params[key]}`);
            continue;
        }

        for (const i in params[key]) {
            const value = params[key][i];
            paramList.push(`${key}[]=${value}`)
        }
    }

    if (paramList.length == 0) {
        return url;
    }

    return `${url}?${paramList.join('&')}`;
}

export function timeToSeconds(time: string) {
    const [h, m, s] = time.split(":").map(Number);
    return h * 3600 + m * 60 + s;
}