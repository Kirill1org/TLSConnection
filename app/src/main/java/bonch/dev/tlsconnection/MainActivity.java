package bonch.dev.tlsconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    private KeyStore trustStore;
    private KeyStore keyStore;
    private TrustManagerFactory trustManagerFactory;
    private KeyManagerFactory keyManagerFactory;
    private SSLSocket sslClientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadTrustStoreCert();
        initTrustManagerFactory();
        loadClientCert();
        initKeyManagerFactory();
        initSSLSocket();
    }

    private void initSSLSocket() {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            sslClientSocket = (SSLSocket) sslcontext.getSocketFactory().createSocket("192.168.0.100",9090);
            sslClientSocket.setUseClientMode(true);



        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initKeyManagerFactory() {
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "12345678".toCharArray());
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

    }

    private void loadClientCert() {
        try {
            InputStream clientKeyStoreIS = getApplicationContext().getAssets().open("keystore.bks");
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(clientKeyStoreIS, "1234qwer!@#$QWER".toCharArray());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }

    private void initTrustManagerFactory() {

        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private void loadTrustStoreCert() {
        try {
            InputStream clientTrustStoreIS = getApplicationContext().getAssets().open("truststore.bks");
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(clientTrustStoreIS, "123qwe!@#QWE".toCharArray());
            Log.e("LOADED CERT IS:", String.valueOf(trustStore.size()));
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}

