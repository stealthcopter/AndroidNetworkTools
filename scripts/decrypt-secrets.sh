#!/bin/bash

# Following this guide to encrypt / decrypt files
# https://github.com/circleci/encrypted-files

ls -al .circleci/

echo $ANDROID_NETWORK_TOOLS_DECRYPTKEY1

# Encrypt
#openssl aes-256-cbc -e -in key.p12 -out .circleci/key.p12.enc -k "${ANDROID_NETWORK_TOOLS_DECRYPTKEY1}"

# Decrypt
openssl aes-256-cbc -d -in .circleci/key.p12.enc -out key.p12 -k "${ANDROID_NETWORK_TOOLS_DECRYPTKEY1}"