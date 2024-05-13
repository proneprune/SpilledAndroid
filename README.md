# An application for android which can find a liquid spill and with a reference object, determine the volume of the spill.
This application was done as coursework in the course II1305 - Project in Information and Communication Technology at the Royal Institute of Technology in Stockholm, Sweden. The project was first intended to be developed as an application to find the volume of blood, but was broadened to allow all liquids. The course used Scrum as a framework for development and all work was done during 4 sprints lasting roughly a week each. The development team consists of 8 developers of which 5 worked with the android version.
### [Download here]()
### [Expo website](https://spilledowner.wixsite.com/spilled)
### [iOS Version](https://github.com/tiselius/Blapp_ios/)



## How to edit the application
### First download opencv for android https://opencv.org/releases/

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

need to follow this video to get the working rotation of the live camera
https://www.youtube.com/watch?v=aWaJXkMxY0c

