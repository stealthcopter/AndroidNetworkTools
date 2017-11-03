#!/bin/bash

# Fires a webhook to slack to notify of successful upload to beta
function webhook {

    gradle_app_name="$1"
    message="$2"
    channel="#mat-testing"
    git_hash=`git rev-parse --short HEAD`
    version=`cat ${gradle_app_name}/build.gradle | grep -m 1 versionName | cut -d'"' -f 2`
    
    # TESTING WEBHOOK https://hooks.slack.com/services/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
    
    app_name="Android Network Tools Sample App"
    icon_url="https://github.com/stealthcopter/AndroidNetworkTools/raw/master/app/src/main/res/mipmap-xhdpi/ic_launcher.png"
    
    echo $message
    echo $channel $gradle_app_name $app_name $version $icon_url

    curl -X POST --data-urlencode 'payload={"channel": "'"$channel"'", "username": "CirclCI Deployment Bot", "text": "*'"$app_name"'* version *'"$version"'*  <'"https://github.com/scottyab/rootbeer/commits/$git_hash"'|'"$git_hash"'> '"$message"'", "icon_url": "'"$icon_url"'"}'   https://hooks.slack.com/services/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

}  

# Uploads a build to Beta
function upload_to_beta {
    echo "Uploading $1 to Beta"
    
    if ./gradlew :$1:crashlyticsUploadDistributionRelease ; then
        webhook "${1}" "Uploading to Beta Succeeded"
    else
        webhook "${1}" "Uploading to Beta Play FAILED :("
    fi
}

# Uploads a build to Google Play
function upload_to_google_play {
    echo "Uploading $1 to Google Play"
    
    if ./gradlew :$1:publishApkRegularRelease ; then
        webhook "${1}" "Uploading to Google Play Succeeded"
    else
        webhook "${1}" "Uploading to Google Play FAILED :("
    fi
}

GIT_COMMIT_DESC=`git log -n 1 $CIRCLE_SHA1`
GIT_CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`


# Only deploy releases if we are on the master branch
# if [[ $GIT_CURRENT_BRANCH != "master" ]]; then
#     echo "Not on master branch, so not deploying"
#     exit 0
# fi


# Print the git commit message
echo "Git commit message: ${GIT_COMMIT_DESC}"

if [[ $GIT_COMMIT_DESC == *"#DEPLOY"* ]]; then
    upload_to_google_play "app"  
fi
