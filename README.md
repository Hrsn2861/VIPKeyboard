# AudioKeyboard

## 交互监听

- TouchDown：SpeakStop()；判定是否是调整范围内；如果是则进行初步调整；并且Speak；
- TouchMove：用户手指移动；SpeakStop()；
- TouchUp：SpeakStop()；用户手指抬起；Speak()；（可能会改变字母确定的概率）
- SlideRight：SpeakStop()；确认现在的输入（如果选中候选词则替换为候选词），开始下一个单词输入；
- SildeUp：获取当前列表下一个候选词，并且Speak；
- SlideLeft：SpeakStop()；BackSpace；Speak(removed_char)；
- SlideDown：获取当前列表 上一个候选词，并且Speak；
- DoubleSlideDown：清空所有的输入；



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

<img src=".\doc\assets\params.PNG" style="zoom:25%;" />

- minDistToStopShifting：如果两个位置相差的距离大于这个距离，则不进行调整；
- keypos：每一个key对应在Key数组的index；
- allchar：Key数组中所有的字符index，其中L后面有一个shift，需要特殊考虑；
- SCALINGNUM：在大小调整的时候，调整的个数；
- INIT_LAYOPUT，VIP_LAYOUT：用来进行Key操作；
- GETKEY_STRICT，GETKEY_LOOSE，getkey_mode：如果是strict，那么必须点击在按键内部才会识别；否则会找到距离最近的；
- ADJUST_BODILY，ADJUST_RESPECTIVELY，moveBodily：如果是BODILY，那所有的按键都跟着调整；否则之调整某一行的按键；（这个只会用到行调整中）；

##### 相关函数设置

- getKeyByPosition：特定模式下，获得给定位置的按键；
- shiftx_linear：对于行进行线性的变换；
- shifty_linear：对于列进行线性的变换；
- shift：将对应字符的按键按照给定的坐标调整；



#### Utils.Painters

里面放置了一些静态的画笔；



#### KeyboardView

一个画键盘的View；

##### 相关变量设置

keys：keys为按键数组；

---

### 识别相关

#### Utils.MotionPoint

记录某一次点击的位置和时间戳；

##### 相关函数

- getDx，getDy：获取x和y坐标上的距离；
- getDistance：获取两次点击之间的距离；



#### Utils.MotionSeperator

##### 相关参数设置

- FLING_LEFT，FLING_RIGHT，FLING_DOWN，FLING_UP，NORMAL_MOVE：区分的几种交互；
- MIN_FLING_VELOCITY：如果滑动的速度超过这个速度才算是FLING；

##### 相关函数设置

- getMotionType：输入两次点击之间之间的交互；

---

### 数据处理相关

#### Letter

记录的是每一个输入的字母，记录时间戳、字符和点的坐标；

#### DataRecorder

记录的是一个Letter的列表；

#### Word

从给定的字典中记录所有单词的string和出现的频率；



#### Predictor

进行相关概率的计算，找到现在的候选词以及下一个字符的计算；

##### 相关参数设置

- 计算Normal的sigmax = 52.7；sigmay = 45.8；

##### 相关函数设置

- getVIPMostPossibleKey：根据改进的算法，通过每个字符的不确定性预测；
- getVIPPossibilityByChar：根据改进的算法，计算语言模型；
- getMostPossibleKey：根据普通算法，忽视每个字符的不确定性；
- getPossibilityByChar：根据普通算法，计算语言模型；
- getMultiByPointAndKey：计算触摸模型；
- calDiffChar：输入每个键盘的中心，然后按照触摸模型计算；
- getCandidate：在普通算法下，得到当前的候选词列表；
- getVIPCandidate：在改进算法下，得到当前的候选词列表；

---

### 主函数

#### 相关参数设置

- minTimeGapThreshold：如果大于这个时间长度那么这个key是正确的；
- minMoveDistToCancelBestChar：如果超过这个移动距离就取消当前选中的最佳字符；（解决如果y轴调整极限的问题）
- inputText：当前输入字符串；
- startPoint，endPoint，currPoint：分别对应一次移动中的起始点和结束点，currPoint用来跟踪；
- skipUpdetect：如果检测到键盘调整，则在抬起的时候不进行判断当前点击位置的字符；
- currMode：现在是否调整键盘位置；
- currCandidateIndex：当前候选词的index，如果是-1则不选择候选词；
- maxChnCandidateLength：获取最多的中文候选词长度；如果长度不剪则会出现卡顿；
- isFirstCharCertain：所有输入的第一个字符是不是确认的；

#### 相关函数设置

- appendText：当前输入框内加入内容；
- deleteLast：删除输入框和recorder的最后的字符；
- refresh，refreshCandidate，refreshCurrCandidate：刷新键盘布局，刷新当前候选词列表，刷新当前候选词；



## 待解决问题

1. 现在需要搞定，如何对于中文的候选词进行排序；肯定是拼音和汉字混合排序，但是如何进行插入以及如何计算是一个问题；