#!/bin/bash
. config.sh

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

# # Only deploy releases if we are on the master branch
# if [[ $GIT_CURRENT_BRANCH != "master" ]]; then
#     echo "Not on master branch, so not deploying release"
#     exit 0
# fi


GIT_COMMIT_DESC=`git log -n 1 $CIRCLE_SHA1`
GIT_CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`

# Print the git commit message
echo "Git commit message: ${GIT_COMMIT_DESC}"

if [[ $GIT_COMMIT_DESC == *"#DEPLOY"* ]]; then
    upload_to_google_play "app"  
fi
