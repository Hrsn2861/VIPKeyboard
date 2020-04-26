# AudioKeyboard

## 交互监听

键盘内交互：

- TouchDown：SpeakStop()；判定是否是调整范围内；如果是则进行初步调整；并且Speak；
- TouchMove：用户手指移动；SpeakStop()；
- TouchUp：SpeakStop()；用户手指抬起；Speak()；（可能会改变字母确定的概率）
- SlideUp or SlideRight：SpeakStop()；确认当前输入，给出候选表；Speak(most_possible_word)；
- SlideLeft：SpeakStop()；BackSpace；Speak(removed_char)；

键盘外的交互：

- SildeRight：同上；
- SlideUp or SlideDown：候选词选择？；
- SlideLeft：同上；
- ？

## 开发中的一些待解决问题

- 对于paddingTop的选取，不能在还未填充keys的时候得到View的Height；（1584为运行得到的值）

## 一些变量阐释

### 界面尺寸

因为计算的时候 MotionEvent event 的 event.getY() 是关于整个界面的；

而一开始初始化的时候是针对每个 key 是不包括上面AudioKeyboard部分的；（因为要放到屏幕下方）

于是在 onTouchEvent() 中使用相对界面的尺寸；

final float paddingTop = 1584 - keyboardHeight;     // 对应的是 2

final float headerHeight = 210；                    // 对应的是 4

final float partialwindowSize = 1584；              // 对应的是 2

final float wholewindowSize = 1794；                // 对应的是 1

<img src=".\assets\params.PNG" style="zoom:25%;" />
