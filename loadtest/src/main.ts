import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Options } from 'k6/options';
import {generateUserData} from "@/utils/helpers";

interface Environment {
    BASE_URL: string;
    API_TOKEN?: string;
}

const env: Environment = {
    BASE_URL: __ENV.BASE_URL || 'https://localhost:8443',
    API_TOKEN: __ENV.API_TOKEN,
};

export const options: Options = {
    stages: [
        { duration: '30s', target: 100 },  // 램프업 단계
        { duration: '1m', target: 100 },  // 지속 단계
        { duration: '1m', target: 200 },  // 부하 증가
        { duration: '1m', target: 200 },  // 고부하 지속
        { duration: '1m', target: 0 },    // 단계적 감소
    ],
    // stages: [
    //     { duration: '5s', target: 1 },
    // ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],  // 실패율 1% 미만
    },
};

const getHeaders = (): Record<string, string> => ({
    'Content-Type': 'application/json',
    'Authorization': env.API_TOKEN ? `Bearer ${env.API_TOKEN}` : '',
});

interface UserResponse {
    email: string;
    name: string;
}

export default function(): void {
    group('User Workflow', () => {
        const newUser = generateUserData()

        group('Create User', () => {
            const createRes = http.post(
                `${env.BASE_URL}/api/users`,
                JSON.stringify(newUser),
                { headers: getHeaders() }
            );

            check(createRes, {
                'user created': (r) => r.status === 200,
                'has email': (r) => JSON.parse(r.body as string).email !== undefined,
            });

            sleep(1);
        });

        let authHeaders: {} = {}

        group('Login', () => {
            const loginPayload = JSON.stringify({
                email: newUser.email,
                password: newUser.password,
            });

            const loginRes = http.post(
                `${env.BASE_URL}/api/auth/login`,
                loginPayload,
                { headers: getHeaders() }
            );

            check(loginRes, {
                'successful login': (r) => r.status === 200,
                'has access token': (r) => JSON.parse(r.body as string).token !== undefined,
            });

            const token = loginRes.json('token') as string;
            authHeaders = {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            };
        })

        sleep(1);

        group('List Users', () => {
            const listRes = http.get(
                `${env.BASE_URL}/api/users`,
                { headers: authHeaders }
            );

            check(listRes, {
                'users retrieved': (r) => r.status === 200,
                'users count > 0': (r) => {
                    try {
                        const responseBody = JSON.parse(r.body as string);
                        return responseBody && responseBody.length > 0;
                    } catch (e) {
                        console.error('JSON 파싱 오류:', e);
                        return false;
                    }
                },
            });

            let userEmails: string[] = [];

            try {
                const users = listRes.json('data') as unknown as UserResponse[];
                if (Array.isArray(users)) {
                    userEmails = users.map(user => user.email);
                }
            } catch (e) {
                console.error("User list parsing error: ", e);
            }

            if (userEmails.length <= 0) {
                return
            }

            const randomUserEmail = userEmails[Math.floor(Math.random() * userEmails.length)];

            group('Get User Details', () => {
                const userRes = http.get(
                    `${env.BASE_URL}/api/users/${randomUserEmail}`,
                    { headers: authHeaders }
                );

                check(userRes, {
                    'user found': (r) => r.status === 200,
                    'correct user returned': (r) => (r.json('email') as string) === randomUserEmail,
                });

                sleep(1);
            });

            sleep(1);
        });
    });
}