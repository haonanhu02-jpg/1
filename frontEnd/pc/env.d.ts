/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}

declare module '*.js' {
  const value: any
  export default value
  export const get: any
  export const post: any
  export const put: any
  export const delt: any
}

declare module '@/utils/*' {
  const value: any
  export default value
  export const get: any
  export const post: any
  export const put: any
  export const delt: any
}

declare module '@/api/*' {
  const value: any
  export default value
}

declare module '@/stores' {
  const stores: any
  export const store: any
  export default stores
}

interface Window {
  $app: any
  sysConfig: any
}

declare module 'axios' {
  export interface AxiosInstance {
    get(url: string, params?: any, config?: any): Promise<any>
    post(url: string, data?: any, config?: any): Promise<any>
    put(url: string, data?: any, config?: any): Promise<any>
    delt(url: string, params?: any, config?: any): Promise<any>
    del(url: string, params?: any, config?: any): Promise<any>
  }

  export interface AxiosResponse<T = any> {
    code?: any
    count?: any
    msg?: any
    data: T
  }
}
