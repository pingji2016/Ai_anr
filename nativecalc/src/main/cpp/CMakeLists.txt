cmake_minimum_required(VERSION 3.22.1)
project(nativecalc)
add_library(nativecalc SHARED nativecalc.cpp)
target_compile_features(nativecalc PRIVATE cxx_std_17)
find_library(log-lib log)
find_library(jni-graphics jnigraphics)
target_link_libraries(nativecalc ${log-lib} ${jni-graphics})


#- $env:JAVA_HOME="C:\Users\pingji\.jdks\jbr-17.0.14"
#- $env:Path="$env:JAVA_HOME\bin;$env:Path"
#- java -version
#- .\gradlew.bat :nativecalc:externalNativeBuildRelease