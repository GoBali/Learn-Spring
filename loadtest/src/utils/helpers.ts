export function randomItem<T>(array: T[]): T {
    return array[Math.floor(Math.random() * array.length)];
}

export function randomBetween(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function randomString(length: number = 10): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

export function logResponse(res: any, label: string = 'Response'): void {
    console.log(`--- ${label} ---`);
    console.log(`Status: ${res.status}`);
    console.log(`Body: ${res.body.substring(0, 100)}...`);
    console.log('-----------------');
}

interface UserData {
    name: string;
    email: string;
    password: string;
}

function uuidv4(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function getFormattedDate(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}${month}${day}`;
}

function generateRealisticEmail(): string {
    const firstNames = ['john', 'jane', 'michael', 'sarah', 'david', 'emily', 'robert', 'olivia', 'william', 'emma',
        'james', 'sophia', 'benjamin', 'isabella', 'lucas', 'mia', 'henry', 'charlotte', 'alexander', 'amelia',
        'minsu', 'jiwon', 'younghee', 'sungmin', 'jihye', 'junho', 'seoyeon', 'dongwook', 'jiyoung'];

    const lastNames = ['smith', 'johnson', 'brown', 'taylor', 'miller', 'wilson', 'moore', 'anderson', 'thomas', 'jackson',
        'white', 'harris', 'martin', 'thompson', 'garcia', 'martinez', 'robinson', 'clark', 'rodriguez', 'lewis',
        'kim', 'lee', 'park', 'jung', 'choi'];

    const domains = ['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 'naver.com', 'daum.net', 'icloud.com',
        'protonmail.com', 'mail.com', 'zoho.com', 'aol.com', 'yandex.com', 'kakao.com'];

    const firstName = firstNames[Math.floor(Math.random() * firstNames.length)];
    const lastName = lastNames[Math.floor(Math.random() * lastNames.length)];
    const domain = domains[Math.floor(Math.random() * domains.length)];

    const emailFormats = [
        () => `${firstName}${lastName}@${domain}`,
        () => `${firstName}.${lastName}@${domain}`,
        () => `${firstName}${lastName}${Math.floor(Math.random() * 999)}@${domain}`,
        () => `${firstName}_${lastName}@${domain}`,
        () => `${lastName}.${firstName}@${domain}`,
        () => `${firstName[0]}${lastName}@${domain}`,
        () => `${firstName}${lastName[0]}@${domain}`,
    ];

    const randomFormat = emailFormats[Math.floor(Math.random() * emailFormats.length)];
    return randomFormat();
}

export function generateUserData(): UserData {
    const datePrefix = getFormattedDate();
    const uuidSuffix = uuidv4().substring(0, 8);

    return {
        name: `Test User ${datePrefix}-${uuidSuffix}`,
        email: generateRealisticEmail(),
        password: `Pw${randomString(8)}!${Math.floor(Math.random() * 100)}`,
    };
}
