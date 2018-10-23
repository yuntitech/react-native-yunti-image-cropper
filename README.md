# react-native-yunti-image-cropper

## Getting started

`$ npm install react-native-yunti-image-cropper --save`

### Mostly automatic installation

`$ react-native link react-native-yunti-image-cropper`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-yunti-image-cropper` and add `RNYuntiImageCropper.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNYuntiImageCropper.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import cn.bookln.rn.imagecropper.RNYuntiImageCropperPackage;` to the imports at the top of the file
  - Add `new RNYuntiImageCropperPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-yunti-image-cropper'
  	project(':react-native-yunti-image-cropper').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-yunti-image-cropper/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-yunti-image-cropper')
  	```

## Usage
```javascript
import RNYuntiImageCropper from 'react-native-yunti-image-cropper';

// TODO: What to do with the module?
RNYuntiImageCropper;
```
  