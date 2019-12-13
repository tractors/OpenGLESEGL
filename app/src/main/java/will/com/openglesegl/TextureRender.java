package will.com.openglesegl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureRender implements HEGLSurfaceView.HGLRender {

    private float[] vertexData = {
            -1f,-1f,
            1f,-1f,
            -1f,1f,
            1f,1f
    };

    private final float[] textureData = {
            0f,0f,
            1f,0f,
            0f,1f,
            1f,1f
//            0f,1f,
//            1f,1f,
//            0f,0f,
//            1f,0f
//            0f,0.5f,
//            0.5f,0.5f,
//            0f,0f,
//            0.5f,0f
    };

    private Context mContext;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int mProgram;
    private int avPosition;
    private int afPosition;
    private int sTexture;
    private int textureId;
    private int vboId;
    private int fboId;
    private int imgTextureId;
    private FBORender mFBORender;
    private onRenderCreateListener mOnRenderCreateListener;

    private int uMatrix;
    private float[] matrix = new float[16];
    private int mWidthPixels;
    private int mHeightPixels;
    private int width;
    private int height;

    public TextureRender(Context context) {
        this.mContext = context;
        mFBORender = new FBORender(mContext);
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);
        textureBuffer.position(0);
    }

    public void setOnRenderCreateListener(onRenderCreateListener onRenderCreateListener) {
        this.mOnRenderCreateListener = onRenderCreateListener;
    }

    @Override
    public void onSurfaceCreate() {
        mFBORender.onCreate();
        //加载glsl语言资源脚本
        String vertexSource = ShaderUtil.readRawResource(mContext,R.raw.vertex_shader_m);
        String fragmentSource = ShaderUtil.readRawResource(mContext,R.raw.fragment_shader);
        //创建程序
        mProgram = ShaderUtil.createProgram(vertexSource,fragmentSource);
        if (mProgram > 0) {
            //得到顶点着色器中的属性
            avPosition = GLES20.glGetAttribLocation(mProgram,"av_Position");
            //得到纹理着色器中的属性
            afPosition = GLES20.glGetAttribLocation(mProgram,"af_Position");

            //获取纹理设置
            sTexture = GLES20.glGetUniformLocation(mProgram,"sTexture");
            //获取矩阵数据
            uMatrix = GLES20.glGetUniformLocation(mProgram,"u_Matrix");

            //创建vbo缓存
            int[] vbos = new int[1];
            GLES20.glGenBuffers(1,vbos,0);
            if (vbos[0] == 0) {
                return;
            }
            vboId = vbos[0];

            //绑定VBO
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboId);

            //GPU分配缓存大小
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,vertexData.length * 4 + textureData.length * 4,null,GLES20.GL_STATIC_DRAW);

            //把缓存数据设置到显存中
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,vertexData.length * 4,vertexBuffer);

            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,vertexData.length * 4,textureData.length * 4,textureBuffer);
            //解绑缓存
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

            //创建FBO
            int[] fbos = new int[1];
            GLES20.glGenFramebuffers(1,fbos,0);
            if (fbos[0] == 0) {
                return;
            }

            fboId = fbos[0];

            //绑定fbo
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,fboId);

            int[] textureIds = new int[1];
            //创建纹理
            GLES20.glGenTextures(1,textureIds,0);
            if (textureIds[0] == 0) {
                return;
            }

            //保存纹理
            textureId = textureIds[0];
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
            //激活纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(sTexture,0);


            //环绕方式（用重复）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
            //过滤方式(用线性)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

            //获取屏幕的宽高
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return;
            }

            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            mWidthPixels = outMetrics.widthPixels;
            mHeightPixels = outMetrics.heightPixels;
            //要在设置纹理之后，才能做绑定FBO
            //设置FBO分配内存大小
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidthPixels, mHeightPixels, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            //把纹理绑定到FBO上
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D,textureId,0);
            //检查纹理是否绑定在FBO上成功
            int result = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (result != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Bind Frame failed");
            }
            //解绑FBO
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

            //解绑纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

            imgTextureId = loadTexture(R.drawable.lyf);

            if (mOnRenderCreateListener != null) {
                mOnRenderCreateListener.onCreate(textureId);
            }
        }
    }

    private int loadTexture(@DrawableRes int resId){
        int[] textureIds = new int[1];
        //创建纹理
        GLES20.glGenTextures(1,textureIds,0);
        if (textureIds[0] == 0) {
            return 0;
        }
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0]);


        //环绕方式（用重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);
        //过滤方式(用线性)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),resId);
        if (bitmap == null) {
            return 0;
        }


        //设置图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();
        bitmap = null;
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        return textureIds[0];
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
//        GLES20.glViewport(0,0,width,height);
//        mFBORender.onChange(width,height);
        this.width = width;
        this.height = height;
        //横屏时
        if (width > height) {
            Matrix.orthoM(matrix,0,- width / ((height /827f)*550f),width / ((height /827f)*550f),-1f,1f,-1f,1f);

        } else {
            //竖屏时
            Matrix.orthoM(matrix,0,-1f,1f,-height /((width / 550f) * 827f),height /((width / 550f) * 827f),-1f,1f);

        }

        //使用矩阵做旋转
        Matrix.rotateM(matrix,0,180,1,0,0);

    }

    @Override
    public void onDrawFrame() {
        //设置屏幕大小
        GLES20.glViewport(0,0,mWidthPixels,mHeightPixels);
        //绑定了FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,fboId);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f,0f,1f);
        //使用源程序
        GLES20.glUseProgram(mProgram);
        //正交投影
        GLES20.glUniformMatrix4fv(uMatrix,1,false,matrix,0);

        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboId);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,imgTextureId);

        //使顶点属性数组有效
        GLES20.glEnableVertexAttribArray(avPosition);
        //为顶点属性赋值
        GLES20.glVertexAttribPointer(avPosition,2,GLES20.GL_FLOAT,false,8,0);
        //使纹理顶点属性数组有效
        GLES20.glEnableVertexAttribArray(afPosition);
        //为纹理顶点属性赋值
        GLES20.glVertexAttribPointer(afPosition,2,GLES20.GL_FLOAT,false,8,vertexData.length * 4);
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        //切换窗口绘制时，重新设置大小
        GLES20.glViewport(0,0,width,height);
        //解绑Fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        mFBORender.onDraw(textureId);
    }

    /**
     * 创建多渲染接口
     */
    public interface onRenderCreateListener{
        void onCreate(int textureId);
    }
}
