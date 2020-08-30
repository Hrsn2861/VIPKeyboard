package com.example.audiokeyboard.Utils;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GestureDetector {
    public static boolean supportDoubleTap = false;
    public enum Direction {
        UP(0), DOWN(1), LEFT(2), RIGHT(3),
        UP_THEN_DOWN(5), UP_THEN_LEFT(6), UP_THEN_RIGHT(7),
        DOWN_THEN_UP(8), DOWN_THEN_LEFT(10), DOWN_THEN_RIGHT(11),
        LEFT_THEN_UP(12), LEFT_THEN_DOWN(13), LEFT_THEN_RIGHT(15),
        RIGHT_THEN_UP(16), RIGHT_THEN_DOWN(17), RIGHT_THEN_LEFT(18),
        UNKNOWN(19);
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

    private void generateTrajectory(List<PointF> list, float[] points) {
        int num = 20;
        for (int i=0; i<points.length/2 - 1; i++) {
            float deltax = points[(i+1)*2] - points[i*2];
            float deltay = points[(i+1)*2+1] - points[i*2+1];
            for (int j=0; j<num; j++) {
                list.add(new PointF(points[i * 2] + deltax *j / num, points[i*2+1]+deltay * j / num));
            }
        }
    }

    List<List<PointF>> templates;
    private void createTemplates() {
        templates = new ArrayList<>();
        for (int i=0; i<19; i++) {
            templates.add(new ArrayList<PointF>());
        }
        float[] points = new float[] {0, 1, 0, 0.5f, 0, 0};//0 up
        for (int i=0; i<3; i++) {
            templates.get(0).add(new PointF(points[i*2], points[i*2+1]));
        }
        points = new float[] {0, 0, 0, 0.5f, 0, 1};//1 down
        for (int i=0; i<3; i++) {
            templates.get(1).add(new PointF(points[i*2], points[i*2+1]));
        }
        points = new float[] {1, 0, 0.5f, 0, 0, 0};//2 left
        for (int i=0; i<3; i++) {
            templates.get(2).add(new PointF(points[i*2], points[i*2+1]));
        }
        points = new float[] {0, 0, 0.5f, 0, 1, 0};//3 right
        for (int i=0; i<3; i++) {
            templates.get(3).add(new PointF(points[i*2], points[i*2+1]));
        }
//        points = new float[] {0, 1, 0, 0, 0, 1};//5 up then down
//        for (int i=0; i<3; i++) {
//            templates.get(5).add(new PointF(points[i*2], points[i*2+1]));
//        }
    }




    final float DISTANCE_THRESHOLD = 20;
    final float DISTANCE_LINE_THRESHOLD = 200; //在某个轴上滑动时，另一个轴上不能大于此距离
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
    private void cancelTaps() {
        //TODO: cancel long press and taps
    }
    private VelocityTracker velocityTracker;
    private final float DISTANCE_SQUARE_THRESHOLD = 8000;
    private final double TOTAL_SPEED_THRESHOLD = 0.3;
    private final int maxFlingVelocity = 8000;
    private final int minFlingVelocity = 500;
    private final int DOUBLE_TAP_TIMEOUT = 300;
    private final int DOUBLE_TAP_MIN_TIME = 40;
    private final int DOUBLE_TAP_SLOP = 100;
    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;
    private class GestureHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SHOW_PRESS:
                    //TODO:
                    break;
                case LONG_PRESS:
                    //TODO:
                    break;
                case TAP:
                    if (listener != null) {
                        if (!stillDown) {
                            //TODO: single tap confirmed
                        } else {
                            deferConfirmSingleTap = true;
                        }
                    }
            }
            super.handleMessage(msg);
        }
    }
    private boolean stillDown;
    private boolean deferConfirmSingleTap;
    private boolean inLongPress;
    private MotionEvent currentDownEvent;
    private MotionEvent currentMotionEvent;
    private MotionEvent previousUpEvent;
    private boolean isDoubleTapping;
    private GestureHandler handler;
    private int maxPointerNum;

    private double dist(PointF p1, PointF p2) {
        return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
    }

    private double dtw(List<PointF> s1, List<PointF> s2) {
        int m = s1.size();
        int n = s2.size();
        double[][] f = new double[m][n];
        for (int i=0; i<m; i++) {
            for (int j=0; j<n; j++) {
                f[i][j] = 2*(m+n);
            }
        }
        f[0][0] =dist(s1.get(0), s2.get(0));
        for (int i=0; i<m; i++) {
            for (int j=0; j<n; j++) {
                if (i == 0 && j == 0) continue;
                double temp = 2*(m+n);
                if (i > 0) {
                    if (f[i-1][j] < temp) temp = f[i-1][j];
                }
                if (j > 0) {
                    if (f[i][j-1] < temp) temp = f[i][j-1];
                }
                if (i > 0 && j > 0) {
                   if (f[i-1][j-1] < temp) temp = f[i-1][j-1];
                }
                f[i][j] = temp + dist(s1.get(i), s2.get(j));
            }
        }
        return f[m-1][n-1];
    }

    private List<PointF> normalizePoints(List<PointF> list) {
        if (list.size() < 3) return list;
        float minx = 5000;
        float maxx = 0;
        float miny = 5000;
        float maxy = 0;
        for (PointF p: list) {
            if (p.x < minx) minx = p.x;
            if (p.x > maxx) maxx = p.x;
            if (p.y < miny) miny = p.y;
            if (p.y > maxy) maxy = p.y;
        }
        float scale = maxx - minx;
        if (maxy - miny > scale) scale = maxy - miny;
        List<PointF> ret = new ArrayList<>();
        for (int i=0; i< list.size(); i++) {
            ret.add(new PointF((list.get(i).x - minx) / scale, (list.get(i).y - miny) / scale));
        }
        return ret;
    }
    private Direction recognizeOneDirectionBySpeed(List<MotionEvent> events) {
        VelocityTracker v = VelocityTracker.obtain();
        for (MotionEvent e : events) {
           // XLog.tag("rec data").i("%d,%s,%f,%f", e.getEventTime(), MotionEvent.actionToString(e.getAction()), e.getX(), e.getY());
            v.addMovement(e);
        }
        int downId = currentDownEvent.getPointerId(currentDownEvent.getActionIndex());
        v.computeCurrentVelocity(1000, maxFlingVelocity);
        float vx = v.getXVelocity(downId);
        float vy = v.getYVelocity(downId);
        //XLog.tag("recognize").d("velocity x:%f, y: %f", vx, vy);
        if (Math.abs(vx) > minFlingVelocity || Math.abs(vy) > minFlingVelocity) {
            if (Math.abs(vx) > Math.abs(vy)) {
                return vx > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                return vy > 0 ? Direction.DOWN: Direction.UP;
            }
        }
        return Direction.UNKNOWN;
    }

    private Direction recognizeOneDirection(List<PointF> points) {
        int ret = 0;
        double value = 1000000;
        List<PointF> norm = normalizePoints(points);
//        for (int i=0; i<norm.size(); i++) {
//            XLog.tag("NORMALIZED").d("(%.3f,%.3f)", norm.get(i).x, norm.get(i).y);
//        }
        for (int i=0; i<4; i++) {

            double d = dtw(templates.get(i), norm);
            if (d <value) {
                value = d;
                ret = i;
            }
            //System.out.println("calculate direction"+ Direction.getDirectionByValue(i).toString() + d);
        }
        return Direction.getDirectionByValue(ret);
    }
    private Direction recognizeSwipeDirection(List<PointF> points) {
        if (points.size() < 10) return recognizeOneDirection(points);
        Direction dir1 = recognizeOneDirection(points.subList(0, (int)(points.size() / 3)));
        Direction dir2 = recognizeOneDirection(points.subList((int)(points.size() *2/3), points.size()));
        if (dir1 != dir2) return joinDirection(dir1, dir2);
        Direction d = recognizeOneDirection(points);
        if (dir1 != d) return Direction.UNKNOWN;
        return dir1;
    }
    private Direction recognizeSwipeDirectionBySpeed(List<MotionEvent> points) {
        if (points.size() < 15) return recognizeOneDirectionBySpeed(points);
        Direction dir1 = recognizeOneDirectionBySpeed(points.subList(0, (int)(points.size() / 3)));
        Direction dir2 = recognizeOneDirectionBySpeed(points.subList((int)(points.size() *2/3), points.size()));
       // XLog.tag("recognize").d("d1: %s, d2: %s", dir1.name(), dir2.name());
        if (dir1 != dir2) return joinDirection(dir1, dir2);
        Direction d = recognizeOneDirectionBySpeed(points);
       // XLog.tag("recognize").d("d1: %s, d2: %s, d: %s", dir1.name(), dir2.name(), d.name());
        if (dir1 != d) return Direction.UNKNOWN;
        return dir1;
    }

    public boolean isPotentialSwipe() {
        if (currentDownEvent == null) return false;
        final VelocityTracker vt = velocityTracker;
        final int pointerId = currentDownEvent.getPointerId(0);
        vt.computeCurrentVelocity(1000, maxFlingVelocity);
        final float vx = vt.getXVelocity(pointerId);
        final float vy = vt.getYVelocity(pointerId);
        return (Math.abs(vx) > minFlingVelocity || Math.abs(vy) > minFlingVelocity);
    }


    private boolean isConsideredDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false;
        }
        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return (deltaX * deltaX + deltaY * deltaY < DOUBLE_TAP_SLOP * DOUBLE_TAP_SLOP);
    }
    private double disSquare(float x1, float y1, float x2, float y2) {
        return (x1 - x2)*(x1 - x2) + (y1-y2)*(y1-y2);
    }
    List<PointF> points;
    List<MotionEvent> events;
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        final int pointerCount = event.getPointerCount();
        if (currentDownEvent != null) {
            int downId = currentDownEvent.getPointerId(currentDownEvent.getActionIndex());
            for (int i = 0; i < pointerCount; i++) {
                int id = event.getPointerId(i);
                if (id == downId) {
                    points.add(new PointF(event.getX(), event.getY()));
                    events.add(MotionEvent.obtain(event));
                    break;
                }
            }
        }
        if (pointerCount > maxPointerNum) maxPointerNum = pointerCount;
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                //cancel tap and long press
                cancelTaps();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
                final int upIndex = event.getActionIndex();
                final int id = event.getPointerId(upIndex);
                final float xv = velocityTracker.getXVelocity(id);
                final float yv = velocityTracker.getYVelocity(id);
                for (int i = 0; i<pointerCount; i++) {
                    if (i == upIndex) continue;
                    final int id2 = event.getPointerId(i);
                    float dot = velocityTracker.getXVelocity(id2) * xv + velocityTracker.getYVelocity(id2) * yv;
                    if (dot < 0) {
                        velocityTracker.clear();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                //handle double tap:
                if (supportDoubleTap) {
                    boolean hadTapMessage = handler.hasMessages(TAP);
                    if (hadTapMessage) handler.removeMessages(TAP);
                    if ((currentDownEvent != null) && (previousUpEvent != null) && hadTapMessage
                            && isConsideredDoubleTap(currentDownEvent, previousUpEvent, event)) {
                        isDoubleTapping = true;
                        //TODO: give feedback on double tap down event
                    } else {
                        handler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT);
                    }
                }

                if (currentDownEvent != null) {
                    currentDownEvent.recycle();
                }
                points.clear();
                events.clear();
                currentDownEvent = MotionEvent.obtain(event);
                if (currentDownEvent != null) {
                    int downId = currentDownEvent.getPointerId(currentDownEvent.getActionIndex());
                    for (int i = 0; i < pointerCount; i++) {
                        int iid = event.getPointerId(i);
                        if (iid == downId) {
                            points.add(new PointF(event.getX(), event.getY()));
                            events.add(MotionEvent.obtain(event));
                            break;
                        }
                    }
                }
                stillDown = true;
                inLongPress = false;
                deferConfirmSingleTap = false;
                //
                break;
            case MotionEvent.ACTION_MOVE:
                if (inLongPress) break;
                if (isDoubleTapping) {
                    //TODO: give some callback feedback
                }
                break;
            case MotionEvent.ACTION_UP:
                stillDown = false;
                MotionEvent currentUpEvent = MotionEvent.obtain(event);
                if (isDoubleTapping) {
                    handled |= listener.onDoubleTap(currentDownEvent);
                } else if (inLongPress) {
                    //TODO: support for long press
                } else { // try to recognize swipe
                    final VelocityTracker vt = velocityTracker;
                    final int pointerId = event.getPointerId(0);
                    vt.computeCurrentVelocity(1000, maxFlingVelocity);
                    final float vx = vt.getXVelocity(pointerId);
                    final float vy = vt.getYVelocity(pointerId);
                    //XLog.tag("ontouchevent").d("velocity x:%f, y: %f", vx, vy);
                    float x1 = currentDownEvent.getX();
                    float x2 = event.getX();
                    float y1 = currentDownEvent.getY();
                    float y2 = event.getY();
                    double disdis = disSquare(x1, y1, x2, y2);
                    long eventtime = event.getEventTime() - currentDownEvent.getEventTime();
                    double totalspeed = Math.sqrt(disdis) / eventtime;
                    System.out.println("total speed" + totalspeed);


                    if ((Math.abs(vx) > minFlingVelocity || Math.abs(vy) > minFlingVelocity) && (disdis > DISTANCE_SQUARE_THRESHOLD) && (totalspeed > TOTAL_SPEED_THRESHOLD)) {

                        System.out.println(disdis);
                        Logger.i("swipe velocity:" + vx +","+ vy + "with" + maxPointerNum + "fingers");
                        //Direction dir = recognizeSwipeDirection(points);
                        Direction dir = recognizeSwipeDirectionBySpeed(events);

                        //XLog.tag("detector").i("swipe direction:" + dir + "with" + maxPointerNum + "finger(s)");
                        if (maxPointerNum == 1) {
                            handled = listener.onSwipe(dir);
                        } else if (maxPointerNum == 2) {
                            handled = listener.on2FingerSwipe(dir);
                        }
                    } else {
                        if (listener != null) {
                            handled = listener.onTap(event);
                        }
                    }
                }
                if (previousUpEvent != null) {
                    previousUpEvent.recycle();
                }
                previousUpEvent = currentUpEvent;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                isDoubleTapping = false;
                deferConfirmSingleTap = false;
                maxPointerNum = 0;
                break;
            default:
                break;
        }
        return  handled;
    }
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
        public boolean onSwipe(Direction direction);
        public boolean on2FingerSwipe(Direction direction);
        public boolean onTap(MotionEvent event);
        public boolean onDoubleTap(MotionEvent event);
    }
    private onGestureListener listener = null;

    public GestureDetector(onGestureListener gestureListener) {
         this.listener = gestureListener;
         handler = new GestureHandler();
         maxPointerNum = 0;
         points = new ArrayList<>();
         events = new ArrayList<>();
         createTemplates();
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
        if (d1 == Direction.UNKNOWN && d2 == Direction.UNKNOWN) return Direction.UNKNOWN;
        if (d1 == Direction.UNKNOWN) return d2;
        if (d2 == Direction.UNKNOWN) return d1;
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
