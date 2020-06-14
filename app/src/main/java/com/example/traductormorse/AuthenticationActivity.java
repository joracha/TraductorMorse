package com.example.traductormorse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
// Video de ayuda consultado: https://www.youtube.com/watch?v=mQyXbM6XGtE

public class AuthenticationActivity extends AppCompatActivity {

    private final String KEY_NAME = "somekeyname";
    private final String PROVIDER = "AndroidKeyStore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        // Validacion de que exista detector de huella digital en el dispositivo
        if (!fingerprintManager.isHardwareDetected()) {
            showError(1, "No hardware detected");
            return;
        }

        // Validacion de que el permiso de huella digital fue concedido a la aplicacion
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            showError(2, "No fingerprint permission");
            return;
        }

        // Validacion de que la huella digital este habilitada en el dispositivo
        if(!keyguardManager.isKeyguardSecure()){
            showError(3, "Keyguard not enabled");
            return;
        }

        // Objetos necesarios para recuperar los AES de las huellas digitales guardadas en el dispositivo
        Cipher cipher;
        try{
            KeyStore keyStore = KeyStore.getInstance(PROVIDER);
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            KeyGenerator keyGeneratorObject = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
            keyStore.load(null);
            keyGeneratorObject.init(new KeyGenParameterSpec.Builder(KEY_NAME,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());

            SecretKey secretKeyObject = keyGeneratorObject.generateKey();

            cipher.init(Cipher.ENCRYPT_MODE, secretKeyObject);
        }catch (Exception e){
            showError(4, e.getMessage());
            return;
        }

        //Evento para escuchar cuando se detecta una pulsasion en el sensor de huella, lo compara con el objeto crypto generado anteriormente

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new AuthenticationResult(getApplicationContext()), null);

    }

    private void showError(int id, String e){
        Toast.makeText(getApplicationContext(), String.valueOf(id) + " " + e , Toast.LENGTH_LONG).show();
        Log.e("Error", e);
    }
}