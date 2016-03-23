package insectocide.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingsMenu extends Activity implements View.OnClickListener {

    private View instructionsButton;
    private View creditsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        instructionsButton = findViewById(R.id.InstructionsButton);
        instructionsButton.setOnClickListener(this);
        creditsButton = findViewById(R.id.CreditsButton);
        creditsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.InstructionsButton:
                instructionsButton.setBackgroundResource(R.drawable.instructionsbuttonpressed);
                Intent instructions = new Intent (this,Instructions.class);
                startActivity(instructions);
                break;
            case R.id.CreditsButton:
                creditsButton.setBackgroundResource(R.drawable.creditsbuttonpressed);
                Intent credits = new Intent (this,Credits.class);
                startActivity(credits);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        instructionsButton.setBackgroundResource(R.drawable.instructionsbutton);
        creditsButton.setBackgroundResource(R.drawable.creditsbutton);
    }
}
