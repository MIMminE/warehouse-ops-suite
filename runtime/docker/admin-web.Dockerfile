FROM node:22-alpine AS build
WORKDIR /workspace
RUN corepack enable
ARG VITE_API_BASE_URL=http://localhost:8080
ARG VITE_DPS_WEBSOCKET_URL=ws://localhost:4030/ws/dps
ARG VITE_PDF_RENDERER_URL=http://localhost:4050
ARG VITE_PRINT_AGENT_URL=http://localhost:4020
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_DPS_WEBSOCKET_URL=$VITE_DPS_WEBSOCKET_URL
ENV VITE_PDF_RENDERER_URL=$VITE_PDF_RENDERER_URL
ENV VITE_PRINT_AGENT_URL=$VITE_PRINT_AGENT_URL
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/admin-web/package.json apps/admin-web/package.json
COPY packages/shared-contracts/package.json packages/shared-contracts/package.json
RUN pnpm install --frozen-lockfile
COPY apps/admin-web apps/admin-web
COPY packages/shared-contracts packages/shared-contracts
RUN pnpm --filter @warehouse/admin-web build

FROM nginx:1.27-alpine
COPY --from=build /workspace/apps/admin-web/dist /usr/share/nginx/html
RUN printf 'ok\n' > /usr/share/nginx/html/health
EXPOSE 80
