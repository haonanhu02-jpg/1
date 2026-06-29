// 环境变量
const envs = {
  development: {
    DOMAIN: 'http://127.0.0.1:8085',
    BASE_URL: '/tools/',
    BASE_API: 'http://127.0.0.1:8085',
  },
  test: {
    DOMAIN: 'https://show.iyque.cn',
    BASE_URL: '/tools/',
    BASE_API: 'https://show.iyque.cn/iyque',
  },
  production: {
    DOMAIN: 'https://iyque.cn',
    BASE_URL: '/tools/',
    BASE_API: '/api',
  },
}

type EnvMode = keyof typeof envs

type BrowserRuntime = typeof globalThis & {
  document?: unknown
  window?: {
    location?: {
      origin?: string
    }
  }
}

const runtimeGlobal = globalThis as BrowserRuntime

function isEnvMode(mode?: string): mode is EnvMode {
  return !!mode && mode in envs
}

// Vite 配置在 Node 环境执行，浏览器运行时才通过当前域名反查环境。
const processMode = isEnvMode(process.env.VUE_APP_ENV) ? process.env.VUE_APP_ENV : undefined
const domainMode = (Object.keys(envs) as EnvMode[]).find(
  (e) => envs[e].DOMAIN === runtimeGlobal.window?.location?.origin,
)
const mode: EnvMode =
  (process.env.NODE_ENV == 'development' || !runtimeGlobal.document ? processMode : domainMode) ??
  processMode ??
  'development'

export const env = { ...envs[mode], ENV: mode }

// 系统常量配置
export const common = {
  // 品牌文案替换：系统简称统一展示为英科弹簧
  SYSTEM_NAME: '英科弹簧', // 系统简称
  SYSTEM_SLOGAN:
    '英科弹簧SCRM-企业微信私域运营平台。', // 系统标语
  COPYRIGHT: 'Copyright © 2022-2025 英科弹簧 All Rights Reserved.', // 版权信息
  LOGO: env.BASE_URL + 'static/logo.png', // 深色logo
}
