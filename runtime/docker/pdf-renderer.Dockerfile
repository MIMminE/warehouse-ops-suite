FROM node:22-alpine AS build
WORKDIR /workspace
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY services/pdf-renderer/package.json services/pdf-renderer/package.json
RUN pnpm install --frozen-lockfile
COPY services/pdf-renderer services/pdf-renderer
RUN pnpm --filter @warehouse/pdf-renderer build

FROM mcr.microsoft.com/playwright:v1.49.1-jammy
WORKDIR /app
COPY --from=build /workspace/node_modules ./node_modules
COPY --from=build /workspace/services/pdf-renderer/package.json ./package.json
COPY --from=build /workspace/services/pdf-renderer/dist ./dist
EXPOSE 4050
CMD ["node", "dist/server.js"]
