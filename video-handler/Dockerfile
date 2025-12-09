FROM golang:1.25.4-trixie as builder

WORKDIR /app

COPY video-handler ./video-handler
COPY video-handler/.env /app/.env

RUN go install github.com/kkdai/youtube/v2/cmd/youtubedr@latest && cd /app/video-handler && go build -o video-handler . \
    && mv /app/video-handler/video-handler /app/run && rm -rf /app/video-handler && mv /go/bin/youtubedr /app

FROM debian:stable-slim as runner

WORKDIR /app

COPY --from=builder /app/youtubedr /usr/local/bin
COPY --from=builder /app/.env /app/.env
COPY --from=builder /app/run /app/run

RUN mkdir /app/tmp-video-file && apt-get update -y && apt-get install -y --no-install-recommends ca-certificates \
    && apt-get install -y --no-install-recommends \
            ffmpeg \
        && rm -rf /var/lib/apt/lists/*


ENTRYPOINT ["sh", "-c", "/app/run"]
