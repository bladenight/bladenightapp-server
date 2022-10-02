#! /bin/bash
#colors for bash
RED='\033[0;31m'
NOCOLOR='\033[0m'
GREEN='\033[0;32m'
#/.fastlane/bin/dependencies/include/openssl
#lines to adapt
DOMAIN='de.bladenight-muenchen'
COUNTRY='DE'
LOCATION='Munich'
STATE='Germany'
LOCALITY='Bavaria'
ORGANISATION='Skatemunich'
ORGANISATIONALUNIT='Bladenight'
COMMONNAME='bladenigth-muenchen.de'

SUBFOLDER='keys'
FILENAME="${ORGANISATIONALUNIT}"
PASSWORD='changeit'

printf "${RED}Alert.\nDo you want to create new certificate files?.\nThis will override old files and can not be undone! Enter: yes to start :"
read asking
if [[ $asking == yes* ]]; then
    printf "${GREEN} OK...Let us go ...${NOCOLOR}\n"
else
   printf "${RED} Canceled...Let us stop ...${NOCOLOR}\n"
   return 1
fi


#assetfolder for app
mkdir 'assets'
cd 'assets'
mkdir 'certs'
printf 'Create key folder %s\n' "$SUBFOLDER"
mkdir ${SUBFOLDER}
cd ${SUBFOLDER}

# Create root CA & Private key
openssl req -x509 \
            -sha256 -days 1024 \
            -nodes \
            -newkey rsa:2048 \
            -subj "/CN=${DOMAIN}/C=${COUNTRY}/L=${LOCATION}" \
            -keyout rootCA.key -out rootCA.crt

# Generate Private key
printf 'write keyfile %s.key\n' "$FILENAME"
openssl genrsa -out ${FILENAME}.key 2048

# Create csf conf

cat > csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = ${COUNTRY}      #Country (C)
ST = ${STATE}       #State (S)
L = ${LOCALITY}     #Locality (L)
O = ${ORGANISATION} #Organization (O)
OU = ${ORGANISATIONALUNIT}   #Organizational Unit (OU)
CN = ${COMMONNAME}      #Common Name (CN)

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = ${DOMAIN}
DNS.2 = www.${DOMAIN}
IP.1 = 127.0.0.1
IP.2 = 178.254.8.134
IP.3 = 87.106.198.210

EOF

# create CSR request using private key
print "create CSR request using private key"
openssl req -new -key ${FILENAME}.key -out ${FILENAME}.csr -config csr.conf

# Create a external config file for the certificate
print "Create a external config file for the certificate"
cat > cert.conf <<EOF

authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${DOMAIN}

EOF

# Create SSl with self signed CA
print "Create SSl with self signed CA"
openssl x509 -req \
    -in ${FILENAME}.csr \
    -CA rootCA.crt -CAkey rootCA.key \
    -CAcreateserial -out ${FILENAME}.crt \
    -days 3650 \
    -sha256 -extfile cert.conf

#create only localhost cert
print "create only localhost cert"
openssl req -x509 -out localhost.crt -keyout localhost.key \
  -newkey rsa:2048 -nodes -sha256 \
  -subj '/CN=localhost' -extensions EXT -config <( \
   printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")

#create pkcs12 - this file must the same on server
print "create pkcs12"
openssl pkcs12 -export -out ../certs/${FILENAME}.p12 -inkey ${FILENAME}.key -in ${FILENAME}.crt -password pass:${PASSWORD}

print "create pfx"
openssl pkcs12 -export -out ${FILENAME}.pfx -inkey rootCA.key -in ${FILENAME}.crt -in ${FILENAME}.crt -in rootca.crt -password pass:${PASSWORD}

printf "${RED}Use assets/certs/$FILENAME.p12 for server and keep password and other files in asset/keys/ on a safe place\n"