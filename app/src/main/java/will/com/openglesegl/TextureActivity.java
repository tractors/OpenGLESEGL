package will.com.openglesegl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class TextureActivity extends AppCompatActivity {

    private GLTextureView mGlTextureView;
    private LinearLayout mLyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mGlTextureView = findViewById(R.id.gl_surface_view);
        mLyContent = findViewById(R.id.ly_container);

        mGlTextureView.getTextureRender().setOnRenderCreateListener(new TextureRender.onRenderCreateListener() {
            @Override
            public void onCreate(final int textureId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mLyContent.getChildCount() >0 ){
                            mLyContent.removeAllViews();
                        }

                        for (int i = 0;i< 3; i++){
                            HMutiSurfaceView mutiSurfaceView = new HMutiSurfaceView(TextureActivity.this);
                            mutiSurfaceView.setTextureId(textureId,i);
                            mutiSurfaceView.setSurfaceAndEGLContext(null,mGlTextureView.getEGLContext());

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            lp.width = 200;
                            lp.height = 300;
                            mutiSurfaceView.setLayoutParams(lp);

                            mLyContent.addView(mutiSurfaceView);
                        }

                    }
                });
            }
        });
    }
}
