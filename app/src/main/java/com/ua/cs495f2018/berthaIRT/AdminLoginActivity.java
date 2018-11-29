package com.ua.cs495f2018.berthaIRT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.ua.cs495f2018.berthaIRT.dialog.OkDialog;


public class AdminLoginActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    CardView bLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        etEmail = findViewById(R.id.adminlogin_input_email);
        etPassword = findViewById(R.id.adminlogin_input_password);

        etEmail.setText("ssinischo@gmail.com");
        etPassword.setText("111111");

        bLogin = findViewById(R.id.adminlogin_button_login);
        bLogin.setOnClickListener(x -> actionLogin());

        CardView bSignup = findViewById(R.id.adminlogin_button_signup);
        bSignup.setOnClickListener(x -> actionSignup());
    }

    private void actionLogin() {
        String sEmail = etEmail.getText().toString();
        String sPassword = etPassword.getText().toString();

            Client.performLogin(this, sEmail, sPassword, r -> {
                if (r.equals("INVALID_CREDENTIALS")){
                    etPassword.setText("");
                    etPassword.setError("Invalid email or password.");
                }
                else if (r.equals("SECURE")) {
                    startActivity(new Intent(AdminLoginActivity.this, AdminMainActivity.class));
                    finish();
                }
            });
    }

    private void actionSignup() {
        LayoutInflater flater = getLayoutInflater();
        @SuppressLint("InflateParams") View v = flater.inflate(R.layout.dialog_admin_signuptype, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        AlertDialog dialog = builder.create();

        v.findViewById(R.id.signuptype_button_newgroup).setOnClickListener(x -> {
            dialog.dismiss();
            actionNewGroup();
        });
        v.findViewById(R.id.signuptype_button_existinggroup).setOnClickListener(x -> {
            dialog.dismiss();
            new OkDialog(this, null, "To join an existing institution as an administrator, you must be invited by an existing administrator of that group.\n\nAdministrators may invite others via email address.", null).show();
        });
        dialog.show();
    }

    private void actionNewGroup() {
        LayoutInflater flater = getLayoutInflater();
        @SuppressLint("InflateParams") View v = flater.inflate(R.layout.dialog_admin_newgroup, null);

        EditText etNewEmail = v.findViewById(R.id.newgroup_input_email);
        EditText etNewInstitution = v.findViewById(R.id.newgroup_input_institution);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        AlertDialog dialog = builder.create();

        v.findViewById(R.id.newgroup_button_signup).setOnClickListener(x->
                Client.net.createGroup(AdminLoginActivity.this, etNewEmail.getText().toString(), etNewInstitution.getText().toString(), ()->{
                    dialog.dismiss();
                    etEmail.setText(etNewEmail.getText().toString());
                    etPassword.requestFocus();
                    new OkDialog(AdminLoginActivity.this, "Institution Created", "A temporary password has been sent to " + etNewEmail.getText().toString() + " along with further instructions.", null).show();
                })
        );
        dialog.show();
    }
}