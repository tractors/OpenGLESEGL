package will.com.openglesegl;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 加载Shader工具类
 */
public class ShaderUtil {

    private static final String TAG = "ShaderUtil";
    /**
     * 读取资源内容
     * @param context
     * @param rawId
     * @return
     */
    public static String readRawResource(Context context, int rawId){
        StringBuffer sb = null;
        try {
            InputStream inputStream = context.getResources().openRawResource(rawId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null){
                sb.append(line).append("\n");
            }

            if (reader !=null) {
                reader.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * 加载shader
     * @param shaderType
     * @param source
     * @return
     */
    public static int loadShader(int shaderType,String source){
        //1、创建shader（着色器：顶点或片元）
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            //2、加载shader源码并编译shader
            GLES20.glShaderSource(shader,source);
            //2、加载shader源码并编译shader
            GLES20.glCompileShader(shader);
            int[] compile = new int[1];
            //3、检查是否编译成功
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compile,0);
            if (compile[0] != GLES20.GL_TRUE) {
                //删除编译
                Log.e(TAG, "shader compile error");
                GLES20.glDeleteShader(shader);
                return 0;
            }
            return shader;
        }

        return 0;
    }

    /**
     * 创建程式链接
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    public static int createProgram(String vertexSource, String fragmentSource){
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);

        if (vertexShader == 0) {
            return 0;
        }

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);

        if (fragmentShader == 0) {
            return 0;
        }
        //4、创建一个渲染程序
        int program = GLES20.glCreateProgram();

        if (program != 0) {
            //5、将着色器程序添加到渲染程序中
            GLES20.glAttachShader(program,vertexShader);
            GLES20.glAttachShader(program,fragmentShader);
            //6、链接程序源
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            //9、检查链接源程序是否成功
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.d("WIShader","link program is error");
                //删除链接源程序
                GLES20.glDeleteProgram(program);
                return 0;
            }
            return program;
        }

        return 0;
    }
}
