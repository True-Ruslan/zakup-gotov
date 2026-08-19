FROM node:24.18.1-bookworm-slim AS dependencies

WORKDIR /workspace
RUN npm install --global pnpm@11.4.0

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/web/package.json apps/web/package.json
COPY packages/api-client/package.json packages/api-client/package.json
RUN pnpm install --frozen-lockfile

FROM dependencies AS build

COPY apps/web apps/web
COPY packages/api-client packages/api-client
COPY openapi openapi

ENV NEXT_TELEMETRY_DISABLED=1
RUN pnpm --filter @zakup-gotov/api-client build \
    && pnpm --filter web build

FROM gcr.io/distroless/nodejs24-debian12:nonroot AS runtime

ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    HOSTNAME=0.0.0.0 \
    PORT=3000

WORKDIR /app

COPY --from=build --chown=65532:65532 /workspace/apps/web/.next/standalone ./
COPY --from=build --chown=65532:65532 /workspace/apps/web/.next/static /app/apps/web/.next/static

USER 65532
EXPOSE 3000

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=10 \
  CMD ["/nodejs/bin/node", "-e", "fetch('http://127.0.0.1:3000/').then(r => { if (!r.ok) process.exit(1) }).catch(() => process.exit(1))"]

CMD ["apps/web/server.js"]
