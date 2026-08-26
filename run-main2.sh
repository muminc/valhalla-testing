#!/bin/sh
java --add-exports java.base/jdk.internal.value=ALL-UNNAMED --enable-preview -cp target/classes Main2
