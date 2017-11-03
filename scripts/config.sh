#!/usr/bin/env bash
ENV_VAR_PREFIX="ANDROID_NETWORK_TOOLS"

# Slack webhook settings
SLACK_CHANNEL="#mat-testing"
SLACK_WEBHOOK_URL="https://hooks.slack.com/services/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"
ICON_URL="https://github.com/stealthcopter/AndroidNetworkTools/raw/master/app/src/main/res/mipmap-xhdpi/ic_launcher.png"

# Git info
GIT_URL="https://github.com/stealthcopter/AndroidNetworkTools"
GIT_TAG=`git name-rev --name-only --tags HEAD`
GIT_COMMIT_DESC=`git log -n 1 $CIRCLE_SHA1`
GIT_CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`

# Fires a webhook to slack to notify of successful upload to beta
function webhook {

    gradle_app_name="$1"
    app_name="$2"
    message="$3"
    git_hash=`git rev-parse --short HEAD`
    version=`cat ${gradle_app_name}/build.gradle | grep -m 1 versionName | cut -d'"' -f 2`
    
    echo $message
    echo $channel $gradle_app_name $app_name $version $ICON_URL

    curl -X POST --data-urlencode 'payload={"channel": "'"$SLACK_CHANNEL"'", "username": "CirclCI Deployment Bot", "text": "*'"$app_name"'* version *'"$version"'*  <'"$GIT_URL/commits/$git_hash"'|'"$git_hash"'> '"$message"'", "icon_url": "'"$ICON_URL"'"}' $SLACK_WEBHOOK_URL

}  
