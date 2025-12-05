'use client'

import {JSX, useEffect, useState} from "react";

interface AnimatedDotProps {
    interval?: number;
    character?: string;
}

export function AnimatedDot(props: AnimatedDotProps): JSX.Element {
    const dotInterval = props.interval || 500;
    const dotCharacter = props.character || '.';

    const [dots, setDots] = useState(dotCharacter);

    useEffect(() => {
        const interval = setInterval(() => {
            setDots(prevDots => {
                if (prevDots.length < 3) {
                    return prevDots + dotCharacter;
                }
                return dotCharacter;
            });
        }, dotInterval);
        return () => clearInterval(interval);
    }, [dotInterval, dotCharacter])

    return (
        <>
            {dots}
        </>
    );
}