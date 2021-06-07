FROM node:12.18.0-stretch-slim as node
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install
COPY . .
ARG configuration
RUN npm run build  -- --configuration $configuration
FROM nginx:1.17.5-alpine
COPY --from=node /usr/src/app/dist/frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY replace-backend-url.sh /
CMD ["sh", "replace-backend-url.sh"]
