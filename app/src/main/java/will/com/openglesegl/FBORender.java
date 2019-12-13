package will.com.openglesegl;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * FBO帧缓存
 */
public class FBORender {

    private Context mContext;
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

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private int mProgram;
    private int avPosition;
    private int afPosition;
    private int sTexture;
    private int vboId;


    public FBORender(Context context) {
        this.mContext = context;

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


    public void onCreate() {
        //加载glsl语言资源脚本
        String vertexSource = ShaderUtil.readRawResource(mContext,R.raw.vertex_shader);
        String fragmentSource = ShaderUtil.readRawResource(mContext,R.raw.fragment_shader);
        //创建程序
        mProgram = ShaderUtil.createProgram(vertexSource,fragmentSource);
        if (mProgram > 0) {
            //得到顶点着色器中的属性
            avPosition = GLES20.glGetAttribLocation(mProgram, "av_Position");
            //得到纹理着色器中的属性
            afPosition = GLES20.glGetAttribLocation(mProgram, "af_Position");

            //获取纹理设置
            sTexture = GLES20.glGetUniformLocation(mProgram, "sTexture");

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

        }
    }

    public void onChange(int width,int height){
        GLES20.glViewport(0,0,width,height);
    }

    public void onDraw(int textureId){
        GLES20.glClearColor(1f,0f,0f,1f);
        //使用源程序
        GLES20.glUseProgram(mProgram);

        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboId);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);

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
    }

}
