package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.net.InetAddress;

public class RegisterActivity extends AppCompatActivity {
    private EditText user, pass, v_pass;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.cornFlowerBlue));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        user = findViewById(R.id.R_user);
        pass = findViewById(R.id.R_pass);
        v_pass = findViewById(R.id.Rv_pass);
        submit = findViewById(R.id.R_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uname = String.valueOf(user.getText());
                String pas = String.valueOf(pass.getText());
                String vpas = String.valueOf(v_pass.getText());
                if (uname.equals("")) {
                    pass.setText("");
                    v_pass.setText("");
                    Toast.makeText(RegisterActivity.this, "用户名为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pas.equals(vpas)) {
                    register(uname, pas);
                    onBackPressed();
                } else {
                    pass.setText("");
                    v_pass.setText("");
                    Toast.makeText(RegisterActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void register(String userName, String password) {
        try {
            XMPPTCPConnection connection;
            XMPPTCPConnectionConfiguration configBuilder = XMPPTCPConnectionConfiguration.builder()
                    .setHostAddress(InetAddress.getByName(getString(R.string.serverHost)))
                    .setXmppDomain(getString(R.string.serverDomain))
                    .setSendPresence(true)
                    .setUsernameAndPassword("admin", "123456")
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                    .build();
            connection = new XMPPTCPConnection(configBuilder);
            // 连接服务器
            connection.connect();
            connection.login();
            AccountManager manager = AccountManager.getInstance(connection);
            manager.sensitiveOperationOverInsecureConnection(true);
            Localpart l_username = Localpart.from(userName);
            manager.createAccount(l_username, password);
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}