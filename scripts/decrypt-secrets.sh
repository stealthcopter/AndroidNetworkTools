#!/bin/bash

# Following this guide to encrypt / decrypt files
# https://github.com/circleci/encrypted-files

openssl aes-256-cbc -d -in .circleci/key.p12.enc -out key.p12 -k $ANDROID_NETWORK_TOOLS_DECRYPTKEY1