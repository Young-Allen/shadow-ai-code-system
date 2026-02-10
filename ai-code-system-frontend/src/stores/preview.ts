import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 文件信息接口
 */
export interface FileInfo {
  content: string
  language: string
}

/**
 * 选中文件接口
 */
export interface SelectedFile {
  path: string
  content: string
  language: string
}

/**
 * 视图模式类型
 */
export type ViewMode = 'code' | 'preview'

/**
 * Preview Store - 管理预览面板的状态
 */
export const usePreviewStore = defineStore('preview', () => {
  // ========== State ==========
  
  /**
   * 当前视图模式：'code' 显示代码，'preview' 显示网页预览
   */
  const viewMode = ref<ViewMode>('preview')

  /**
   * 当前选中的文件
   */
  const selectedFile = ref<SelectedFile | null>(null)

  /**
   * 文件映射表，缓存所有解析的文件
   */
  const fileMap = ref<Map<string, FileInfo>>(new Map())

  // ========== Getters ==========

  /**
   * 是否有选中的文件
   */
  const hasSelectedFile = computed(() => selectedFile.value !== null)

  /**
   * 当前文件的编程语言
   */
  const currentFileLanguage = computed(() => selectedFile.value?.language || 'plaintext')

  // ========== Actions ==========

  /**
   * 设置视图模式
   * @param mode 视图模式：'code' 或 'preview'
   */
  function setViewMode(mode: ViewMode) {
    viewMode.value = mode
  }

  /**
   * 选中文件并切换到代码视图
   * @param path 文件路径
   * @param content 文件内容
   * @param language 编程语言
   */
  function selectFile(path: string, content: string, language: string) {
    selectedFile.value = {
      path,
      content,
      language,
    }
    // 自动切换到代码视图
    viewMode.value = 'code'
  }

  /**
   * 添加文件到文件映射表
   * @param path 文件路径
   * @param content 文件内容
   * @param language 编程语言
   */
  function addFile(path: string, content: string, language: string) {
    fileMap.value.set(path, {
      content,
      language,
    })
  }

  /**
   * 清除选中的文件
   */
  function clearSelection() {
    selectedFile.value = null
  }

  return {
    // State
    viewMode,
    selectedFile,
    fileMap,
    // Getters
    hasSelectedFile,
    currentFileLanguage,
    // Actions
    setViewMode,
    selectFile,
    addFile,
    clearSelection,
  }
})
