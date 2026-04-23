# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json yarn.lock* .yarnrc.yml* ./
RUN corepack enable && yarn install
COPY . .
RUN yarn build

# Serve stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/conf.d/nginx.conf.template
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
