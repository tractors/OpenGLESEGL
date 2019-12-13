package will.com.openglesegl;

import android.content.Context;
import android.util.AttributeSet;

public class HMutiSurfaceView extends HEGLSurfaceView {
    private HMutiRender mHMutiRender;
    public HMutiSurfaceView(Context context) {
        this(context,null);
    }

    public HMutiSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HMutiSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHMutiRender = new HMutiRender(context);
        setRender(mHMutiRender);

    }

    public void setTextureId(int textureId,int index){
        if (mHMutiRender != null){
            mHMutiRender.setTextureId(textureId,index);
        }
    }
}
