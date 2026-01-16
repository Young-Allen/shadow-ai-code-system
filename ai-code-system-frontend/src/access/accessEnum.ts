/**
 * 权限定义
 */
const ACCESS_ENUM = {
    NOT_LOGIN: 'notLogin',
    USER: 'user',
    ADMIN: 'admin',
  } as const
  
  export default ACCESS_ENUM
  export type AccessType = (typeof ACCESS_ENUM)[keyof typeof ACCESS_ENUM]
  