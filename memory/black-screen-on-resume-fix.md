name: black-screen-on-resume-fix
description: 修复 App 从后台回到前台时相机黑屏的问题
metadata:
  type: bugfix
  date: 2026-06-03
  priority: high
  status: completed

summary: |
  App 从后台回到前台时，相机预览出现黑屏。这是因为 CameraPresenter 在 onStop 生命周期时完全释放了相机（mCamera.release()），导致在 onResume 时无法自动恢复预览。
  
  **修复方案：**
  1. 移除了 onStop 时的相机释放操作，改为暂停预览
  2. 在 onResume 时自动恢复预览
  3. 添加了 resumePreview 方法用于手动恢复预览
  
  **关键改动：**
  - CameraPresenter.onStop(): 只暂停预览，不释放相机
  - CameraPresenter.onResume(): 自动恢复预览
  - CameraFrangment.onResume(): 恢复相机预览
  - CameraFrangment.onPause(): 暂停相机预览
  
  **效果：**
  - App 从后台回到前台时，相机预览会自动恢复
  - 不再需要重新申请权限
  - 预览无缝恢复
