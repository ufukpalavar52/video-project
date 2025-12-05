FROM node:21-alpine AS deps
WORKDIR /app

COPY video-processor-ui/package.json video-processor-ui/package-lock.json ./

RUN npm install

FROM node:21-alpine AS builder

WORKDIR /app

COPY video-processor-ui .
COPY --from=deps /app/node_modules ./node_modules

RUN npm run build

FROM node:21-alpine AS runner
WORKDIR /app

COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json

ENV NODE_ENV production
ENV PORT 3000

CMD ["npm", "start"]

EXPOSE 3000