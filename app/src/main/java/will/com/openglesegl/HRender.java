package will.com.openglesegl;


import android.opengl.GLES20;

public class HRender implements HEGLSurfaceView.HGLRender {

    public HRender() {
    }

    @Override
    public void onSurfaceCreate() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f,1.0f,0f,0f);
    }
}
