#!/bin/bash

# Following this guide to encrypt / decrypt files
# https://github.com/circleci/encrypted-files

# Encrypted using openssl 1.1.0g /usr/local/bin/openssl 

# Encrypt
#openssl aes-256-cbc -e -in key.p12 -out .circleci/key.p12.enc -k $ANDROID_NETWORK_TOOLS_DECRYPTKEY1

# Decrypt
openssl aes-256-cbc -d -in .circleci/key.p12.enc -out key.p12 -k $ANDROID_NETWORK_TOOLS_DECRYPTKEY1
openssl aes-256-cbc -d -in .circleci/keystore.enc -out keystore -k $ANDROID_NETWORK_TOOLS_DECRYPTKEY2
