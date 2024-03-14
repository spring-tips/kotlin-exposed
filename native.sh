#!/usr/bin/env bash

rm -rf target && rm -rf build &&  ./gradlew nativeCompile &&  ./build/native/nativeCompile/exposed
