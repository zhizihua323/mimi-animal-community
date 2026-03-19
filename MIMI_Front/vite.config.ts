import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';
// 引入 Node 内置的 path 模块（用于处理文件路径，无需额外安装）
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  build: {
    // 开发阶段启用源码映射（保留原配置）
    sourcemap: process.env.NODE_ENV === 'development',
  },
  plugins: [uni()], // 保留 uni 插件配置
  // 新增：配置路径别名，让 @ 指向 src 目录
  resolve: {
    alias: {
      // 键：别名符号（@）；值：src 目录的绝对路径
      '@': path.resolve(__dirname, 'src'),
    },
  },
});