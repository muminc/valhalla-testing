#!/bin/sh
# -XX:+UnlockDiagnosticVMOptions \
#  -XX:+PrintInlineLayout \
#  -XX:+PrintFlatArrayLayout \
#  -XX:+PrintFieldLayout
java --add-exports java.base/jdk.internal.value=ALL-UNNAMED --add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED --enable-preview -cp target/classes MainValhalla