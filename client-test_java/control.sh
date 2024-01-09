#!/bin/bash
jar_full_path=""
jvm_opts=""
app_prop=""
operation=""

while [[ $# -gt 0 ]]; do
    # 将当前处理的命令行参数赋值给变量 key
    key="$1"

    case $key in
    --jar-full-path)
        jar_full_path=$2
        shift
        shift
        ;;
    --jvm_opts)
        jvm_opts=$2
        shift
        shift
        ;;
    --app_prop)
        app_prop=$2
        shift
        shift
        ;;
    --operation)
        operation=$2
        shift
        shift
        ;;
    *)
        shift
        ;;
    esac
done

Start() {
    java $jvm_opts -jar ${jar_full_path} -Dfile.encoding=utf-8 ${app_prop} -Djava.security.egd=file:/dev/./urandom &>/dev/null
}

Stop() {
    pid=$(ps -ef | grep -n "$jar_full_path" | grep -v grep | grep -v kill | awk '{print $2}')
    if [ ${pid} ]; then
        kill ${pid}
        for ((i = 0; i < 10; ++i)); do
            sleep 1
            pid=$(ps -ef | grep -n "$jar_full_path" | grep -v grep | grep -v kill | awk '{print $2}')
            if [ ${pid} ]; then
                echo -e ".\c"
            else
                echo 'Stop Success!'
                break
            fi
        done
        pid=$(ps -ef | grep -n "$jar_full_path" | grep -v grep | grep -v kill | awk '{print $2}')
        if [ ${pid} ]; then
            echo 'Kill Process!'
            kill -9 ${pid}
        fi
    else
        echo 'App already stop!'
    fi

}

case $operation in
"start")
    Start
    ;;
"stop")
    Stop
    ;;
"restart")
    Stop
    Start
    ;;
*)
    echo "unknown command"
    exit 1
    ;;
esac
