FROM golang:1.25.4-trixie AS builder

WORKDIR /app

COPY video-handler ./video-handler
COPY video-handler/.env /app/.env

RUN cd /app/video-handler && go build -o video-handler . \
    && mv /app/video-handler/video-handler /app/run && rm -rf /app/video-handler

FROM debian:stable-slim AS runner
ENV DENO_INSTALL=/usr/local

WORKDIR /app

COPY --from=builder /app/.env /app/.env
COPY --from=builder /app/run /app/run

RUN mkdir /app/tmp-video-file && apt-get update -y && apt-get install -y --no-install-recommends ca-certificates \
    && apt-get install -y --no-install-recommends ffmpeg curl unzip python3 \
    && curl -fsSL https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp \
    && curl -fsSL https://deno.land/install.sh -o /tmp/install_deno.sh \
    && sh /tmp/install_deno.sh \
    && apt remove -y curl unzip && \
    apt autoremove -y && apt clean \
    && rm -rf /var/lib/apt/lists/* && rm -rf /tmp/install_deno.sh


ENTRYPOINT ["sh", "-c", "/app/run"]
