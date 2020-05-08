# AudioKeyboard

## 交互监听

- TouchDown：SpeakStop()；判定是否是调整范围内；如果是则进行初步调整；并且Speak；
- TouchMove：用户手指移动；SpeakStop()；
- TouchUp：SpeakStop()；用户手指抬起；Speak()；（可能会改变字母确定的概率）
- SlideRight：SpeakStop()；确认现在的输入（如果选中候选词则替换为候选词），开始下一个单词输入；
- SildeUp：获取当前列表下一个候选词，并且Speak；
- SlideLeft：SpeakStop()；BackSpace；Speak(removed_char)；
- SlideDown：获取当前列表上一个候选词，并且Speak；



## 类与各自的功能

### 画图相关

#### Utils.key

key类对应的是每一个按键的位置，会记录按键的初始位置、初始宽度、初始高度，当前位置、当前宽度、当前高度；

##### 相关参数设置

- tapRange：对应的是如果在这个位置内，则进行调整；
- MODE_INIT：对应的计算所有内容的时候按照初始位置计算；
- MODE_VIP：对应的是计算所有内容的时候按照当前位置计算；

##### 相关函数

- getDist：特定模式下，获取两个按键中心的距离；
- getBottom，getTop，getLeft，getRight：特定模式下，获取按键的四周位置；
- getBottomTap，getTopTap，getLeftTap，getRightTap：特定模式下，获取按键激活调整的范围；
- reset，resetX，resetY：相关参数的初始化；
- ContainTap：特定模式下，获得一个坐标关于按键的相对位置；左上，正上，右上，正左，正中，正又，左下，正下，右下为获取的9个相对位置；分别以123456789标号；



### Utils.KeyPos

KeyPos是核心的键盘View的参数设置；（不清楚java静态类的建立过程，不过对于初始位置相关内容的获取，用了静态函数的方法，可能后续会出锅）

##### 相关参数设置

- KeyboardWidth，KeyboardHeight：对应键盘的尺寸；
- bottomthreshold，topthreshold：如果超过这两个边界则不进行响应；
- partialWindowSize，wholeWindowSize，paddingtop：对应的是整个屏幕的一些尺寸；分别对应的是2和1和3；（这一部分没有实现通过系统的函数得到内容，应该不能适用于多个手机，只能用于虚拟机测试）
- minDistToStopShifting：如果两个位置相差的距离大于这个距离，则不进行调整；
- keypos：每一个key对应在Key数组的index；
- allchar：Key数组中所有的字符index，其中L后面有一个shift，需要特殊考虑；
- SCALINGNUM：在大小调整的时候，调整的个数；
- INIT_LAYOPUT，VIP_LAYOUT：用来进行Key操作；
- GETKEY_STRICT，GETKEY_LOOSE，getkey_mode：如果是strict，那么必须点击在按键内部才会识别；否则会找到距离最近的；
- ADJUST_BODILY，ADJUST_RESPECTIVELY，moveBodily：如果是BODILY，那所有的按键都跟着调整；否则之调整某一行的按键；（这个只会用到行调整中）；

<img src=".\assets\params.PNG" style="zoom:25%;" />



## 开发中的一些待解决问题

- 对于paddingTop的选取，不能在还未填充keys的时候得到View的Height；（1584为运行得到的值）
