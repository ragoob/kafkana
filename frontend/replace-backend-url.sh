find '/usr/share/nginx/html' -name '*.js' -exec sed -i -e 's,API_URL,'"$API_URL"',g' {} \;
find '/usr/share/nginx/html' -name '*.js' -exec sed -i -e 's,SOCKET_URL,'"$SOCKET_URL"',g' {} \;
nginx -g "daemon off;"