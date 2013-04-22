KEYSTORE=$1.jks
STOREPASS=storepass

ALIAS=$1
KEYPASS=keypass

CA_KEYPASS=keypass

PKCS12_STOREPASS=storepass

# Generate key pair and store it in $KEYSTORE at alias $ALIAS
keytool -keystore $KEYSTORE -storepass $STOREPASS -genkeypair -alias $ALIAS -keyalg RSA -dname CN=$1 -keypass keypass

# Generate a CSR and use it to generate a cert ($1.pem) signed by CA
keytool -keystore $KEYSTORE -storepass $STOREPASS -certreq -alias $ALIAS -keypass "$KEYPASS" | keytool -keystore ca.jks -storetype JKS -storepass storepass -gencert -alias ca -keypass "$CA_KEYPASS" -rfc > $1.pem

# Import the CA cert into $KEYSTORE so that we can import the CSR reply
keytool -keystore $KEYSTORE -storepass $STOREPASS -importcert -alias ca -file ca.pem -noprompt

# Import the CSR reply (the cert issued by CA) into $KEYSTORE
keytool -keystore $KEYSTORE -storepass $STOREPASS -importcert -alias $ALIAS -keypass "$KEYPASS" -file $1.pem

# Export the cert and key pair in the PKCS#12 format to $1.p12
keytool -importkeystore -srckeystore $KEYSTORE -srcstorepass $STOREPASS -destkeystore $1.p12 -deststoretype PKCS12 -deststorepass "$PKCS12_STOREPASS" -srcalias $ALIAS -srckeypass "$KEYPASS"
