package com.example.otpverify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class verifyphone extends AppCompatActivity {
    TextView t;
    EditText e;
    Button b;
    private String otp;
    private FirebaseAuth mAuth;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifyphone);
        mAuth = FirebaseAuth.getInstance();
        String phone=getIntent().getStringExtra("phone");
        t=findViewById(R.id.sent);
        e=findViewById(R.id.otp);
        bar=findViewById(R.id.progressbar);
        b=findViewById(R.id.verify);

        t.setText(("Please Enter the code sent on your registered mobile number "+phone).toString());

        sendotp(phone);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code=e.getText().toString().trim();
                if(code.isEmpty() || code.length()<6) {
                    e.setError("Enter code");
                    e.requestFocus();
                    return;
                }
                verifycode(code);
            }
        });


    }

    private void verifycode(String code){
        PhoneAuthCredential pac=PhoneAuthProvider.getCredential(otp,code);
        signinwithcredentials(pac);
    }

    private void signinwithcredentials(PhoneAuthCredential pac) {
        mAuth.signInWithCredential(pac).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Intent i=new Intent(verifyphone.this,profile.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);


                }else{
                    Toast.makeText(verifyphone.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendotp(String number){
        bar.setVisibility(View.VISIBLE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);



        /*    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );*/
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            otp=s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if(code != null){

                e.setText(code);
                verifycode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(verifyphone.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
}