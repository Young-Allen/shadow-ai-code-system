/**
 * 可视化编辑器工具类
 * 负责处理 iframe 通信和元素选择逻辑
 */

export interface SelectedElement {
  selector: string
  tagName: string
  className: string
  id: string
  textContent: string
  xpath: string
}

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private selectedElement: SelectedElement | null = null
  private onElementSelectedCallback: ((element: SelectedElement | null) => void) | null = null
  private scriptInjected = false

  /**
   * 初始化可视化编辑器
   */
  init(iframe: HTMLIFrameElement, onElementSelected: (element: SelectedElement | null) => void) {
    this.iframe = iframe
    this.onElementSelectedCallback = onElementSelected
    this.scriptInjected = false

    // 监听来自 iframe 的消息
    window.addEventListener('message', this.handleMessage.bind(this))

    // 等待 iframe 加载完成后注入脚本
    iframe.addEventListener('load', () => {
      console.log('iframe load 事件触发')
      this.scriptInjected = false
      // 延迟注入，确保 iframe 内容完全加载
      setTimeout(() => {
        this.injectScript()
      }, 800)
    })

    // 如果 iframe 已经加载完成，立即注入
    if (iframe.contentWindow && iframe.contentDocument?.readyState === 'complete') {
      console.log('iframe 已加载完成，立即注入脚本')
      setTimeout(() => {
        this.injectScript()
      }, 800)
    }
  }

  /**
   * 进入编辑模式
   */
  enterEditMode() {
    console.log('VisualEditor: 进入编辑模式')
    this.isEditMode = true
    
    // 确保脚本已注入
    if (!this.scriptInjected) {
      console.log('脚本未注入，尝试注入')
      this.injectScript()
      // 延迟发送消息，等待脚本加载
      setTimeout(() => {
        this.sendMessageToIframe({ type: 'ENTER_EDIT_MODE' })
      }, 500)
    } else {
      this.sendMessageToIframe({ type: 'ENTER_EDIT_MODE' })
    }
  }

  /**
   * 退出编辑模式
   */
  exitEditMode() {
    console.log('VisualEditor: 退出编辑模式')
    this.isEditMode = false
    this.selectedElement = null
    this.sendMessageToIframe({ type: 'EXIT_EDIT_MODE' })
    this.onElementSelectedCallback?.(null)
  }

  /**
   * 清除选中的元素
   */
  clearSelection() {
    console.log('VisualEditor: 清除选中')
    this.selectedElement = null
    this.sendMessageToIframe({ type: 'CLEAR_SELECTION' })
    this.onElementSelectedCallback?.(null)
  }

  /**
   * 获取当前选中的元素
   */
  getSelectedElement(): SelectedElement | null {
    return this.selectedElement
  }

  /**
   * 销毁编辑器
   */
  destroy() {
    window.removeEventListener('message', this.handleMessage.bind(this))
    this.iframe = null
    this.selectedElement = null
    this.onElementSelectedCallback = null
    this.scriptInjected = false
  }

  /**
   * 处理来自 iframe 的消息
   */
  private handleMessage(event: MessageEvent) {
    // 安全检查：确保消息来自同域
    if (!this.iframe || event.source !== this.iframe.contentWindow) {
      return
    }

    const { type, data } = event.data

    console.log('收到 iframe 消息:', type, data)

    switch (type) {
      case 'ELEMENT_SELECTED':
        this.selectedElement = data
        this.onElementSelectedCallback?.(data)
        break
      case 'ELEMENT_CLEARED':
        this.selectedElement = null
        this.onElementSelectedCallback?.(null)
        break
      case 'SCRIPT_LOADED':
        console.log('脚本加载完成')
        this.scriptInjected = true
        break
    }
  }

  /**
   * 向 iframe 发送消息
   */
  private sendMessageToIframe(message: Record<string, unknown>) {
    if (this.iframe && this.iframe.contentWindow) {
      console.log('发送消息到 iframe:', message)
      this.iframe.contentWindow.postMessage(message, '*')
    } else {
      console.warn('无法发送消息：iframe 或 contentWindow 不存在')
    }
  }

  /**
   * 向 iframe 注入可视化编辑脚本
   */
  private injectScript() {
    if (!this.iframe || !this.iframe.contentWindow) {
      console.warn('iframe 或 contentWindow 不存在')
      return
    }

    try {
      const iframeDoc = this.iframe.contentWindow.document
      
      // 检查文档是否准备好
      if (!iframeDoc || !iframeDoc.body) {
        console.warn('iframe 文档未准备好，延迟注入')
        setTimeout(() => this.injectScript(), 500)
        return
      }

      // 检查是否已经注入过脚本
      if (iframeDoc.getElementById('visual-editor-script')) {
        console.log('脚本已注入，跳过')
        this.scriptInjected = true
        return
      }

      console.log('开始注入可视化编辑脚本')

      const script = `
      (function() {
        // 标记脚本已加载
        if (window.__VISUAL_EDITOR_LOADED__) {
          console.log('[VisualEditor] 脚本已加载');
          return;
        }
        window.__VISUAL_EDITOR_LOADED__ = true;
        
        let isEditMode = false;
        let hoveredElement = null;
        let selectedElement = null;
        const HOVER_BORDER_COLOR = '#1890ff';
        const SELECTED_BORDER_COLOR = '#ff4d4f';
        const BORDER_WIDTH = '2px';

        console.log('[VisualEditor] 可视化编辑器脚本已注入');

        // 生成元素的 XPath
        function getXPath(element) {
          if (element.id) {
            return '//*[@id="' + element.id + '"]';
          }
          if (element === document.body) {
            return '/html/body';
          }
          let ix = 0;
          const siblings = element.parentNode ? element.parentNode.childNodes : [];
          for (let i = 0; i < siblings.length; i++) {
            const sibling = siblings[i];
            if (sibling === element) {
              const parentPath = element.parentNode ? getXPath(element.parentNode) : '';
              return parentPath + '/' + element.tagName.toLowerCase() + '[' + (ix + 1) + ']';
            }
            if (sibling.nodeType === 1 && sibling.tagName === element.tagName) {
              ix++;
            }
          }
          return '';
        }

        // 生成 CSS 选择器
        function getSelector(element) {
          if (element.id) {
            return '#' + element.id;
          }
          if (element.className && typeof element.className === 'string') {
            const classes = element.className.split(' ').filter(c => c.trim()).join('.');
            if (classes) {
              return element.tagName.toLowerCase() + '.' + classes;
            }
          }
          return element.tagName.toLowerCase();
        }

        // 获取元素信息
        function getElementInfo(element) {
          return {
            selector: getSelector(element),
            tagName: element.tagName,
            className: element.className || '',
            id: element.id || '',
            textContent: element.textContent ? element.textContent.substring(0, 100) : '',
            xpath: getXPath(element)
          };
        }

        // 设置元素边框
        function setElementBorder(element, color) {
          if (!element || element === document.body || element === document.documentElement) {
            return;
          }
          element.style.outline = BORDER_WIDTH + ' solid ' + color;
          element.style.outlineOffset = '-2px';
        }

        // 清除元素边框
        function clearElementBorder(element) {
          if (!element) return;
          element.style.outline = '';
          element.style.outlineOffset = '';
        }

        // 鼠标移入事件
        function handleMouseOver(e) {
          if (!isEditMode) return;
          e.stopPropagation();
          
          // 如果已经有选中的元素，不处理悬停
          if (selectedElement) return;

          const target = e.target;
          if (target === document.body || target === document.documentElement) {
            return;
          }

          if (hoveredElement && hoveredElement !== target) {
            clearElementBorder(hoveredElement);
          }

          hoveredElement = target;
          setElementBorder(hoveredElement, HOVER_BORDER_COLOR);
        }

        // 鼠标移出事件
        function handleMouseOut(e) {
          if (!isEditMode) return;
          e.stopPropagation();

          // 如果已经有选中的元素，不处理悬停
          if (selectedElement) return;

          if (hoveredElement) {
            clearElementBorder(hoveredElement);
            hoveredElement = null;
          }
        }

        // 点击事件
        function handleClick(e) {
          if (!isEditMode) return;
          e.preventDefault();
          e.stopPropagation();

          const target = e.target;
          if (target === document.body || target === document.documentElement) {
            return;
          }

          console.log('[VisualEditor] 点击元素:', target);

          // 清除之前的选中状态
          if (selectedElement) {
            clearElementBorder(selectedElement);
          }

          // 清除悬停状态
          if (hoveredElement) {
            clearElementBorder(hoveredElement);
            hoveredElement = null;
          }

          // 设置新的选中状态
          selectedElement = target;
          setElementBorder(selectedElement, SELECTED_BORDER_COLOR);

          // 发送选中信息到父窗口
          const elementInfo = getElementInfo(selectedElement);
          console.log('[VisualEditor] 发送元素信息到父窗口:', elementInfo);
          
          try {
            window.parent.postMessage({
              type: 'ELEMENT_SELECTED',
              data: elementInfo
            }, '*');
          } catch (err) {
            console.error('[VisualEditor] 发送消息失败:', err);
          }
        }

        // 进入编辑模式
        function enterEditMode() {
          console.log('[VisualEditor] 进入编辑模式');
          isEditMode = true;
          document.body.style.cursor = 'crosshair';
          document.addEventListener('mouseover', handleMouseOver, true);
          document.addEventListener('mouseout', handleMouseOut, true);
          document.addEventListener('click', handleClick, true);
        }

        // 退出编辑模式
        function exitEditMode() {
          console.log('[VisualEditor] 退出编辑模式');
          isEditMode = false;
          document.body.style.cursor = '';
          
          // 清除所有边框
          if (hoveredElement) {
            clearElementBorder(hoveredElement);
            hoveredElement = null;
          }
          if (selectedElement) {
            clearElementBorder(selectedElement);
            selectedElement = null;
          }

          // 移除事件监听
          document.removeEventListener('mouseover', handleMouseOver, true);
          document.removeEventListener('mouseout', handleMouseOut, true);
          document.removeEventListener('click', handleClick, true);
        }

        // 清除选中
        function clearSelection() {
          console.log('[VisualEditor] 清除选中');
          if (selectedElement) {
            clearElementBorder(selectedElement);
            selectedElement = null;
          }
          if (hoveredElement) {
            clearElementBorder(hoveredElement);
            hoveredElement = null;
          }
          try {
            window.parent.postMessage({
              type: 'ELEMENT_CLEARED'
            }, '*');
          } catch (err) {
            console.error('[VisualEditor] 发送消息失败:', err);
          }
        }

        // 监听来自父窗口的消息
        window.addEventListener('message', function(event) {
          console.log('[VisualEditor] 收到父窗口消息:', event.data);
          const { type } = event.data || {};
          
          switch (type) {
            case 'ENTER_EDIT_MODE':
              enterEditMode();
              break;
            case 'EXIT_EDIT_MODE':
              exitEditMode();
              break;
            case 'CLEAR_SELECTION':
              clearSelection();
              break;
          }
        });

        // 通知父窗口脚本已加载
        try {
          window.parent.postMessage({
            type: 'SCRIPT_LOADED'
          }, '*');
        } catch (err) {
          console.error('[VisualEditor] 发送脚本加载消息失败:', err);
        }

        console.log('[VisualEditor] 初始化完成');
      })();
    `

      const scriptElement = iframeDoc.createElement('script')
      scriptElement.id = 'visual-editor-script'
      scriptElement.textContent = script
      iframeDoc.body.appendChild(scriptElement)
      
      console.log('可视化编辑脚本注入成功')
      this.scriptInjected = true
    } catch (error) {
      console.error('注入脚本失败:', error)
      // 如果是跨域错误，延迟重试
      if (error instanceof DOMException) {
        console.log('可能是跨域问题，延迟重试')
        setTimeout(() => this.injectScript(), 1000)
      }
    }
  }
}
