#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTImageLoader.h>
#else
#import "RCTBridgeModule.h"
#import "RCTImageLoader.h"
#endif

#import "TOCropView.h"

@interface RNYuntiImageCropper : NSObject <RCTBridgeModule>

@end
