# An application for android that can scan blood spills and estimate the volume of blood.

## First download opencv for android https://opencv.org/releases/

import module in android studio
```new -> import module -> module name = :opencv```

directory 

```...\opencv-4.9.0-android-sdk\OpenCV-android-sdk\sdk```

add     ```implementation(project(":opencv"))```

into 
```.../app/build.gradle.kts```


add ```include(":opencv")```
into ```settings.gradle.kts```




## go into opencv
```...\OpenCV-android-sdk\sdk\native\libs```

create new folder ```jniLibs``` in
```src/main```
move the item from ```.../native/libs``` into there

go into 
```.../app/cpp/cmakeLists.txt```

change 

```set(OpenCV_DIR .../opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni)```


make sure you are running java 11 in opencv

