/**
 * 工具调用消息解析器
 * 用于解析 AI 返回的工具调用消息，识别文件操作并提取文件信息
 */

/**
 * 工具调用消息接口
 */
export interface ToolCallMessage {
  type: 'tool_call'
  toolName: string // 工具名称，如 "fsWrite", "strReplace"
  operation: string // 操作类型，如 "write", "replace", "delete"
  filePath: string // 文件路径
  fileContent?: string // 文件内容（如果有）
  language?: string // 编程语言（从文件扩展名推断）
}

/**
 * 转义正则表达式特殊字符
 * @param str 需要转义的字符串
 * @returns 转义后的字符串
 */
function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 从文件路径推断编程语言
 * @param filePath 文件路径
 * @returns 编程语言标识，如 'vue', 'typescript', 'javascript' 等
 */
export function inferLanguageFromPath(filePath: string): string {
  const ext = filePath.split('.').pop()?.toLowerCase()

  const languageMap: Record<string, string> = {
    vue: 'vue',
    ts: 'typescript',
    tsx: 'typescript',
    js: 'javascript',
    jsx: 'javascript',
    json: 'json',
    html: 'html',
    css: 'css',
    scss: 'scss',
    less: 'less',
    md: 'markdown',
    py: 'python',
    java: 'java',
    go: 'go',
    rs: 'rust',
    c: 'c',
    cpp: 'cpp',
    h: 'c',
    hpp: 'cpp',
    sh: 'bash',
    yaml: 'yaml',
    yml: 'yaml',
    xml: 'xml',
    sql: 'sql',
    php: 'php',
    rb: 'ruby',
    swift: 'swift',
    kt: 'kotlin',
    dart: 'dart',
  }

  return languageMap[ext || ''] || 'plaintext'
}

/**
 * 解析工具调用消息
 * 支持 Markdown 格式的工具调用解析，提取文件路径和代码块内容
 * 
 * @param content 消息内容
 * @returns 工具调用消息数组
 * 
 * @example
 * // Markdown 格式示例：
 * // [工具调用] 写入文件 src/App.vue
 * // ```vue
 * // <template>
 * //   <div>Hello</div>
 * // </template>
 * // ```
 */
export function parseToolCalls(content: string): ToolCallMessage[] {
  const toolCalls: ToolCallMessage[] = []

  try {
    // 正则表达式匹配 [工具调用] 写入文件 <文件路径>
    const toolCallRegex = /\[工具调用\]\s*写入文件\s+([^\s\n]+)/g
    let match

    while ((match = toolCallRegex.exec(content)) !== null) {
      const filePath = match[1]
      const language = inferLanguageFromPath(filePath)

      // 尝试提取代码块内容
      // 匹配格式：[工具调用] 写入文件 <文件路径> 后面跟着的代码块
      const codeBlockRegex = new RegExp(
        `\\[工具调用\\]\\s*写入文件\\s+${escapeRegex(filePath)}\\s*\`\`\`[^\\n]*\\n([\\s\\S]*?)\`\`\``,
        'i'
      )
      const codeMatch = content.match(codeBlockRegex)
      const fileContent = codeMatch ? codeMatch[1].trim() : ''

      toolCalls.push({
        type: 'tool_call',
        toolName: 'fsWrite',
        operation: 'write',
        filePath,
        fileContent,
        language,
      })
    }
  } catch (error) {
    // 解析失败时记录警告，但不抛出错误
    console.warn('解析工具调用失败:', error)
  }

  return toolCalls
}
