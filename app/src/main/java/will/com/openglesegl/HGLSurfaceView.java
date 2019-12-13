package will.com.openglesegl;

import android.content.Context;
import android.util.AttributeSet;

public class HGLSurfaceView extends HEGLSurfaceView {
    public HGLSurfaceView(Context context) {
        this(context,null);
    }

    public HGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new HRender());
    }
}
