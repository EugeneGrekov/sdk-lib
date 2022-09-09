package guru.msgs.notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowActivity extends AppCompatActivity {

    private int mCounter;
    private Button m_buttonNext;
    private ImageView m_imageView;
    private TextView m_heading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        m_heading = (TextView)findViewById(R.id.heading);
        m_imageView = (ImageView)findViewById(R.id.imageView);
        mCounter = 1;

        m_buttonNext = (Button)findViewById(R.id.buttonNext);
        m_buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NextStep();
            }
        });

        LinearLayout all_linear = (LinearLayout) findViewById(R.id.all_linear);
        all_linear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NextStep();
            }
        });

        String heading_str = getString(R.string.act_show_heading, 1);
        m_heading.setText(heading_str);

    }//

    @Override
    public void onBackPressed() {
        NextStep();
    }

    void NextStep() {
        mCounter++;

        String heading_str = getString(R.string.act_show_heading, mCounter);
        m_heading.setText(heading_str);

        switch (mCounter) {
            case 2:
                m_imageView.setImageResource(R.drawable.show02);
                return;
            case 3:
                m_imageView.setImageResource(R.drawable.show03);
                m_buttonNext.setText(R.string.act_show_btn_end);
                return;
            default:
                finish();
                return;
        }
    }//

}///