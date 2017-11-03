 #!/bin/bash

# Reliably include our config file
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/config.sh"



function create_github_release { 

    version=`cat $1/build.gradle | grep -m 1 versionName | cut -d'"' -f 2`
    
    curl -v -i -X POST -H "Content-Type:application/json" -H "Authorization: token $GITHUB_RELEASE_TOKEN" -d '{"tag_name": "'$version'","name": "'$version'","body": '$GITHUB_RELEASE_DESC',"draft": true}' $GITHUB_RELEASE_URL

}

# Only deploy releases if we are on the master branch
# if [[ $GIT_CURRENT_BRANCH != "master" ]]; then
#     echo "Not on master branch, so not deploying release"
#     exit 0
# fi

if [[ $GIT_COMMIT_DESC == *"#RELEASE"* ]]; then
    echo "Creating github release"
    if create_github_release $GITHUB_RELEASE_MODULE; then
        webhook $GITHUB_RELEASE_MODULE "Created github release"
    else
        webhook $GITHUB_RELEASE_MODULE "Failed to create github release :("
    fi
fi