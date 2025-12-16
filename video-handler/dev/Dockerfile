FROM golang:1.25.4-trixie AS depency

RUN go install github.com/air-verse/air@latest

FROM debian:stable-slim AS runner

ENV PATH="/usr/local/go/bin:${PATH}"
ENV DENO_INSTALL=/usr/local
WORKDIR /app

COPY --from=depency /go/bin/air /usr/local/bin

RUN apt-get update -y && apt-get install -y --no-install-recommends ca-certificates \
    && apt-get install -y --no-install-recommends ffmpeg wget curl unzip python3 \
    && wget https://go.dev/dl/go1.25.5.linux-amd64.tar.gz && rm -rf /usr/local/go \
        && tar -C /usr/local -xzf go1.25.5.linux-amd64.tar.gz && rm -rf go1.25.5.linux-amd64.tar.gz \
        && curl -fsSL https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux  -o /usr/local/bin/yt-dlp \
        && chmod a+rx /usr/local/bin/yt-dlp \
        && curl -fsSL https://deno.land/install.sh -o /tmp/install_deno.sh \
        && sh /tmp/install_deno.sh \
        && apt remove -y curl unzip wget && \
        apt autoremove -y && apt clean \
        && rm -rf /var/lib/apt/lists/*

CMD ["air"]