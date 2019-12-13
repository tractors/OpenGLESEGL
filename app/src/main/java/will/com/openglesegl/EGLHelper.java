package will.com.openglesegl;

import android.opengl.EGL14;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 自定义EGLHelper
 */
public class EGLHelper {

    //egl的实例对象
    private EGL10 mEgl;
    //egl的显示设备对象
    private EGLDisplay mEglDisplay;
    //egl的上下文
    private EGLContext mEglContext;
    //egl的surface
    private EGLSurface mEglSurface;

    /**
     * 初始化egl
     * @param surface
     * @param eglContext
     */
    public void initEgl(Surface surface, EGLContext eglContext){

        //1、得到egl的GL实例
        mEgl = (EGL10) EGLContext.getEGL();
        //2、获取默认的显示设备
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //判断是否获取到
        if (EGL10.EGL_NO_DISPLAY == mEglDisplay) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        //3、初始化设备并获取到版本
        int[] version = new int[2];

        if (!mEgl.eglInitialize(mEglDisplay,version)) {
            throw new RuntimeException("eglInitialize failed");
        }

        //4、设置显示设备的属性
        int[] attrbutes = new int[]{
                EGL10.EGL_RED_SIZE,8,
                EGL10.EGL_GREEN_SIZE,8,
                EGL10.EGL_BLUE_SIZE,8,
                EGL10.EGL_ALPHA_SIZE,8,
                EGL10.EGL_DEPTH_SIZE,8,
                EGL10.EGL_STENCIL_SIZE,8,
                EGL10.EGL_RENDERABLE_TYPE,4,
                EGL10.EGL_NONE
        };

        int[] num_config = new int[1];

        if (!mEgl.eglChooseConfig(mEglDisplay,attrbutes,null,1,num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        //5、从系统中获取相应的属性配置
        EGLConfig[] configs = new EGLConfig[numConfigs];

        if (!mEgl.eglChooseConfig(mEglDisplay,attrbutes,configs,numConfigs,num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        //6、创建EglContext
        //设置使用的GEL的版本
        int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL10.EGL_NONE};
        if (eglContext != null) {
            mEglContext = mEgl.eglCreateContext(mEglDisplay,configs[0], eglContext,attrib_list);
        } else {
            mEglContext = mEgl.eglCreateContext(mEglDisplay,configs[0],EGL10.EGL_NO_CONTEXT,attrib_list);
        }

        //7、创建渲染的Surface
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay,configs[0],surface,null);
        //8、把EglContext和surface绑定在显示设备当中
        if (!mEgl.eglMakeCurrent(mEglDisplay,mEglSurface,mEglSurface,mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * 9、刷新数据并显示在设备上,渲染场景
     * @return
     */
    public boolean swapBuffers(){
        if (mEgl != null) {
            return mEgl.eglSwapBuffers(mEglDisplay,mEglSurface);
        }
        throw new RuntimeException("egl is null");
    }

    /**
     * 获取eglContext
     * @return
     */
    public EGLContext getEglContext() {
        return mEglContext;
    }

    /**
     * 销毁egl
     */
    public void destoryEgl(){
        if (mEgl != null && mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
            mEgl.eglMakeCurrent(mEglDisplay,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_CONTEXT);

            mEgl.eglDestroySurface(mEglDisplay,mEglSurface);
            mEglSurface = null;
            mEgl.eglDestroyContext(mEglDisplay,mEglContext);
            mEglContext = null;

            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
            mEgl = null;
        }
    }
}
