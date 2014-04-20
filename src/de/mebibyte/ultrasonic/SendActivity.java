package de.mebibyte.Sonic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendActivity extends Activity {

    private EditText editText;
    private Button sendButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        //final FFTView fftView = new FFTView(this);
        //getActionBar().setCustomView(fftView);
        //getActionBar().setDisplayShowCustomEnabled(true);

        //Receiver receiver = new Receiver(Config.SAMPLE_RATE);
        //receiver.register(fftView);

        final Sender sender = new Sender(Config.SAMPLE_RATE);

        editText = (EditText) findViewById(R.id.edittext_content);
        sendButton = (Button) findViewById(R.id.button_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sender.send(editText.getText().toString());
            }
        });

        // Dangerous stuff, segfaults right now...
        /* Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setRotate(0, 45);
        ColorFilter cf = new ColorMatrixColorFilter(cm);
        p.setColorFilter(cf);
        getWindow().getDecorView().setLayerType(View.LAYER_TYPE_SOFTWARE, p); */
    }

}
