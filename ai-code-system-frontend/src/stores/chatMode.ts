import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ChatMode = 'normal' | 'agent'

/**
 * 对话模式 Store：普通模式 / Agent 模式
 * 首页与对话页共用，保证选择一致
 */
export const useChatModeStore = defineStore('chatMode', () => {
  /** 是否为 Agent 模式，默认 false（普通模式） */
  const agentMode = ref(false)

  function setAgentMode(value: boolean) {
    agentMode.value = value
  }

  /** 当前模式：'normal' | 'agent' */
  function getMode(): ChatMode {
    return agentMode.value ? 'agent' : 'normal'
  }

  function setMode(mode: ChatMode) {
    agentMode.value = mode === 'agent'
  }

  return {
    agentMode,
    setAgentMode,
    getMode,
    setMode,
  }
})
