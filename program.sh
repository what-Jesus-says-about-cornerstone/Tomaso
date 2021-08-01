#!/bin/bash

shadow(){
    ./node_modules/.bin/shadow-cljs "$@"
}

server(){
    shadow -A:dev server
    # yarn server
}

dev(){

    npm i
    shadow -A:dev watch :main

}

compile(){
    npm i
    shadow -A:dev compile  :main
}

release(){
    npm i
    shadow -A:dev release  :main
}

js_ipfs_install(){
    cd /ctx/js-ipfs
    npm i
    ./node_modules/.bin/lerna bootstrap
}

main(){
    npm i
    compile
    js_ipfs_install
    daemon
}

start(){
    cd /ctx/js-ipfs/packages/ipfs
    bash init-and-daemon.sh
}

tree(){
    clojure -A:dev -Stree
}


cljs_compile(){
    clj -A:dev -m cljs.main -co cljs-build.edn -c
    #  clj -A:dev -m cljs.main -co cljs-build.edn -v -c # -r
}

"$@"