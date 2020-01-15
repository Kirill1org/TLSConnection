package bonch.dev.tlsconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private KeyStore trustStore;
    private KeyStore keyStore;
    private TrustManagerFactory trustManagerFactory;
    private KeyManagerFactory keyManagerFactory;

    private static final String IP = "192.168.0.102";
    private static final int PORT = 9090;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        loadKeyStoreCert();
        loadTrustStoreCert();
        Completable.fromAction(() -> initSSLSocket())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.e("Init socket", "is success");
                }, Throwable -> Log.e("Server recived error:", Throwable.getLocalizedMessage()));

    }

    private void initSSLSocket() {

        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        try (SSLSocket sslClientSocket = (SSLSocket) sslcontext.getSocketFactory().createSocket(IP, PORT);
             OutputStream outputStream = sslClientSocket.getOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            sslClientSocket.setUseClientMode(true);

            byte [] dataArray = initData();
            dataOutputStream.writeInt(dataArray.length);
            dataOutputStream.write(dataArray, 0, dataArray.length);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTrustStoreCert() {
        try (InputStream inputStream = getApplicationContext().getAssets().open("keystore.bks")) {

            trustStore = KeyStore.getInstance("JKS");
            trustStore.load(inputStream, "123qwe!@#QWE".toCharArray());

            initTrustManagerFactory();

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("LoadTrustCert error");
        }

    }

    private void initTrustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException {
        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
    }


    private void loadKeyStoreCert() {
        try (InputStream inputStream = getApplicationContext().getAssets().open("truststore.bks")) {
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(inputStream, "123qwe!@#QWE".toCharArray());

            initKeyManagerFactory();

        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("LoadServerCert error");
        }
    }

    private void initKeyManagerFactory() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

        keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "123qwe!@#QWE".toCharArray());
    }

    private byte[] initData() {

        SecureRandom secureRandom = new SecureRandom();
        int length = secureRandom.nextInt(100) + 1;
        byte[] dataArray = new byte[length];
        secureRandom.nextBytes(dataArray);
        return dataArray;
    }
}

