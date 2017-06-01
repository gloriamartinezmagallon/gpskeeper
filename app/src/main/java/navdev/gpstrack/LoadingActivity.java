package navdev.gpstrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goToMain();
                    }
                });
            }
        }).start();
    }

    private void goToMain(){

        Intent intent = getIntent();
        System.out.println("Intent loading "+intent.getAction());

        Intent newintent = new Intent(LoadingActivity.this, MainActivity.class);
        if(Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_PICK.equals(intent.getAction())){
            newintent.setAction(intent.getAction());
            newintent.setData(intent.getData());
       }
        startActivity(newintent);
        finish();
    }



}
