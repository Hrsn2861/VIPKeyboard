package com.example.audiokeyboard.Utils;

import android.view.MotionEvent;

import com.elvishew.xlog.XLog;

public class GestureDetector {
    public enum Direction {
        UP(0), DOWN(1), LEFT(2), RIGHT(3),
        UP_THEN_DOWN(5), UP_THEN_LEFT(6), UP_THEN_RIGHT(7),
        DOWN_THEN_UP(8), DOWN_THEN_LEFT(10), DOWN_THEN_RIGHT(11),
        LEFT_THEN_UP(12), LEFT_THEN_DOWN(13), LEFT_THEN_RIGHT(15),
        RIGHT_THEN_UP(16), RIGHT_THEN_DOWN(17), RIGHT_THEN_LEFT(18);
        public final int value;

        Direction(int value) {
            this.value = value;
        }

        public static Direction getDirectionByValue(int value) {
            for (Direction dir : Direction.values()) {
                if (dir.value == value) {
                    return dir;
                }
            }
            return null;
        }
    }
    final float DISTANCE_THRESHOLD = 300;
    final float DISTANCE_LINE_THRESHOLD = 50; //在某个轴上滑动时，另一个轴上不能大于此距离
    final float DISTANCE_JITTOR = 10;
    final long TIME_THRESHOLD = 300;
    final int TRACK_POINTER_NUM = 5;
    private int fingerNum = 0;
    private float startPos[][] = new float [TRACK_POINTER_NUM][2];
    private float movePos[][] = new float[TRACK_POINTER_NUM][2];
    private float midPos[][] = new float[TRACK_POINTER_NUM][2];
    private long startTime = -1;
    private long midTime = -1;
    private void reset() {
        fingerNum = 0;
        hasDir = false;
        hasSecondDir = false;
        second_threshold = DISTANCE_JITTOR;
        startTime = -1;
        for (int i=0; i<TRACK_POINTER_NUM; i++) {
            startPos[i][0] = -1;
            startPos[i][1] = -1;
            midPos[i][0] = -1;
            midPos[i][1] = -1;
            movePos[i][0] = -1;
            movePos[i][1] = -1;
            firstDir[i] = null;
            secondDir[i] = null;
        }

    }
    private Direction firstDir[] = new Direction[TRACK_POINTER_NUM];
    private Direction secondDir[] = new Direction[TRACK_POINTER_NUM];
    private boolean hasDir = false;
    private boolean hasSecondDir = false;
    private float second_threshold = DISTANCE_JITTOR;
    public boolean onTouch(MotionEvent event) {
        XLog.tag("detector").i("startPos");
        XLog.tag("detector").i(startPos);
        XLog.tag("detector").i(midPos);
        XLog.tag("detector").i(movePos);
        XLog.tag("detector").i(debug(firstDir));
        XLog.tag("detector").i(debug(secondDir));
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int actionId = event.getActionIndex();
        float x = event.getX(actionId);
        float y = event.getY(actionId);
        long time = event.getEventTime();
        if (actionId >= TRACK_POINTER_NUM) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                reset();
                fingerNum += 1;
                if (fingerNum > 2) return true;
                startPos[actionId][0] = x;
                startPos[actionId][1] = y;
                startTime = event.getEventTime();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                fingerNum += 1;
                if (fingerNum > 2) return true;
                startPos[actionId][0] = x;
                startPos[actionId][1] = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int count = event.getPointerCount();
                for (int i = 0; i < count; i++) {
                    movePos[i][0] = event.getX(i);
                    movePos[i][1] = event.getY(i);

//                movePos[actionId][0] = x;
//                movePos[actionId][1] = y;
                    if (hasDir && time - midTime < TIME_THRESHOLD) {
                        if (Math.abs(movePos[actionId][0] - midPos[actionId][0]) > second_threshold &&
                                Math.abs(movePos[actionId][1] - midPos[actionId][1]) < DISTANCE_LINE_THRESHOLD) {
                            if (movePos[actionId][0] > midPos[actionId][0]) {
                                secondDir[actionId] = Direction.RIGHT;
                            } else {
                                secondDir[actionId] = Direction.LEFT;
                            }
                        } else if (Math.abs(movePos[actionId][1] - midPos[actionId][1]) > second_threshold &&
                                Math.abs(movePos[actionId][0] - midPos[actionId][0]) < DISTANCE_LINE_THRESHOLD) {
                            if (movePos[actionId][1] > midPos[actionId][1]) {
                                secondDir[actionId] = Direction.DOWN;
                            } else {
                                secondDir[actionId] = Direction.UP;
                            }
                        }
                        hasSecondDir = true;
                        if (secondDir[actionId] == firstDir[actionId]) {
                            midPos[actionId][0] = x;
                            midPos[actionId][1] = y;
                            midTime = time;
                            hasSecondDir = false;
                        }
                        if (hasSecondDir) {
                            second_threshold = DISTANCE_THRESHOLD;
                        }
                    }

                    if (!hasDir && time - startTime < TIME_THRESHOLD) {
                        if (Math.abs(movePos[actionId][0] - startPos[actionId][0]) > DISTANCE_THRESHOLD
                                && Math.abs(movePos[actionId][1] - startPos[actionId][1]) < DISTANCE_LINE_THRESHOLD) {
                            hasDir = true;
                            midTime = time;
                            midPos[actionId][0] = x;
                            midPos[actionId][1] = y;
                            if (movePos[actionId][0] > startPos[actionId][0]) {
                                firstDir[actionId] = Direction.RIGHT;
                            } else {
                                firstDir[actionId] = Direction.LEFT;
                            }
                        } else if (Math.abs(movePos[actionId][1] - startPos[actionId][1]) > DISTANCE_THRESHOLD
                                && Math.abs(movePos[actionId][0] - startPos[actionId][0]) < DISTANCE_LINE_THRESHOLD) {
                            hasDir = true;
                            midTime = time;
                            midPos[actionId][0] = x;
                            midPos[actionId][1] = y;
                            if (movePos[actionId][1] > startPos[actionId][1]) {
                                firstDir[actionId] = Direction.DOWN;
                            } else {
                                firstDir[actionId] = Direction.UP;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("***********" + (event.getEventTime() - startTime) + "************");
                if (hasDir) {
                    slideResponse(fingerNum, firstDir, secondDir);
                } else {
                    if (listener != null) {
                        XLog.tag("detector").i("ontap");

                        listener.onTap(event);
                    }
                }

                break;
            default:
                break;
        }
        return true;
    }
    public interface onGestureListener {
        public void onSwipe(Direction direction);
        public void on2FingerSwipe(Direction direction);
        public void onTap(MotionEvent event);
    }
    private onGestureListener listener = null;

    public GestureDetector(onGestureListener gestureListener) {
         this.listener = gestureListener;
    }

    private void slideResponse(int fingerNum, Direction[] direction1, Direction[] direction2) {
        XLog.tag("detector").i("slide response: fingerNum" + fingerNum + ",direction1" + debug(direction1) + ",direction2" + debug(direction2));
        if (listener != null) {
            if (fingerNum == 1) {
                listener.onSwipe(joinDirection(firstDir[0], secondDir[0]));
            } else if (fingerNum == 2) {
                listener.on2FingerSwipe(joinDirection(firstDir[0], secondDir[0]));
            }
        }
    }

    private Direction joinDirection(Direction d1, Direction d2) {
        if (d2 == null) return d1;
        if (d1 == d2) return d1;
        return Direction.getDirectionByValue((1+d1.value) * 4+d2.value);
    }

    private String debug(Direction[] dir) {
        String ret = "<";
        for (int i=0; i<dir.length; i++) {
            if (dir[i] != null) {
                ret += dir[i].toString() + ",";
            }
        }
        return ret;
    }


}
