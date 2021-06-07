find '/usr/share/nginx/html' -name '*.js' -exec sed -i -e 's,API_URL,'"$API_URL"',g' {} \;
nginx -g "daemon off;"