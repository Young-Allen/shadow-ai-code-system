import axios from 'axios'
import {message} from 'ant-design-vue'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

//创建Axios实例
const myAxios = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000,
    withCredentials: true,
})

//全局请求拦截器
myAxios.interceptors.request.use(config => {
    return config
}, error => {
    return Promise.reject(error)
})

//全局响应拦截器
myAxios.interceptors.response.use(response => {
    const {data} = response
    //未登录
    if(data.code === 40100){
        //不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转至用户登录页面
        if(
            !response.request.responseURL.includes('/user/get/login') &&
            !window.location.href.includes('/user/login')
        ){
            message.error('请先登录')
            window.location.href = `/user/login?redirect=${window.location.href}`
        }
    }
    return response
}, error => {
    return Promise.reject(error)
})

export default myAxios 