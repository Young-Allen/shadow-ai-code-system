/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_APP_DEPLOY_DOMAIN: string
  readonly VITE_APP_PREVIEW_DOMAIN: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
