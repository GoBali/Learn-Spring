export K6_WEB_DASHBOARD=true
export K6_WEB_DASHBOARD_PORT=5665
export K6_WEB_DASHBOARD_OPEN=true
export K6_BROWSER_REPORT=true

npm run build && k6 run dist/test.js -e BASE_URL="https://localhost:8443"