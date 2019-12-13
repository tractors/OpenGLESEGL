package will.com.openglesegl;

import android.content.Context;
import android.util.AttributeSet;

public class GLTextureView extends HEGLSurfaceView {
    private TextureRender mTextureRender = null;
    public GLTextureView(Context context) {
        this(context,null);
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextureRender = new TextureRender(context);
        setRender(mTextureRender);
    }

    public TextureRender getTextureRender() {
        return mTextureRender;
    }
}
