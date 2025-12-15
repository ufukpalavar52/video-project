FROM golang:1.25.4-trixie AS depency

RUN go install github.com/air-verse/air@latest && go install github.com/kkdai/youtube/v2/cmd/youtubedr@latest

FROM debian:stable-slim AS runner

ENV PATH="/usr/local/go/bin:${PATH}"

WORKDIR /app

COPY --from=depency /go/bin/youtubedr /usr/local/bin
COPY --from=depency /go/bin/air /usr/local/bin

RUN apt-get update -y && apt-get install -y --no-install-recommends ca-certificates \
    && apt-get install -y --no-install-recommends ffmpeg wget \
    && wget https://go.dev/dl/go1.25.5.linux-amd64.tar.gz && rm -rf /usr/local/go \
        && tar -C /usr/local -xzf go1.25.5.linux-amd64.tar.gz && rm -rf go1.25.5.linux-amd64.tar.gz \
        && rm -rf /var/lib/apt/lists/*

CMD ["air"]