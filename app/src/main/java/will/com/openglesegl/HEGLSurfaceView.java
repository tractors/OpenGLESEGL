package will.com.openglesegl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 自定义GLSurfaceView
 */
public abstract class HEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    //设置手动渲染
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    //设置系统自动渲染
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    private Surface mSurface;
    private EGLContext mEGLContext;
    private HEGLThread mHeglThread;
    private HGLRender mHGLRender;
    //渲染模式
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    //1、继承surfaceview，并实现其callback方法
    public HEGLSurfaceView(Context context) {
        this(context,null);
    }

    public HEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //添加回调，这个很重要，不加，重写的回调不起作用
        getHolder().addCallback(this);
    }

    /**
     * 设置surface和eglContext
     * @param surface
     * @param eglContext
     */
    public void setSurfaceAndEGLContext(Surface surface,EGLContext eglContext){
        this.mSurface = surface;
        this.mEGLContext = eglContext;
    }

    public void setRender(HGLRender hGLRender) {
        this.mHGLRender = hGLRender;
    }

    public void setRenderMode(int renderMode) {
        if (mHGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = renderMode;
    }

    public EGLContext getEGLContext() {
        if (mHeglThread != null) {
            return mHeglThread.getEGLContext();
        }

        return null;
    }

    public void requestRender(){
        if (mHeglThread != null) {
            mHeglThread.requestRender();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mSurface == null) {
            mSurface = surfaceHolder.getSurface();
        }

        mHeglThread = new HEGLThread(this);
        //设置HEGLTHread 线程已经创建
        mHeglThread.isCreate = true;
        mHeglThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        mHeglThread.width = width;
        mHeglThread.height = height;
        mHeglThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            mHeglThread.onDestory();
            mHeglThread = null;
            mSurface = null;
            mEGLContext = null;
    }

    /**
     * HEGLSurfaceView对外开放的接口
     */
    public interface HGLRender{
        //创建surafce
        void onSurfaceCreate();
        //surface状态改变
        void onSurfaceChanged(int width,int height);
        //绘制纹理
        void onDrawFrame();
    }

    /**
     * 创建一个内部的静态线程，有效控制内存泄漏EGLThread，为OpenGL提供绘制
     */
    static class HEGLThread extends Thread{
        WeakReference<HEGLSurfaceView> mHEGLSurfaceViewWeakReference;
        private EGLHelper mEGLHelper = null;
        private Object mObject = null;
        //是否退出
        private boolean isExit = false;
        //是否创建了线程
        private volatile boolean isCreate = false;
        //surface状态是否改变
        private volatile boolean isChange = false;
        //是否是第一次执行
        private volatile boolean isStart = false;
        //surface的宽
        private int width;
        //surface的高
        private int height;

        public HEGLThread(HEGLSurfaceView heglSurfaceView) {
            if (heglSurfaceView != null) {
                mHEGLSurfaceViewWeakReference = new WeakReference<HEGLSurfaceView>(heglSurfaceView);
            }
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            mEGLHelper = new EGLHelper();
            mObject = new Object();

            mEGLHelper.initEgl(mHEGLSurfaceViewWeakReference.get().mSurface,mHEGLSurfaceViewWeakReference.get().mEGLContext);

            while (true){

                if (isExit){
                    //释放资源
                    release();
                    break;
                }

                if (isStart){
                    //手动刷新
                    if (mHEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY){
                        synchronized (mObject){
                            try {
                                mObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //系统自动刷新
                    } else if (mHEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY){
                        try {
                            Thread.sleep(1000/60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value ");
                    }
                }


                onCreate();
                onChange(width,height);
                onDraw(mEGLHelper);

                isStart = true;
            }
        }

        private void onCreate(){
            if (isCreate && mHEGLSurfaceViewWeakReference.get().mHGLRender != null) {
                isCreate = false;
                mHEGLSurfaceViewWeakReference.get().mHGLRender.onSurfaceCreate();
            }
        }

        private void onChange(int width,int height){
            if (isChange && mHEGLSurfaceViewWeakReference.get().mHGLRender != null){
                isChange = false;
                mHEGLSurfaceViewWeakReference.get().mHGLRender.onSurfaceChanged(width,height);
            }
        }

        private void onDraw(EGLHelper eglHelper){
            if (mHEGLSurfaceViewWeakReference.get().mHGLRender != null && eglHelper != null) {
                //在第一次执行时，执行两次
                mHEGLSurfaceViewWeakReference.get().mHGLRender.onDrawFrame();
                if (!isStart) {
                    mHEGLSurfaceViewWeakReference.get().mHGLRender.onDrawFrame();
                }

                eglHelper.swapBuffers();
            }
        }

        /**
         * 唤醒等待
         */
        private void requestRender(){
            if (mObject != null) {
                synchronized (mObject){
                    mObject.notifyAll();
                }
            }
        }

        /**
         * 销毁
         */
        private void onDestory(){
            isExit = true;
            //唤醒等待
            requestRender();
        }

        /**
         * 释放资源
         */
        public void release(){
            if (mEGLHelper != null) {
                mEGLHelper.destoryEgl();
                mEGLHelper = null;
                mObject = null;
                mHEGLSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEGLContext(){
            if (mEGLHelper != null) {
                return mEGLHelper.getEglContext();
            }
            return null;
        }
    }
}
